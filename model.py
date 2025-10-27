import os, json, torch, torch.nn as nn, torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import numpy as np


def estimate_max_points(root="dataset", percentile=95):
    lengths = []
    for lbl in os.listdir(root):
        dirpath = os.path.join(root, lbl)
        if not os.path.isdir(dirpath): 
            continue
        for fname in os.listdir(dirpath):
            if not fname.endswith(".json"): 
                continue
            path = os.path.join(dirpath, fname)
            with open(path) as f:
                data = json.load(f)
            n_points = sum(len(stroke) for stroke in data["strokes"])
            lengths.append(n_points)
    if not lengths:
        raise RuntimeError("No JSON traces found under dataset/")
    max_points = int(np.percentile(lengths, percentile))
    print(f"Estimated MAX_POINTS = {max_points} (covers {percentile}% of samples)")
    return max_points


# --- call this once at the top of your training script ---
MAX_POINTS = estimate_max_points("dataset", percentile=99)
print("MAX POINTS = ", MAX_POINTS)
# exit()
# MAX_POINTS = 128


# ---------- Dataset ----------
# def load_trace(path):
#     data = json.load(open(path))
#     strokes = data["strokes"]
#     pts = np.array([[p["x"], p["y"], p["t"]] for s in strokes for p in s], dtype=np.float32)
#     pts[:, :2] = (pts[:, :2] - pts[:, :2].min(0)) / (np.ptp(pts[:, :2], axis=0) + 1e-6)
#     pts[:, 2] -= pts[0, 2]
#     pts[:, 2] /= (pts[-1, 2] + 1e-6)
#     # Δx, Δy, pen_lift
#     seq = []
#     prev = pts[0]
#     # print(prev)
#     for s in strokes:
#         print(s)
#         for i, p in enumerate(s):
#             if np.all(p == prev): continue
#             dx, dy = p[0]-prev[0], p[1]-prev[1]
#             pen = 1.0 if i==0 else 0.0
#             seq.append([dx,dy,pen])
#             prev = p
#     seq = np.array(seq, np.float32)
#     if len(seq) < MAX_POINTS:
#         seq = np.pad(seq, ((0,MAX_POINTS-len(seq)),(0,0)))
#     else:
#         seq = seq[:MAX_POINTS]
#     return seq

def load_trace(path, MAX_POINTS=128):
    data = json.load(open(path))
    strokes = data["strokes"]  # list[list[{x,y,t}]]

    # --- normalize x,y to [0,1] by bbox ---
    all_xy = [(pt["x"], pt["y"]) for s in strokes for pt in s]
    if not all_xy:
        # empty trace -> all zeros
        return np.zeros((MAX_POINTS, 3), dtype=np.float32)

    xs, ys = zip(*all_xy)
    minx, maxx = min(xs), max(xs)
    miny, maxy = min(ys), max(ys)
    w = max(maxx - minx, 1e-6)
    h = max(maxy - miny, 1e-6)
    scale = 1.0 / max(w, h)

    def norm_xy(pt):
        return ((pt["x"] - minx) * scale, (pt["y"] - miny) * scale)

    # --- build sequence: [Δx, Δy, pen_lift] ---
    seq = []
    prev_x = prev_y = None
    first_point = True

    for s in strokes:
        if not s:
            continue
        for j, pt in enumerate(s):
            x, y = norm_xy(pt)
            if first_point:
                # no delta for very first point
                prev_x, prev_y = x, y
                first_point = False
                continue
            dx = x - prev_x
            dy = y - prev_y
            pen_lift = 1.0 if j == 0 else 0.0  # first point of a stroke (except the very first overall)
            seq.append([dx, dy, pen_lift])
            prev_x, prev_y = x, y

    seq = np.asarray(seq, dtype=np.float32)

    # pad/trim to MAX_POINTS
    if len(seq) < MAX_POINTS:
        pad = np.zeros((MAX_POINTS - len(seq), 3), dtype=np.float32)
        seq = np.vstack([seq, pad])
    else:
        seq = seq[:MAX_POINTS]

    return seq


class RuneDataset(Dataset):
    def __init__(self, root):
        self.paths, self.labels, self.label2id = [], [], {}
        lbls = sorted(os.listdir(root))
        for i,l in enumerate(lbls):
            self.label2id[l] = i
            for f in os.listdir(os.path.join(root,l)):
                if f.endswith(".json"):
                    self.paths.append(os.path.join(root,l,f))
                    self.labels.append(i)
    def __len__(self): return len(self.paths)
    def __getitem__(self, i):
        return torch.tensor(load_trace(self.paths[i])), torch.tensor(self.labels[i])

    
# ---------- Transformer ----------
class StrokeTransformer(nn.Module):
    def __init__(self, num_classes, d_model=128, nhead=4, depth=3):
        super().__init__()
        self.input_proj = nn.Linear(3, d_model)
        layer = nn.TransformerEncoderLayer(d_model, nhead, 256, batch_first=True)
        self.enc = nn.TransformerEncoder(layer, depth)
        self.cls = nn.Linear(d_model, num_classes)
    def forward(self, x):
        x = self.input_proj(x)
        h = self.enc(x)
        return self.cls(h.mean(1))

    
# ---------- Train ----------
# --- build splits ---
from torch.utils.data import random_split, DataLoader
ds = RuneDataset(root)
val_frac = 0.2
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


# root = "dataset"
# ds = RuneDataset(root)
# loader = DataLoader(ds, batch_size=32, shuffle=True)
# device = "cuda" if torch.cuda.is_available() else "cpu"
# model = StrokeTransformer(num_classes=len(ds.label2id)).to(device)
# opt = optim.Adam(model.parameters(), lr=1e-3)
# lossf = nn.CrossEntropyLoss()

# for epoch in range(15):
#     model.train()
#     total, correct = 0,0
#     for x,y in loader:
#         x,y = x.to(device), y.to(device)
#         opt.zero_grad()
#         out = model(x)
#         loss = lossf(out,y)
#         loss.backward()
#         opt.step()
#         pred = out.argmax(1)
#         total += len(y)
#         correct += (pred==y).sum().item()
#     print(f"Epoch {epoch+1:02d}  acc={correct/total:.3f}")

# torch.save({"state_dict":model.state_dict(),
#             "labels": list(ds.label2id.keys())},
#            "artifacts/rune_seq.pt")
