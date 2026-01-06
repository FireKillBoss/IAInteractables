# IAInteractables

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.5+-brightgreen" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java Version">
  <img src="https://img.shields.io/badge/Platform-Paper-blue" alt="Platform">
  <img src="https://img.shields.io/badge/Platform-Spigot-yellow" alt="Platform">
</p>

Custom furnaces and workbenches plugin for Paper and Spigot for 1.20.5+ with ItemsAdder integration

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
- Particle effects
- Sound effects
  
### 🎨 Advanced Customization
- Custom GUI structure and size (up to 54 slots)
- Custom filler items
- HEX colors support
- Legacy color codes support
- Progress bar details
- Recipe book for furnaces and workbenches

## 📦 Requirements

- **Server**: [Paper](https://papermc.io/) or [Spigot](https://www.spigotmc.org/) 1.20.5 or higher
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
title: "&dCustom furnace"
itemsadder-furniture: "myitems:custom_furnace" #Also rename default.yml to [custom_furnace] for it to work!
structure:
  - "X X X X X X X X X"
  - "X X X X X X X X X"
  - "X A B X P X R X X"
  - "X X U X X X X X X"
  - "X X X X X X X X X"

filler:
  material: BLACK_STAINED_GLASS_PANE
  name: ""
  lore: []

progress-bar:
  stages: 10  #There are 3 custom stages: 5, 10 and 20. You can delete everything in progress-bar and leave only stages: 5 because plugin has automatic preset for 5 stages, if you want 10 or 20 stages, then you will have to set up them yourself.
  0:
    material: RED_CONCRETE
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  1:
    material: RED_TERRACOTTA
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  2:
    material: ORANGE_TERRACOTTA
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  3:
    material: YELLOW_TERRACOTTA
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  4:
    material: YELLOW_CONCRETE
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  5:
    material: LIME_TERRACOTTA
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  6:
    material: LIME_CONCRETE
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  7:
    material: EMERALD_BLOCK
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  8:
    material: GREEN_TERRACOTTA
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"
  9:
    material: GREEN_CONCRETE
    name: "&cProgress: %progress%"
    lore:
      - "&7%ticks% / %total_ticks% ticks"
      - "&7%seconds_left% seconds left"

effects:
  cooking-interval: 20
  on-start:
    sound:
      sound: BLOCK_FURNACE_FIRE_CRACKLE
      category: BLOCKS
      volume: 1.0
      pitch: 1.0
    particle:
      particle: FLAME
      count: 15
      offset-x: 0.3
      offset-y: 0.3
      offset-z: 0.3
      speed: 0.05
  on-cooking:
    sound:
      sound: BLOCK_LAVA_POP
      category: BLOCKS
      volume: 0.5
      pitch: 1.5
    particle:
      particle: SMOKE_NORMAL
      count: 5
      offset-x: 0.2
      offset-y: 0.4
      offset-z: 0.2
      speed: 0.02
  on-complete:
    sound:
      sound: BLOCK_ANVIL_LAND
      category: BLOCKS
      volume: 0.8
      pitch: 1.8
    particle:
      particle: VILLAGER_HAPPY
      count: 20
      offset-x: 0.5
      offset-y: 0.5
      offset-z: 0.5
      speed: 0.1

recipes:
  1z:
    cook-time: 100
    result:
      material: IRON_BLOCK
      amount: 1
    raws:
      A: [IRON_ORE]
      B: [DEEPSLATE]
    fuels:
      U: [COAL]

# All placeholders for progress-bar: %progress%, %ticks%, %total_ticks%, %seconds_left%
```

### Workbench Example (`workbenches/default.yml`)

```yaml
title: "&dCustom workbench"
itemsadder-furniture: "myitems:custom_workbench" #Also rename default.yml to [custom_workbench] for it to work!
structure:
  - "O X X X O X X X X"
  - "X X O X X X X X X"
  - "X O O O X X X R X"
  - "X X O X X X X X X"
  - "O X X X O X X X X"

filler:
  material: BLACK_STAINED_GLASS_PANE
  name: ""
  lore: []

effects:
  on-craft:
    sound:
      sound: ENTITY_EXPERIENCE_ORB_PICKUP
      category: PLAYERS
      volume: 1.0
      pitch: 1.2
    particle:
      particle: ENCHANTMENT_TABLE
      count: 30
      offset-x: 0.5
      offset-y: 1.0
      offset-z: 0.5
      speed: 0.5

recipes:
  first:
    result:
      material: IRON_BLOCK
      amount: 1
    0:
      material: STONE
      amount: 1
    1:
      material: STONE
      amount: 1
    2:
      material: STONE
      amount: 1
    3:
      material: STONE
      amount: 1
    4:
      material: STONE
      amount: 1
    5:
      material: STONE
      amount: 1
    6:
      material: STONE
      amount: 1
    7:
      material: STONE
      amount: 1
    8:
      material: STONE
      amount: 1
```

### Basic config example (`config.yml`)
```
no-perm: "&cNo rights!"
reload: "&aConfigs succesfully reloaded!"
usages:
  - "&f/iai reload <name> - reload configs"
recipe-book:
  enabled: true
  item: "KNOWLEDGE_BOOK"
  name: "&6Recipe book"
  gui-title: "&8Recipe book"
  hidden-stations:
    - "secret_workbench"
    - "admin_furnace"
```
