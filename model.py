import os, json, torch, torch.nn as nn
from torch.utils.data import Dataset, DataLoader, random_split
import numpy as np
from torch.optim.lr_scheduler import LambdaLR

# ───────────────────────────────────────────────────────────────
MAX_POINTS = 100
# ───────────────────────────────────────────────────────────────


def load_raw_points(path):
    data = json.load(open(path))
    strokes = data["strokes"]

    pts = []
    for s in strokes:
        for j,p in enumerate(s):
            x, y, t = p["x"], p["y"], p["t"]
            pen = 1.0 if j == 0 else 0.0
            pts.append([x, y, t, pen])

    if len(pts) == 0:
        return np.zeros((1,4), np.float32)

    return np.array(pts, np.float32)


class StrokePreprocessor(nn.Module):
    def __init__(self, max_points=100):
        super().__init__()
        self.max_points = max_points

    def forward(self, pts):
        """
        pts: (B, N, 3 or 4)
        pts[...,0] = x
        pts[...,1] = y
        pts[...,2] = t
        pts[...,3] = pen (optional)
        """

        # 1) normalize x,y
        xs = pts[..., 0]
        ys = pts[..., 1]
        minx = xs.min(dim=1, keepdim=True).values
        maxx = xs.max(dim=1, keepdim=True).values
        miny = ys.min(dim=1, keepdim=True).values
        maxy = ys.max(dim=1, keepdim=True).values
        range_x = (maxx - minx).clamp(min=1e-6)
        range_y = (maxy - miny).clamp(min=1e-6)
        scale = 1.0 / torch.maximum(range_x, range_y)

        x_norm = (xs - minx) * scale
        y_norm = (ys - miny) * scale

        # 2) normalize t to [0,1]
        ts = pts[..., 2]
        t0 = ts[:, 0:1]
        tN = ts[:, -1:]
        tspan = (tN - t0).clamp(min=1e-6)
        t_norm = (ts - t0) / tspan

        # 3) reconstruct pen (first point = 1, others = 0)
        B, N, _ = pts.shape
        pen = torch.zeros(B, N, device=pts.device)
        pen[:, 0] = 1.0
        pen = pen.unsqueeze(-1)

        pts_norm = torch.stack([x_norm, y_norm, t_norm], dim=-1)
        pts_norm = torch.cat([pts_norm, pen], dim=-1)  # (B,N,4)

        # 4) downsample / upsample to MAX_POINTS+1
        maxp = self.max_points + 1
        N_current = pts_norm.shape[1]

        # Compute uniform index mapping: linspace(0, N-1, maxp)
        idx_float = torch.linspace(0, N_current - 1, maxp, device=pts.device)
        idx = idx_float.round().long()
        idx = idx.clamp(max=N_current - 1)
        pts_resampled = pts_norm[:, idx]

        # 5) compute deltas (dx,dy,dt)
        diffs = pts_resampled[:, 1:, :3] - pts_resampled[:, :-1, :3]
        pen_seq = pts_resampled[:, 1:, 3:4]

        seq = torch.cat([diffs, pen_seq], dim=-1)  # (B, max_points, 4)

        # 6) magnitude filter — ONNX-friendly version
        mag = torch.sqrt((seq[..., 0] ** 2 + seq[..., 1] ** 2))
        keep = mag > 1e-5
        # You can't shrink tensors dynamically in ONNX, so instead: zero out
        seq = seq * keep.unsqueeze(-1)

        # 7) per-sample standardization
        mean = seq.mean(dim=1, keepdim=True)
        std = seq.std(dim=1, keepdim=True) + 1e-6
        seq = (seq - mean) / std

        # 8) final trim to MAX_POINTS
        if seq.shape[1] > self.max_points:
            seq = seq[:, :self.max_points]

        return seq


def collate_pad(batch):
    xs, ys, names = zip(*batch)

    # find max length in this batch
    max_len = max(x.shape[0] for x in xs)

    # pad to (B, max_len, 4)
    padded = []
    for x in xs:
        L = x.shape[0]
        if L < max_len:
            pad = torch.zeros(max_len - L, 4)
            padded.append(torch.cat([x, pad], dim=0))
        else:
            padded.append(x)
    xs = torch.stack(padded, dim=0)

    ys = torch.tensor(ys)
    return xs, ys, names    


# ───────────────────────────────────────────────────────────────
class RuneDataset(Dataset):
    def __init__(self, root):
        self.paths, self.labels, self.label2id = [], [], {}
        lbls = sorted(os.listdir(root))
        for i, l in enumerate(lbls):
            print(f"Label {i}: {l}")
            d = os.path.join(root, l)
            if not os.path.isdir(d): continue
            self.label2id[l] = i
            js = [f for f in os.listdir(d) if f.endswith(".json")]
            print(f" Found {len(js)} JSONs")
            for f in js:
                self.paths.append(os.path.join(d, f))
                self.labels.append(i)

    def __len__(self): return len(self.paths)

    def __getitem__(self, i):
        raw = load_raw_points(self.paths[i])    # returns (N,4)
        x = torch.tensor(raw, dtype=torch.float32)
        y = torch.tensor(self.labels[i], dtype=torch.long)
        return x, y, os.path.basename(self.paths[i])


# ───────────────────────────────────────────────────────────────
# Positional Encoding
class PositionalEncoding(nn.Module):
    def __init__(self, d_model, max_len=MAX_POINTS):
        super().__init__()
        pe = torch.zeros(max_len, d_model)
        pos = torch.arange(0, max_len, dtype=torch.float).unsqueeze(1)
        div = torch.exp(torch.arange(0, d_model, 2) * (-np.log(10000.0) / d_model))
        pe[:, 0::2] = torch.sin(pos * div)
        pe[:, 1::2] = torch.cos(pos * div)
        self.register_buffer("pe", pe.unsqueeze(0))

    def forward(self, x):
        return x + self.pe[:, :x.size(1)]


