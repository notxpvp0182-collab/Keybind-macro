# ⌨️ Keyboard Macro
**Created by NoTXGameR**

Ultra-fast keybind & macro system for Minecraft 1.21.4 (Fabric).  
Assign any key/mouse button to instant multi-action chains — built for PvP and utility.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-green)
![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://github.com/YOUR_USERNAME/keyboard-macro/actions/workflows/build.yml/badge.svg)

---

## ✨ Features
- Unlimited customizable keybind profiles
- 11 action types: hotbar select, right/left click, jump, sneak, sprint, swap hands, drop item, open inventory, chat command, delay
- Zero-delay execution engine (same-tick firing)
- Water Clutch & Fast Pearl presets included
- Full key + mouse button capture
- JSON config with auto-save, backup & corruption recovery
- Modern dark GUI — search, scroll, add/edit/delete/duplicate

## 📦 Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for **1.21.4**
2. Put **Fabric API** in your `mods` folder
3. Put **Mod Menu** in your `mods` folder *(recommended)*
4. Drop the `.jar` into `.minecraft/mods`
5. Launch!

## 🎮 Usage
| Action | How |
|--------|-----|
| Open GUI | Press `K` in-game |
| Open via Mod Menu | ESC → Mods → Keyboard Macro → Config |
| Create profile | Click `+ Add` |
| Assign key | Click the key button → press any key |
| Enable/disable | Toggle ON/OFF per profile or use Master switch |

## 🔧 Building from Source
```bash
git clone https://github.com/YOUR_USERNAME/keyboard-macro.git
cd keyboard-macro
./gradlew build
# JAR → build/libs/keyboard-macro-1.0.0.jar
```

## 🚀 GitHub Actions — Auto-Build
Every push to `main`/`master` automatically builds and uploads the JAR as a downloadable artifact.

**To create a release:**
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions will build and publish a full release with download link automatically.

## 📋 Requirements
| | |
|---|---|
| Minecraft | 1.21.4 |
| Loader | Fabric |
| Java | 21 |
| Fabric API | ✅ Required |
| Mod Menu | ⭐ Recommended |

---
*Keyboard Macro — Created by NoTXGameR*
