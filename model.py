import os, json, torch, torch.nn as nn, torch.optim as optim
from torch.utils.data import Dataset, DataLoader, random_split
import numpy as np
# import matplotlib.pyplot as plt


# def estimate_max_points(root="dataset", percentile=95):
#     lengths = []
#     for lbl in os.listdir(root):
#         dirpath = os.path.join(root, lbl)
#         if not os.path.isdir(dirpath): 
#             continue
#         for fname in os.listdir(dirpath):
#             if not fname.endswith(".json"): 
#                 continue
#             path = os.path.join(dirpath, fname)
#             with open(path) as f:
#                 data = json.load(f)
#             n_points = sum(len(stroke) for stroke in data["strokes"])
#             lengths.append(n_points)
#     if not lengths:
#         raise RuntimeError("No JSON traces found under dataset/")
#     max_points = int(np.percentile(lengths, percentile))
#     print(f"Estimated MAX_POINTS = {max_points} (covers {percentile}% of samples)")
#     return max_points


# # --- call this once at the top of your training script ---
# MAX_POINTS = estimate_max_points("dataset", percentile=99)
# print("MAX POINTS = ", MAX_POINTS)
MAX_POINTS = 100
# print(MAX_POINTS)

def load_trace(path, MAX_POINTS=128):
    data = json.load(open(path))
    strokes = data["strokes"]  # list[list[{x,y,t}]]

    # --- collect all points ---
    all_pts = [p for s in strokes for p in s]
    if not all_pts:
        return np.zeros((MAX_POINTS, 4), dtype=np.float32)

    # normalize spatial coords by bounding box
    xs, ys = [p["x"] for p in all_pts], [p["y"] for p in all_pts]
    minx, maxx = min(xs), max(xs)
    miny, maxy = min(ys), max(ys)
    w = max(maxx - minx, 1e-6)
    h = max(maxy - miny, 1e-6)
    scale = 1.0 / max(w, h)

    def norm_xy(p):
        return ((p["x"] - minx) * scale, (p["y"] - miny) * scale)

    # normalize time to [0,1]
    ts = [p["t"] for p in all_pts]
    t0, tN = ts[0], ts[-1]
    tspan = max(tN - t0, 1e-6)

    # --- build [Δx, Δy, Δt, pen_lift] sequence ---
    seq = []
    prev_x = prev_y = None
    prev_t = None
    first_point = True

    for s in strokes:
        # if len(s) > 50:
        #     s = s[::2]
        if not s: continue
        for j, p in enumerate(s):
            x, y = norm_xy(p)
            t = (p["t"] - t0) / tspan
            if first_point:
                prev_x, prev_y, prev_t = x, y, t
                first_point = False
                continue
            dx = x - prev_x
            dy = y - prev_y
            dt = t - prev_t
            pen = 1.0 if j == 0 else 0.0
            seq.append([dx, dy, dt, pen])
            prev_x, prev_y, prev_t = x, y, t

    seq = np.asarray(seq, np.float32)
    # pad/trim
    # Downsample or pad the sequence
    if len(seq) > MAX_POINTS:
        indices = np.round(np.linspace(0, len(seq) - 1, MAX_POINTS)).astype(int)
        seq = seq[indices]
    else:
        seq = np.vstack([seq, np.zeros((MAX_POINTS - len(seq), 4), np.float32)])

    # if len(seq) < MAX_POINTS:
    #     seq = np.vstack([seq, np.zeros((MAX_POINTS - len(seq), 4), np.float32)])
    # else:
    #     seq = seq[:MAX_POINTS]

    return seq


