import numpy as np
import json
import os
from datetime import datetime


def random_trace(num_strokes=1, num_points=40, jitter=0.1, seed=None):
    """
    Generate a single random trace (list of strokes).
    Each stroke is a list of {x, y, t}.
    """
    if seed is not None:
        np.random.seed(seed)

    strokes = []
    t = 0
    for _ in range(num_strokes):
        # random start position
        x0, y0 = np.random.rand(2)
        pts = []
        for i in range(num_points):
            dx, dy = np.random.randn(2) * jitter
            x0 = np.clip(x0 + dx, 0, 1)
            y0 = np.clip(y0 + dy, 0, 1)
            t += np.random.randint(5, 20)  # time step in ms
            pts.append({"x": float(x0), "y": float(y0), "t": int(t)})
        strokes.append(pts)
    return {"strokes": strokes}


out_dir = "./dataset/Unknown"
os.makedirs(out_dir, exist_ok=True)

for i in range(100):  # make 100 random traces
    sample = random_trace(
        num_strokes=np.random.randint(1, 4),
        num_points=np.random.randint(10, 60),
        jitter=np.random.uniform(0.05, 0.25)
    )
    ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
    path = os.path.join(out_dir, f"noise_{ts}.json")
    with open(path, "w") as f:
        json.dump(sample, f)
        
