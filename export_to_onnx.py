import torch
from model import StrokeTransformer

# --- 1. Load the trained model checkpoint ---
ckpt = torch.load("artifacts/rune_seq.pt", map_location="cpu")
labels = ckpt["labels"]
print(labels)
num_classes = len(labels)

# --- 2. Re-create the same model architecture ---
model = StrokeTransformer(num_classes=num_classes, d_model=96, nhead=3, depth=2)
model.load_state_dict(ckpt["state_dict"])
model.eval()

# --- 3. Create a dummy input and export to ONNX ---
MAX_POINTS = 100      # use the same value you trained with, fixed at 100
dummy = torch.zeros(1, MAX_POINTS, 4, dtype=torch.float32)

torch.onnx.export(
    model,
    dummy,
    "artifacts/rune_seq.onnx",
    input_names=["trace"],
    output_names=["logits"],
    dynamic_axes={"trace": {0: "batch_size"}},  # allows variable batch size
    opset_version=17
)

print("✅ Exported ONNX model to artifacts/rune_seq.onnx")

