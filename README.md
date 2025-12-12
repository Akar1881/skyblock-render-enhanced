# Skyblock Render Enhanced (SRE)

[![Modrinth](https://img.shields.io/modrinth/dt/skyblocker-render-enhanced?label=Modrinth%20Downloads&color=00AF5C&logo=modrinth)](https://modrinth.com/mod/skyblocker-render-enhanced)
![Java](https://img.shields.io/badge/Java-21+-red)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-blue)
![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.18.2+-black)
![License](https://img.shields.io/badge/License-GPL%203.0-green)

A lightweight **Fabric mod** for **Hypixel Skyblock** that hides all players except the ones you choose.  
Perfect for **hub drops**, **foraging**, **slayer bosses**, **end lobbies**, or any area where too many players cause visual clutter.

---

## Screenshots

### Player Render OFF  
A crowded hub where all other players are hidden.

![Render Off](pictures/toggleoff.png)

---

### Player Render ON  
Players are visible again after toggling.

![Render On](pictures/toggleon.png)

---

### Configuration GUI  
Clean and simple settings menu powered by YACL (Yet Another Config Lib).

![Config GUI](pictures/configgui.png)

### Slayer Config GUI

![Slayer Config GUI](pictures/configslayergui.png)

### Slayer Normal Mode

![Slayer Normal](pictures/slayernormalmode.png)

### Slayer Highlight Mode

![Slayer Highlighted](pictures/slayerhighlightmode.png)

### Slayer Hide Mode

![Slayer Hidden](pictures/slayerhidemode.png)


---

## Features

### Player Hiding  
Hide all players except those you've added to your allowlist.

### Whitelist System  
Keep specific players **always visible**, even when hiding everyone else.

### Slayer Boss Management
Control visibility of slayer bosses with multiple modes:
- **Off**: Show all slayer bosses (feature disabled)
- **Hide**: Hide other players' slayer bosses, show yours and party/whitelisted players' bosses
- **Glow**: Highlight your, party members', and whitelisted players' slayer bosses with a glow effect

Supported Slayer Bosses:
- Voidgloom Seraph
- Revenant Horror
- Tarantula Broodfather
- Sven Packmaster
- Inferno Demonlord
- Riftstalker Bloodfiend

### Beautiful Config GUI (YACL)  
All settings can be controlled from a clean configuration menu powered by YACL.

### Clean Command System  
Simple, modern `/sre` commands for full control.

### Keybind Support  
Quickly toggle the renderer or open the GUI without typing commands.

### Automatic NPC Detection  
NPCs remain visible, so nothing important disappears.

### Party Member Support  
Automatically render Hypixel party members - perfect for Diana events and slayer parties!

### Lightweight & Skyblock-Friendly  
Runs smoothly even in the busiest lobbies.

---

## Installation

1. Install **Fabric Loader 0.18.2+** for Minecraft 1.21.10  
   https://fabricmc.net/use/installer/

2. Install **Fabric API**  
   https://modrinth.com/mod/fabric-api

3. Download **Skyblock Render Enhanced** from Modrinth:  
   https://modrinth.com/mod/skyblocker-render-enhanced

4. Place the `.jar` file inside:  
   `.minecraft/mods/`

5. Launch Minecraft using the Fabric profile.

---

## Usage

### Commands

| Command | Description |
|--------|-------------|
| `/sre` | Open the config GUI |
| `/sre help` | Show list of commands |
| `/sre toggle player` | Toggle player rendering on/off |
| `/sre toggle slayer` | Cycle slayer mode (Off/Hide/Glow) |
| `/sre whitelist add <player>` | Add player to whitelist |
| `/sre whitelist remove <player>` | Remove player from whitelist |

---

### Keybinds

(Default, customizable under **Controls > Keybinds**)

| Key | Action |
|-----|--------|
| `M` | Open SRE config GUI |
| `V` | Toggle player rendering on/off |
| `B` | Cycle slayer mode |

---

## Building from Source

```bash
git clone https://github.com/akar1881/skyblock-render-enhanced.git
cd skyblock-render-enhanced
./gradlew build
```
The compiled JAR will appear in:
build/libs/

---

## Requirements

- Minecraft 1.21.10
- Fabric Loader 0.18.2+
- Fabric API
- Java 21+

---

## Credits

- Inspired by SRP (Select Player Renderer) by Syfe
- Fully rewritten for Fabric 1.21.10 with YACL
- Developed by akar1881

---

## License

This project is licensed under the GPL 3.0 License.
