# 🐔 JavaBeans - Chicken Farmer Game

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=flat-square)]()
[![License](https://img.shields.io/badge/License-Educational-blue?style=flat-square)](#license)

A 2D farming simulation game built in Java, inspired by Stardew Valley. This project demonstrates Object-Oriented Design, Design Patterns, and Software Architecture principles through incremental development stages.

**Course**: CSSE2002 (Programming in the Large) - University of Queensland  
**Semester**: 2, 2025

## 🎮 Quick Start

### Requirements
- **Java 21** ([Download](https://www.oracle.com/java/technologies/downloads/#java21))
- **IntelliJ IDEA** or any Java IDE
- **Git** (optional, for cloning)

### Setup (IntelliJ)
1. Open project folder in IntelliJ
2. Go to `File → Project Structure → Project`
3. Set SDK to **Java 21**
4. Go to `File → Project Structure → Libraries`
5. Click `+` → Add `lib/engine.jar`
6. Right-click `src/Main.java` → **Run**

The game window will open with a playable character!

## 📖 Overview

**JavaBeans** is a farming game where you control a chicken farmer to:
- 🔨 **Mine ore** to earn coins
- 🌾 **Till soil** and **plant crops** (cabbages)
- 🥬 **Harvest produce** to collect food
- 📦 **Manage inventory** with various tools

### Completed Features (Stages 1-3)
- ✅ Player movement with WASD controls
- ✅ 25×25 tile-based world system with map loading
- ✅ All tile types (Grass, Dirt, Water, OreVein) with proper behaviors
- ✅ Collision detection (no walking on water)
- ✅ Mining ore with Jackhammer tool (earn coins)
- ✅ Tilling soil with Hoe tool
- ✅ Planting cabbages (costs coins)
- ✅ Harvesting crops
- ✅ Complete inventory system (coins, food, items, slots)
- ✅ Inventory UI overlays
- ✅ Game state management with clean architecture
- ✅ Sprite-based rendering with proper Z-order
- ✅ Full interaction system (Usable & Interactable interfaces)
- ✅ NPC character (Brutus)

## 🧠 Development Approach

**No AI Tools Used** 🚫🤖

This entire project (Stages 1-3) was **implemented entirely without any assistance from generative AI tools** such as ChatGPT, GitHub Copilot, or similar services.

- ✅ All code written manually from understanding of specification
- ✅ All design decisions made independently
- ✅ All debugging and problem-solving done through personal analysis
- ✅ No AI code generation, suggestions, or completion tools used

This demonstrates **genuine problem-solving skills**, **deep understanding** of OOP principles, and **authentic learning** from the CSSE2002 curriculum.

---

## 📊 Academic Results

**Overall Grade: Near Perfect** *(Stages 1-3 Only)*

> ⚠️ **Note**: This portfolio includes Stages 1-3 implementation only. **JUnit Tests (Stage 4)** are NOT included in this project submission. The grade evaluation reflects only the game functionality and code quality, excluding test coverage assessment.

| Category | Score            | Details |
|----------|------------------|---------|
| **Functionality** | ~95%             | All game mechanics working flawlessly. Passed nearly all unit tests. Minor deduction due to one edge case logic error in automated tests (non-critical, doesn't affect gameplay) which has been fixed. |
| **Code Style** | 100%             | Full compliance with UQ CSSE2002 style guide. Zero Checkstyle violations. Comprehensive JavaDoc on all public classes and methods. |
| **Software Design** | Excellent        | Clean architecture, 5+ design patterns correctly implemented, proper separation of concerns, extensible for future features. |
| **Gameplay** | Fully Functional | All game features work smoothly without bugs affecting user experience. The logic error only affected academic testing, not actual gameplay. |

**Key Achievement**: Demonstrated ability to implement complex software systems following OOP principles and design patterns under academic constraints. Successfully built a fully functional game from specification with clean, maintainable code.

## 🏗️ Architecture

The project follows **Model-View-Controller (MVC)** pattern:

```
┌─────────────────────────────────────────┐
│    Game Engine (engine.jar)             │
│    Handles rendering, input, timing     │
└────────────────────┬────────────────────┘
                     ↓
┌─────────────────────────────────────────┐
│    JavaBeanFarm (Main Controller)       │
│    Coordinates all game components      │
└──────┬──────────────────┬───────────────┘
       ↓                  ↓
   Player System      World System
   - Position         - Tiles
   - Movement         - Collision
   - Input Handler    - Rendering
```

### Key Components

| Component | Responsibility |
|-----------|-----------------|
| **JavaBeanFarm** | Main game controller, orchestrates tick/render cycle |
| **ChickenFarmer** | Player entity, handles movement and interactions |
| **BeanWorld** | World state, stores and manages tiles |
| **Tile Hierarchy** | Dirt, Grass, Water, OreVein with specific behaviors |
| **GameState** | Data holder connecting all components |
| **TinyInventory** | Manages coins, food, and items |

## 📁 Project Structure

```
src/
├── Main.java                          # Entry point
└── builder/
    ├── JavaBeanFarm.java              # Game controller
    ├── GameState.java / JavaBeanGameState.java
    ├── Tickable.java                  # Updatable interface
    ├── player/
    │   ├── Player.java                # Interface
    │   ├── ChickenFarmer.java         # Implementation
    │   └── PlayerManager.java         # Tick/render manager
    ├── world/
    │   ├── World.java / BeanWorld.java
    │   ├── WorldBuilder.java
    │   └── WorldLoadException.java
    ├── entities/
    │   ├── Brutus.java                # NPC
    │   ├── Interactable.java          # Proximity interaction
    │   ├── Usable.java                # Click interaction
    │   ├── tiles/
    │   │   ├── Tile.java
    │   │   ├── Dirt.java, Grass.java, Water.java, OreVein.java
    │   │   └── TileFactory.java
    │   └── resources/
    │       ├── Cabbage.java
    │       └── Ore.java
    └── inventory/
        ├── Inventory.java / TinyInventory.java
        ├── items/
        │   ├── Item.java
        │   ├── Hoe.java, Jackhammer.java, Bucket.java
        └── ui/
            ├── InventoryOverlay.java
            └── ResourceOverlay.java

resources/
├── uqLogo.map                         # Game world map
└── art/                               # Sprite files

lib/
└── engine.jar                         # Game engine (provided)
```

## 🎯 Implementation Stages

The project is fully implemented through Stage 3. **Stage 4 (JUnit tests) is NOT included in this portfolio submission.**

### **Stage 0** ✅ - Proof of Concept
Display Brutus character walking randomly (pre-implemented)

### **Stage 1** ✅ - Player System
Controllable chicken farmer with WASD movement

**Classes**: `ChickenFarmer`, `PlayerManager`, `JavaBeanGameState`

### **Stage 2** ✅ - World & Tiles  
Tile-based world with collision detection and map loading

**Classes**: `BeanWorld`, `Tile`, `Dirt`, `Grass`, `Water`, `OreVein`, `TileFactory`

### **Stage 3** ✅ - Player Interactions
Full game mechanics: mining, tilling, planting, harvesting

**Classes**: `Cabbage`, `Ore`, Tool implementations, complete `TinyInventory`

### **Stage 4** 📝 - JUnit Tests (EXCLUDED)
Comprehensive unit tests for `TinyInventory` and `BeanWorld`

> **Important**: Stage 4 (writing JUnit tests) is the final component of the original assignment but is **NOT included** in this portfolio project. This submission focuses on demonstrating the complete game implementation (Stages 1-3) with professional code quality and architecture.

## 🧠 Design Patterns Used

| Pattern | Application |
|---------|-------------|
| **MVC** | Separation of model, view, controller |
| **Factory** | `TileFactory` creates appropriate tile types |
| **Strategy** | Different behavior per tile type |
| **Observer** | `Tickable` interface for frame updates |
| **Command** | `Usable`/`Interactable` for player interactions |

## 🎮 Controls

| Key | Action |
|-----|--------|
| **W** | Move up |
| **A** | Move left |
| **S** | Move down |
| **D** | Move right |
| **Mouse Scroll** | Switch held item |
| **Left Click** | Use tool |

## 🧪 Testing

### Stage 4 - JUnit Tests (NOT INCLUDED)

This project does NOT include the JUnit test implementation (Stage 4 of the assignment). 

JUnit tests for `TinyInventory` and `BeanWorld` would typically be written in a separate `test/` folder and would evaluate:

- ✅ Correct implementations work as specified
- ✅ Edge cases handled properly
- ✅ State consistency maintained
- ✅ Faulty implementations can be detected

**Portfolio Focus**: This submission emphasizes the complete game implementation (Stages 1-3) with professional code quality, architecture, and design patterns, rather than test coverage assessment.

## 📚 Learning Outcomes

Implementing this project teaches:

**OOP Concepts**
- Interfaces and inheritance hierarchies
- Encapsulation and access modifiers
- Polymorphism through different tile types

**Design Patterns**
- MVC architecture
- Factory pattern for object creation
- Strategy pattern for behavior variation
- Observer pattern for event handling

**Software Engineering**
- Incremental development (stages)
- Clean code and style conventions
- Comprehensive documentation (JavaDoc)
- Test-driven development principles
- Version control best practices

**Java Proficiency**
- Collections (ArrayList, List)
- Exception handling
- File I/O operations
- Game loop and state management
- Coordinate systems and collision detection

## ⚙️ Technical Details

### Game Loop
```
Each Frame:
1. Update (tick)
   - Read input
   - Move player
   - Update world tiles
2. Render
   - Draw world tiles (background)
   - Draw player
   - Draw UI overlays
3. Display on screen
```

### Coordinate System
- **Pixel-based**: Player and entities use pixel coordinates
- **Tile-based**: World organized in 25×25 grid
- **Conversion**: Engine provides `pixelToTile()` method

### World Map Format
Maps are stored in `.map` files (text-based):
- `G` = Grass tile
- `D` = Dirt tile  
- `W` = Water tile
- `O` = Ore vein tile

## 🔧 Build & Compilation

### Using IDE (Recommended)
- IntelliJ: Right-click `Main.java` → Run
- VS Code: Click "Run" above `main()` method

### Command Line
```bash
# Compile
javac -cp lib/engine.jar -d out src/builder/*.java src/builder/**/*.java

# Run
java -cp out:lib/engine.jar Main
```

## 📝 Code Style

Follows **UQ CSSE2002 Style Guide** (based on Google Java Style):
- Line length: **Max 100 characters**
- Indentation: **4 spaces**
- Naming: `PascalCase` classes, `camelCase` methods
- All public classes/methods documented with **JavaDoc**

## 📋 Requirements Met

✅ Implements classes exactly as specified in JavaDoc  
✅ No changes to public/protected signatures  
✅ Clean architecture with clear separation of concerns  
✅ Design patterns demonstrated effectively  
✅ Comprehensive JavaDoc documentation  
✅ Code style compliant  
✅ Incremental development approach  
✅ Ready for extension to future stages  


## 📖 Documentation

- **JavaDoc**: Comprehensive documentation in all source files
- **Assignment Spec**: Official requirements at https://csse2002.uqcloud.net/assessment/ass1/docs/

## 🤝 Academic Integrity

This is an **educational project** for CSSE2002 at the University of Queensland.

**Allowed**: Learning from code, using as reference, displaying on portfolio  
**Not Allowed**: Submitting as your own work, violating UQ policies

See https://uq.mu/rl553 for UQ Academic Integrity policy.

## ✨ Key Features Highlighted

🎯 **Clean Architecture**
- Clear separation between UI, logic, and data
- Each component has single responsibility
- Easy to test and extend

🧩 **Design Patterns**
- Demonstrates 5+ design patterns in practice
- Shows when and why to use each pattern
- Excellent for interview discussions

📈 **Scalability**
- Can easily add new tile types
- Can extend with new game mechanics
- Foundation for multiplayer or advanced features

🧪 **Testability**
- Designed for comprehensive unit testing
- Mock-friendly architecture
- Clear interfaces and contracts

## 📞 Support

- **Course Staff**: csse2002@uq.edu.au
- **Official Spec**: https://csse2002.uqcloud.net/assessment/ass1/docs/
- **Assignment Sheet**: See `ass1.pdf` in project

## 👤 Author

**Trung Bao Truong** - Student at University of Queensland
CSSE2002 - Programming in the Large (Semester 2, 2025)

- GitHub: [@BrianTr9](https://github.com/BrianTr9)
- University: [University of Queensland](https://www.uq.edu.au)

## 📄 License

**Educational License** - University of Queensland CSSE2002 (Semester 2, 2025)

This project is created for educational purposes as part of the CSSE2002 assignment at the University of Queensland. 

**Usage Terms:**
- ✅ **Allowed**: Portfolio display, GitHub showcase, code review, technical interviews
- ✅ **Allowed**: Learning and studying from the architecture and design patterns
- ✅ **Allowed**: Referencing this as your own completed work (with proper attribution)
- ❌ **Not Allowed**: Copying code to submit as your own for other assignments/courses
- ❌ **Not Allowed**: Violating UQ Academic Integrity policy by plagiarizing this work
- ❌ **Not Allowed**: Commercial use without permission

**Source Code**: This project builds upon the provided game engine (`engine.jar`) from UQ course staff.

For more information, see the [UQ Academic Integrity Policy](https://uq.mu/rl553).

---

<div align="center">

### 🌾 Developed as Portfolio Project for CSSE2002 🌾

Java • OOP • Design Patterns • Game Development

Made with ❤️ for learning and demonstration

</div>
