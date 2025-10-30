# 🪄 RuneSwipe

**RuneSwipe** is a gesture-based spellcasting game for Android where players draw ancient runes on the screen to cast powerful spells in real time.  
Built with **Kotlin** and **Jetpack Compose**, RuneSwipe combines handwriting recognition, neural gesture classification, and magical combat into one immersive experience.

---

## ✨ Overview

Players duel as wizards, tracing magical symbols (runes) to unleash fireballs, shields, and other elemental spells. Each rune is recognized by a lightweight neural network trained on hand-drawn gesture data.

RuneSwipe brings together:
- Real-time drawing capture
- Gesture recognition using PyTorch-based models
- Interactive spell animations and visual effects
- Kotlin + Compose UI with Material 3 design

---

## 🧙‍♂️ Features

- 🎨 **Rune Drawing Interface** – Trace magical patterns directly on-screen.  
- 🧠 **Neural Recognition Engine** – Classifies gestures into rune categories using a trained model.  
- 🔥 **Spellcasting System** – Cast offensive or defensive spells depending on the rune drawn.  
- 📜 **Tome Screen** – View all learned spells and their rune patterns.  
- 💾 **Dataset Builder** – Record and label new rune traces for model training.  
- 🧩 **Modular Architecture** – Easily extend the spellbook with new runes and effects.  

---

## 🧰 Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Model Runtime** | PyTorch / TorchScript |
| **Build Tool** | Gradle (KTS) |
| **Data Format** | JSON (trace data, model metadata) |

---

## 🗂️ Project Structure

```

RuneSwipe/
├── app/
│   ├── src/main/java/com/example/runeswipe/
│   │   ├── model/              # Data models (Spell, Point, Stroke, etc.)
│   │   ├── ui/                 # Compose UI components (RuneCanvas, TomeScreen, etc.)
│   │   └── MainActivity.kt     # App entry point
│   │   
│   ├── assets/                 # Saved rune datasets and trained model
│   └── res/                    # UI resources (icons, strings, colors)
├── dataset/                    # JSON gesture data for training
├── model.py                    # Neural recognition model (Python)
└── README.md                   # This file

````

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Giraffe (or newer)
- Kotlin 1.9+
- Gradle 8+
- Python 3.10+ (for training)
- PyTorch installed (`pip install torch torchvision`)

### Build & Run (Android)
```bash
git clone https://github.com/jfy4/RuneSwipe.git
````

Then install the APK on your Android device or run via Android Studio’s emulator.

### Train the Recognition Model (Python)

```bash
cd RuneSwipe
python model.py
```

This generates a `runes_model.pt` file, which is exported to TorchScript and loaded in the app.

---

## 📚 Gameplay Loop

1. Draw a rune pattern on the screen.
2. The gesture is vectorized and sent to the model.
3. The recognized spell is displayed (or "Unknown" if not matched).
4. TODO: The corresponding animation/effect plays (e.g. fireball, shield, lightning).

---

## 🧪 Example Dataset Format

Each rune trace is stored as JSON:

```json
{
  "strokes": [
    [
      { "x": 615.8, "y": 1057.6, "t": 145926693 },
      { "x": 601.7, "y": 1034.5, "t": 145926710 }
    ]
  ],
  "label": "fireball"
}
```

---

## 🤝 Contributing

Contributions are welcome!
If you’d like to add new runes, improve the recognition model, or design new spell effects:

1. Fork the repository
2. Create a new branch (`feature/new-spell`)
3. Commit and push your changes
4. Open a Pull Request

---

## 🪶 License

This project is licensed under the **MIT License** – see the LICENSE file for details.

---

## 🧭 Roadmap

* [ ] Add hundreds of spells
* [ ] Add multiplayer wizard duels
* [ ] Add particle-based spell effects
* [ ] Include wizard/witch custom appearence
* [ ] Expand the spell skill tree
* [ ] Include ability to "chain" spells
* [ ] Publish to Google Play

---

## 💡 Inspiration

RuneSwipe is inspired by the idea of merging **gesture recognition** with **fantasy spellcasting** — letting players “draw their magic” like a true wizard.
