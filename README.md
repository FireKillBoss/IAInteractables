# IAInteractables

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.5--1.21.4-brightgreen" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java Version">
  <img src="https://img.shields.io/badge/Platform-Paper-blue" alt="Platform">
</p>

Custom furnaces and workbenches plugin for Paper 1.20.5+ with ItemsAdder integration

## ✨ Features

### 🔥 Custom Furnaces
- Arbitrary recipe creation
- Multiple ingredient slots
- Progress bar visualization
- Custom cooking time
- Particle effects
- Sound effects

### 🔨 Custom Workbenches
- Flexible slot layout
- Drag & drop crafting

### 🎨 Advanced Customization
- Custom GUI structure and size (up to 54 slots)
- Custom filler items
- HEX colors support
- Legacy color codes support

## 📦 Requirements

- **Server**: [Paper](https://papermc.io/) 1.20.5 or higher
- **Java**: 21 or higher
- **Dependencies**:
  - [PlaceholderAPI](https://www.spigotmc.org/resources/6245/)
  - [ItemsAdder](https://www.spigotmc.org/resources/73355/)

## 🚀 Installation

1. Download the latest release from [Releases](../../releases)
2. Place `IAInteractables.jar` into `plugins/` folder
3. Install dependencies (PlaceholderAPI, ItemsAdder)
4. Restart server
5. Configure furnaces/workbenches in `plugins/IAInteractables/`

## ⚙️ Configuration

### Furnace Example (`furnaces/blast_furnace.yml`)

```yaml
title: "&6Blast Furnace"
itemsadder-furniture: "namespace:blast_furnace"

structure:
  - "X X X X X X X X X"
  - "X X A X B X X X X"
  - "X X X X X X R X X"
  - "X X X F X X X X X"
  - "X X X X X X X X X"

filler:
  material: BLACK_STAINED_GLASS_PANE
  name: ""

recipes:
  steel:
    cook-time: 400
    result:
      material: ia-steel_ingot
      amount: 1
    raws:
      A: [IRON_INGOT, ia-iron_ore]
      B: [COAL, CHARCOAL]
    fuels:
      F: [COAL, LAVA_BUCKET]

effects:
  particles:
    on-start:
      particle: FLAME
      count: 20
      offset-x: 0.5
      offset-y: 0.5
      offset-z: 0.5
      speed: 0.05
    on-complete:
      particle: TOTEM_OF_UNDYING
      count: 30
  
  sounds:
    on-start:
      sound: block.furnace.fire_crackle
      volume: 1.0
      pitch: 1.0

  cooking-interval: 60
```

### Workbench Example (`workbenches/default.yml`)

```yaml
title: "&bAdvanced Crafting Table"

structure:
  - "X X X X X X X X X"
  - "X 0 1 2 X X X R X"
  - "X 3 4 5 X X X X X"
  - "X 6 7 8 X X X X X"

filler:
  material: GRAY_STAINED_GLASS_PANE

recipes:
  diamond_sword:
    result:
      material: DIAMOND_SWORD
      amount: 1
    0:
      material: DIAMOND
      amount: 1
    1:
      material: DIAMOND
      amount: 1
    4:
      material: STICK
      amount: 1
```
