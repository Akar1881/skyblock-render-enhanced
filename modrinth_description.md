# Skyblock Render Enhanced (SRE)

Skyblock Render Enhanced (SRE) is a lightweight Fabric mod for Hypixel Skyblock that hides all players except the ones you choose. It is designed for crowded areas such as hub lobbies, foraging islands, slayer locations, and end lobbies where too many players can cause visual clutter.

---

## Features

### Player Hiding
Hide all players except those you have added to your allowlist.

### Whitelist System
Keep selected players always visible, even when hiding everyone else.

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

### Config GUI (YACL)
All settings can be adjusted through a clean configuration menu powered by YACL.

### Keybind Support
Toggle the renderer or open the configuration menu through customizable keybinds.

### Automatic NPC Detection
NPCs remain visible so important Skyblock entities are not hidden.

### Party Member Rendering
Hypixel party members are automatically rendered for better coordination during events or boss fights. Party members' slayer bosses are also always visible.

### Lightweight and Efficient
Designed to run smoothly in busy Skyblock environments with optimized caching and minimal performance impact.

---

## Commands

| Command | Description |
|--------|-------------|
| `/sre` | Open the config GUI |
| `/sre help` | Show list of commands |
| `/sre toggle player` | Toggle player rendering on or off |
| `/sre toggle slayer` | Cycle slayer mode (Off/Hide/Glow) |
| `/sre whitelist add <player>` | Add a player to the whitelist |
| `/sre whitelist remove <player>` | Remove a player from the whitelist |

---

## Keybinds

(Default, can be changed in Controls > Keybinds)

| Key | Action |
|-----|--------|
| `M` | Open the SRE config GUI |
| `V` | Toggle player rendering on or off |
| `B` | Cycle slayer mode (Off/Hide/Glow) |

---

## Requirements

- Minecraft 1.21.10  
- Fabric Loader 0.18.2+  
- Fabric API  
- Java 21+
