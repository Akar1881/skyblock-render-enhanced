# Skyblock Render Enhanced (SRE)

[![Modrinth](https://img.shields.io/modrinth/dt/skyblocker-render-enhanced?label=Modrinth%20Downloads&color=00AF5C&logo=modrinth)](https://modrinth.com/mod/skyblocker-render-enhanced)
![Java](https://img.shields.io/badge/Java-21+-red)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-blue)
![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.18.2+-black)
![License](https://img.shields.io/badge/License-GPL%203.0-green)

A lightweight **Fabric mod** for **Hypixel Skyblock** that hides all players except the ones you choose.  
Perfect for **hub drops**, **foraging**, **slayer bosses**, **end lobbies**, or any area where too many players cause visual clutter.

---

## ✨ Features

### ✔ Player Hiding  
Hide all players except those you've added to your allowlist.

### ✔ Whitelist System  
Keep specific players **always visible**, even when hiding everyone else.

### ✔ Beautiful Config GUI (Cloth Config)  
All settings can be controlled from a clean configuration menu.

### ✔ Clean Command System  
Simple, modern `/sre` commands for full control.

### ✔ Keybind Support  
Quickly toggle the renderer or open the GUI without typing commands.

### ✔ Automatic NPC Detection  
NPCs remain visible, so nothing important disappears.

### ✔ Lightweight & Skyblock-Friendly  
Runs smoothly even in the busiest lobbies.

---

## 📥 Installation

1. Install **Fabric Loader 0.18.2+** for Minecraft 1.21.10  
   https://fabricmc.net/use/installer/

2. Install **Fabric API**  
   https://modrinth.com/mod/fabric-api

3. Download **Skyblock Render Enhanced** from Modrinth:  
   ➜ https://modrinth.com/mod/skyblocker-render-enhanced

4. Place the `.jar` file inside:  
   `.minecraft/mods/`

5. Launch Minecraft using the Fabric profile.

---

## 🧭 Usage

### Commands

| Command | Description |
|--------|-------------|
| `/sre` | Open the config GUI |
| `/sre help` | Show list of commands |
| `/sre toggle` | Toggle the mod on/off |
| `/sre whitelist list` | List whitelisted players |
| `/sre whitelist add <player>` | Add player to whitelist |
| `/sre whitelist remove <player>` | Remove player from whitelist |

---

### Keybinds

(Default, customizable under **Controls → Keybinds**)

| Key | Action |
|-----|--------|
| `M` | Open SRE config GUI |
| `V` | Toggle mod on/off |

---

## 🛠 Building from Source

```bash
git clone https://github.com/akar1881/skyblock-render-enhanced.git
cd skyblock-render-enhanced
./gradlew build
```

The compiled JAR will appear in:

```
build/libs/
```

---

## 📦 Requirements

- Minecraft **1.21.10**
- Fabric Loader **0.18.2+**
- Fabric API
- Cloth Config
- Java **21+**

---

## 🏆 Credits

- Inspired by **SRP (Select Player Renderer)** by Syfe  
- Fully rewritten for Fabric 1.21.10 with Cloth Config  
- Developed by **akar1881**

---

## 📄 License

This project is licensed under the **GPL 3.0** License.