# ───────────────────────────────────────────────────────────────
class StrokeTransformer(nn.Module):
    def __init__(self, num_classes, d_model=96, nhead=3, depth=2, max_points=100):
        super().__init__()
        self.pre = StrokePreprocessor(max_points=max_points)
        self.input_proj = nn.Linear(4, d_model)
        self.posenc = PositionalEncoding(d_model, max_len=max_points)
        layer = nn.TransformerEncoderLayer(d_model, nhead, 256, batch_first=True)
        self.enc = nn.TransformerEncoder(layer, depth)
        self.cls = nn.Linear(d_model, num_classes)

    def forward(self, raw_pts):
        """
        raw_pts: (B, N, 3 or 4) raw x,y,t,pen from JSON or from Kotlin
        """
        x = self.pre(raw_pts)           # ✔ preprocessing inside ONNX
        x = self.input_proj(x)
        x = self.posenc(x)
        h = self.enc(x)
        return self.cls(h.mean(1))


# ───────────────────────────────────────────────────────────────
if __name__ == "__main__":
    # print(load_trace("./app/src/main/assets/sample_trace.json").flatten()[:8])
    # exit()
    root = "dataset"
    ds = RuneDataset(root)
    print("Label2id:", ds.label2id)
    print("Num classes:", len(ds.label2id))
    # exit()
    device = "cuda" if torch.cuda.is_available() else "cpu"

    val_frac = 0.1
    n_val = int(len(ds) * val_frac)
    n_train = len(ds) - n_val
    train_ds, val_ds = random_split(ds, [n_train, n_val],
                                    generator=torch.Generator().manual_seed(42))
    train_loader = DataLoader(train_ds, batch_size=16, shuffle=True, collate_fn=collate_pad)
    val_loader   = DataLoader(val_ds,   batch_size=32, shuffle=False, collate_fn=collate_pad)

    model = StrokeTransformer(num_classes=len(ds.label2id),
                              d_model=96, nhead=3, depth=2).to(device)

    lossf = nn.CrossEntropyLoss(label_smoothing=0.1)
    opt = torch.optim.AdamW(model.parameters(), lr=1e-3, weight_decay=1e-4)

    # ── Warmup + CosineAnnealingLR combined ─────────────────────
    warmup_steps = 200
    total_epochs = 30
    total_steps = total_epochs * len(train_loader)

    def lr_lambda(step):
        if step < warmup_steps:
            return step / warmup_steps
        progress = (step - warmup_steps) / float(max(1, total_steps - warmup_steps))
        return 0.5 * (1.0 + np.cos(np.pi * progress))

    scheduler = LambdaLR(opt, lr_lambda)

    best_val, patience, bad = -1, 10, 0
    step = 0

    for epoch in range(total_epochs):
        model.train()
        correct = total = 0
        for x, y, _ in train_loader:
            x, y = x.to(device), y.to(device)
            opt.zero_grad()
            logits = model(x)
            loss = lossf(logits, y)
            loss.backward()
            nn.utils.clip_grad_norm_(model.parameters(), 1.0)
            opt.step()
            scheduler.step()
            step += 1
            pred = logits.argmax(1)
            correct += (pred == y).sum().item()
            total += y.size(0)
        train_acc = correct / total

        # Validation
        model.eval()
        vcorrect = vtotal = vloss_sum = 0
        with torch.no_grad():
            for x, y, fnames in val_loader:
                x, y = x.to(device), y.to(device)
                logits = model(x)
                vloss = lossf(logits, y)
                vloss_sum += vloss.item() * y.size(0)
                vcorrect += (logits.argmax(1) == y).sum().item()
                vtotal += y.size(0)
                # ✅ Print true label, predicted label, and filename
                # for name, t, p in zip(fnames, y.tolist(), logits.argmax(1).tolist()):
                #     true_label = list(ds.label2id.keys())[t]
                #     pred_label = list(ds.label2id.keys())[p]
                #     print(f"{name:20s}  true={true_label:10s}  pred={pred_label:10s}")
        val_acc = vcorrect / vtotal
        val_loss = vloss_sum / vtotal
        print(f"ep {epoch+1:02d}  train_acc={train_acc:.3f}  val_acc={val_acc:.3f}  val_loss={val_loss:.3f}")

        if val_acc > best_val:
            best_val, bad = val_acc, 0
            os.makedirs("artifacts", exist_ok=True)

            # Save PyTorch model
            torch.save({"state_dict": model.state_dict(),
                        "labels": list(ds.label2id.keys())},
                       "artifacts/rune_seq.pt")

            # ───────────────────────────────────────────────────────────────
            # Export ONNX model (raw → fully preprocessed → transformer)
            # ───────────────────────────────────────────────────────────────
            model.eval()

            # Dummy input: batch size 1, variable-length allowed (e.g. 200 raw points)
            dummy = torch.zeros(1, 200, 4, dtype=torch.float32).to(device)

            torch.onnx.export(
                model,
                dummy,
                "artifacts/rune_seq.onnx",
                input_names=["raw_points"],
                output_names=["logits"],
                opset_version=17,
                dynamic_axes={
                    "raw_points": {1: "num_points"},   # allow variable N
                    "logits": {0: "batch_size"}
                }
            )

            print("Exported ONNX model → artifacts/rune_seq.onnx")
        else:
            bad += 1
            if bad >= patience:
                print("Early stopping.")
                break
