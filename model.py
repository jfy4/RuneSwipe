import os, json, torch, torch.nn as nn
from torch.utils.data import Dataset, DataLoader, random_split
import numpy as np
from torch.optim.lr_scheduler import LambdaLR, CosineAnnealingLR

# ───────────────────────────────────────────────────────────────
MAX_POINTS = 100
# ───────────────────────────────────────────────────────────────

def load_trace(path, MAX_POINTS=MAX_POINTS):
    data = json.load(open(path))
    strokes = data["strokes"]

    # --- collect all points ---
    all_pts = [p for s in strokes for p in s]
    if not all_pts:
        return np.zeros((MAX_POINTS, 4), dtype=np.float32)

    # normalize spatial coords
    xs, ys = [p["x"] for p in all_pts], [p["y"] for p in all_pts]
    minx, maxx = min(xs), max(xs)
    miny, maxy = min(ys), max(ys)
    w, h = max(maxx - minx, 1e-6), max(maxy - miny, 1e-6)
    scale = 1.0 / max(w, h)

    def norm_xy(p): return ((p["x"] - minx) * scale, (p["y"] - miny) * scale)

    # normalize time to [0,1]
    ts = [p["t"] for p in all_pts]
    t0, tN = ts[0], ts[-1]
    tspan = max(tN - t0, 1e-6)

    # build Δx, Δy, Δt, pen_lift sequence
    seq = []
    prev_x = prev_y = prev_t = None
    first = True
    for s in strokes:
        if not s: continue
        for j, p in enumerate(s):
            x, y = norm_xy(p)
            t = (p["t"] - t0) / tspan
            if first:
                prev_x, prev_y, prev_t = x, y, t
                first = False
                continue
            dx, dy, dt = x - prev_x, y - prev_y, t - prev_t
            pen = 1.0 if j == 0 else 0.0
            seq.append([dx, dy, dt, pen])
            prev_x, prev_y, prev_t = x, y, t

    seq = np.asarray(seq, np.float32)

    # ── Denoise / remove nearly-zero motion ─────────────────────
    if len(seq) > 0:
        mag = np.linalg.norm(seq[:, :2], axis=1)
        seq = seq[mag > 1e-5]

    # ── Standardize (zero mean, unit var) ───────────────────────
    if len(seq) > 0:
        mean, std = seq.mean(0, keepdims=True), seq.std(0, keepdims=True) + 1e-6
        seq = (seq - mean) / std

    # pad/trim
    if len(seq) > MAX_POINTS:
        idx = np.round(np.linspace(0, len(seq) - 1, MAX_POINTS)).astype(int)
        seq = seq[idx]
    else:
        seq = np.vstack([seq, np.zeros((MAX_POINTS - len(seq), 4), np.float32)])
    return seq


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
        x = torch.tensor(load_trace(self.paths[i]))
        y = torch.tensor(self.labels[i])
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
    def __init__(self, num_classes, d_model=96, nhead=3, depth=2):
        super().__init__()
        self.input_proj = nn.Linear(4, d_model)
        self.posenc = PositionalEncoding(d_model)
        layer = nn.TransformerEncoderLayer(d_model, nhead, 256, batch_first=True)
        self.enc = nn.TransformerEncoder(layer, depth)
        self.cls = nn.Linear(d_model, num_classes)

    def forward(self, x):
        x = self.input_proj(x)
        x = self.posenc(x)
        h = self.enc(x)
        return self.cls(h.mean(1))


# ───────────────────────────────────────────────────────────────
if __name__ == "__main__":
    root = "dataset"
    ds = RuneDataset(root)
    print("Label2id:", ds.label2id)
    print("Num classes:", len(ds.label2id))
    exit()
    device = "cuda" if torch.cuda.is_available() else "cpu"

    val_frac = 0.2
    n_val = int(len(ds) * val_frac)
    n_train = len(ds) - n_val
    train_ds, val_ds = random_split(ds, [n_train, n_val],
                                    generator=torch.Generator().manual_seed(42))
    train_loader = DataLoader(train_ds, batch_size=16, shuffle=True)
    val_loader   = DataLoader(val_ds, batch_size=32, shuffle=False)

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
            for x, y, _ in val_loader:
                x, y = x.to(device), y.to(device)
                logits = model(x)
                vloss = lossf(logits, y)
                vloss_sum += vloss.item() * y.size(0)
                vcorrect += (logits.argmax(1) == y).sum().item()
                vtotal += y.size(0)
        val_acc = vcorrect / vtotal
        val_loss = vloss_sum / vtotal
        print(f"ep {epoch+1:02d}  train_acc={train_acc:.3f}  val_acc={val_acc:.3f}  val_loss={val_loss:.3f}")

        if val_acc > best_val:
            best_val, bad = val_acc, 0
            os.makedirs("artifacts", exist_ok=True)
            torch.save({"state_dict": model.state_dict(),
                        "labels": list(ds.label2id.keys())},
                       "artifacts/rune_seq.pt")
        else:
            bad += 1
            if bad >= patience:
                print("Early stopping.")
                break