class RuneDataset(Dataset):
    def __init__(self, root):
        self.paths, self.labels, self.label2id = [], [], {}
        lbls = sorted(os.listdir(root))
        for i, l in enumerate(lbls):
            dirpath = os.path.join(root, l)
            self.label2id[l] = i
            # print(dirpath)
            if not os.path.isdir(dirpath):
                continue
            print(f"Label {i}: {l}")
            jsons = [f for f in os.listdir(dirpath) if f.endswith(".json")]
            print(f"  Found {len(jsons)} JSONs")
            for f in jsons:
                self.paths.append(os.path.join(dirpath, f))
                self.labels.append(i)
    def __len__(self): return len(self.paths)
    def __getitem__(self, i):
        x = torch.tensor(load_trace(self.paths[i]))
        y = torch.tensor(self.labels[i])
        # fname = os.path.basename(self.paths[i])
        return x, y

    
# ---------- Transformer ----------
class StrokeTransformer(nn.Module):
    def __init__(self, num_classes, d_model=128, nhead=4, depth=3):
        super().__init__()
        self.input_proj = nn.Linear(4, d_model)
        layer = nn.TransformerEncoderLayer(d_model, nhead, 256, batch_first=True)
        self.enc = nn.TransformerEncoder(layer, depth)
        self.cls = nn.Linear(d_model, num_classes)
    def forward(self, x):
        x = self.input_proj(x)
        h = self.enc(x)
        return self.cls(h.mean(1))

    
# ---------- Train ----------
# --- build splits ---
root = "dataset"
ds = RuneDataset(root)
print("Label2id:", ds.label2id)
print("Num classes:", len(ds.label2id))
# exit()
device = "cuda" if torch.cuda.is_available() else "cpu"
val_frac = 0.1
n_val = int(len(ds)*val_frac)
n_train = len(ds) - n_val
train_ds, val_ds = random_split(ds, [n_train, n_val], generator=torch.Generator().manual_seed(42))

train_loader = DataLoader(train_ds, batch_size=16, shuffle=True)
val_loader   = DataLoader(val_ds,   batch_size=32, shuffle=False)

# --- model with a bit more regularization ---
model = StrokeTransformer(num_classes=len(ds.label2id), d_model=96, nhead=3, depth=2).to(device)


# label smoothing + weight decay helps
lossf = torch.nn.CrossEntropyLoss(label_smoothing=0.1)
opt = torch.optim.AdamW(model.parameters(), lr=1e-3, weight_decay=1e-4)

best_val = -1
patience, bad = 5, 0

for epoch in range(30):
    # train
    model.train()
    correct = total = 0
    for x,y in train_loader:
        x,y = x.to(device), y.to(device)
        opt.zero_grad()
        logits = model(x)
        loss = lossf(logits, y)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        opt.step()
        pred = logits.argmax(1)
        # for name, t, p in zip(fnames, y.tolist(), pred.tolist()):
        #     true_label = list(ds.label2id.keys())[t]
        #     pred_label = list(ds.label2id.keys())[p]
        #     print(f"{name:20s}  true={true_label:10s}  pred={pred_label:10s}")
        correct += (pred==y).sum().item(); total += y.size(0)
    train_acc = correct/total

    # validate
    model.eval()
    vcorrect = vtotal = 0
    vloss_sum = 0.0
    with torch.no_grad():
        for x,y in val_loader:
            x,y = x.to(device), y.to(device)
            logits = model(x)
            vloss = lossf(logits, y)
            vloss_sum += vloss.item()*y.size(0)
            pred = logits.argmax(1)
            vcorrect += (pred==y).sum().item(); vtotal += y.size(0)
    val_acc = vcorrect/vtotal
    val_loss = vloss_sum/vtotal
    print(f"ep {epoch+1:02d}  train_acc={train_acc:.3f}  val_acc={val_acc:.3f}  val_loss={val_loss:.3f}")


    # early stopping
    if val_acc > best_val:
        best_val = val_acc; bad = 0
        torch.save({"state_dict":model.state_dict(),
                    "labels": list(ds.label2id.keys())},
                   "artifacts/rune_seq.pt")
    else:
        bad += 1
        if bad >= patience:
            print("Early stopping."); break
