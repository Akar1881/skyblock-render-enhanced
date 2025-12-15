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

### Party Slayer Counter 
Track how many slayer bosses you've killed for party members - perfect for slayer carries!

**Two Counting Modes:**
- **Auto Mode**: Automatically detects and counts kills via "LOOT SHARE" messages + allows manual adjustments
- **Manual Mode**: You count everything yourself using commands

**How it works:**
- Enable the counter in settings or with `/sre toggle counter`
- Choose your mode: Auto or Manual (via GUI or `/sre counter mode`)
- In Auto mode: kills are counted automatically when you help kill party members' bosses
- Use `/sre counter add <player>` to manually add kills
- Use `/sre counter remove <player>` to subtract kills (fix mistakes!)
- View counts with `/sre counter` command or the on-screen widget
- Widget position is fully customizable - drag it anywhere with `/sre widget`
- Data is saved to cache and automatically clears when party disbands

**Why you need this:**
- Perfect for slayer carries where you kill bosses for paying customers
- Track exactly how many bosses you've done for each party member
- Manual adjustment for when auto-detection misses or double-counts
- Never lose count even after hundreds of boss kills!

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
| `/sre toggle counter` | Toggle party slayer counter on or off |
| `/sre counter` | Show party slayer kill counts |
| `/sre counter mode` | Toggle between Auto and Manual mode |
| `/sre counter add <player> [amount]` | Add kills for a player (default: 1) |
| `/sre counter remove <player> [amount]` | Remove kills for a player (default: 1) |
| `/sre counter clear [player]` | Clear all counter data, or specific player |
| `/sre widget` | Open widget position editor |
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
