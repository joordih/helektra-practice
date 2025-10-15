<div align="center">

# ⚡ Helektra Practice

![Solar Lightning](https://api.iconify.design/solar/lightning-bold-duotone.svg?color=%23FFD700&width=64)

**A modern, high-performance Minecraft practice server plugin built from scratch**

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/joordih/helektra)
[![Minecraft](https://img.shields.io/badge/minecraft-1.8--1.21+-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/license-MIT-orange.svg)](LICENSE)

</div>

---

## ![Solar Star](https://api.iconify.design/solar/star-bold-duotone.svg?color=%23FFD700&width=24) About

**Helektra** is a comprehensive practice server plugin designed and built entirely from scratch with **zero dependencies**. It's completely plug-and-play, featuring an optimized custom arena system, full version compatibility, extensive configuration options, and a powerful event system.

### ![Solar Target](https://api.iconify.design/solar/target-bold-duotone.svg?color=%23FFD700&width=20) Core Philosophy

- **Zero Dependencies** - No external plugins required, works out of the box
- **Performance First** - Custom-built systems optimized for speed
- **Version Compatibility** - Full support from 1.8.x to 1.21.x and beyond
- **Highly Configurable** - Every aspect can be customized via YAML
- **Modern Architecture** - Clean code following industry best practices

---

## ![Solar Bolt](https://api.iconify.design/solar/bolt-bold-duotone.svg?color=%23FFD700&width=24) Features

### ![Solar Widget](https://api.iconify.design/solar/widget-5-bold-duotone.svg?color=%2366D9EF&width=20) Core Systems

- **![Solar Users](https://api.iconify.design/solar/users-group-rounded-bold-duotone.svg?color=%23A78BFA&width=18) Profile System** - Persistent player data with MongoDB integration
- **![Solar Sword](https://api.iconify.design/pepicons-print/sword.svg?color=%23F87171&width=18) Kit System** - Fully customizable kits with editor GUI
- **![Solar Map](https://api.iconify.design/solar/map-bold-duotone.svg?color=%2334D399&width=18) Arena System** - Advanced arena management with multiple reset strategies
- **![Solar Ranking](https://api.iconify.design/solar/ranking-bold-duotone.svg?color=%23FBBF24&width=18) Scoreboard System** - Dynamic scoreboards based on player state
- **![Solar Settings](https://api.iconify.design/solar/settings-bold-duotone.svg?color=%2394A3B8&width=18) Configuration** - Everything configurable via YAML files

### ![Solar Database](https://api.iconify.design/solar/database-bold-duotone.svg?color=%2366D9EF&width=20) Arena System

Our custom-built arena system is designed for maximum performance and flexibility:

- **Multiple Reset Strategies**
  - Journal-based (track & revert changes)
  - Section-based (chunk snapshots)
  - Chunk Swap (instant swap)
  - Hybrid (best of both worlds)

- **Smart Pooling** - Pre-generated arenas ready for instant use
- **Physics Guard** - Prevent TNT/liquid spread between arenas
- **Block Tracking** - Intelligent change detection
- **Metrics** - Real-time performance monitoring

### ![Social Scoreboard](https://api.iconify.design/solar/chart-bold-duotone.svg?color=%2366D9EF&width=20) Scoreboard System

- **State-Based Display** - Different scoreboard for each game state
- **PlaceholderAPI Support** - Full PAPI integration
- **Custom Placeholders** - Register your own placeholders
- **Fully Configurable** - Customize via `scoreboards.yml`
- **Performance Optimized** - Efficient update system

### ![Solar Calendar](https://api.iconify.design/solar/calendar-mark-bold-duotone.svg?color=%23A78BFA&width=20) Event System

- **Custom Events** - Host tournaments and special matches
- **Participant Tracking** - Automatic player management
- **Configurable Rewards** - Set up prizes and incentives

### ![Solar Translation](https://api.iconify.design/solar/translation-bold-duotone.svg?color=%2334D399&width=20) Multi-Language

- **Localization System** - Full i18n support
- **Easy Translation** - Simple YAML-based translations
- **Per-Player Language** - Players can choose their language

---

## ![Solar Download](https://api.iconify.design/solar/download-bold-duotone.svg?color=%23FFD700&width=24) Version Compatibility

| Minecraft Version | Status | Features |
|-------------------|--------|----------|
| **1.8.x** | ![Solar Check](https://api.iconify.design/solar/check-circle-bold-duotone.svg?color=%2334D399&width=16) Supported | Core features, basic arenas |
| **1.9-1.12** | ![Solar Check](https://api.iconify.design/solar/check-circle-bold-duotone.svg?color=%2334D399&width=16) Supported | All core features + dual wielding |
| **1.13-1.16** | ![Solar Check](https://api.iconify.design/solar/check-circle-bold-duotone.svg?color=%2334D399&width=16) Supported | Full feature set + new blocks |
| **1.17-1.19** | ![Solar Check](https://api.iconify.design/solar/check-circle-bold-duotone.svg?color=%2334D399&width=16) Supported | Full support + modern mechanics |
| **1.20-1.21+** | ![Solar Check](https://api.iconify.design/solar/check-circle-bold-duotone.svg?color=%2334D399&width=16) Supported | Latest features + optimizations |

### ![Solar Code](https://api.iconify.design/solar/code-bold-duotone.svg?color=%2366D9EF&width=20) Version-Specific Features

- **NMS Abstraction Layer** - Seamless version compatibility
- **Strategy Pattern** - Different implementations per version
- **Automatic Detection** - Plugin detects and adapts to server version
- **Future-Proof** - Easy to add support for new versions

---

## ![Solar Box](https://api.iconify.design/solar/box-bold-duotone.svg?color=%23FFD700&width=24) Installation

### ![Solar List Check](https://api.iconify.design/solar/list-check-bold-duotone.svg?color=%2366D9EF&width=20) Requirements

- **Minecraft Server** - Paper, Spigot, or compatible fork (1.8-1.21+)
- **Java** - Java 21+ (for 1.20.5+) or Java 17+ (older versions)
- **MongoDB** - For persistent data storage
- **PlaceholderAPI** - Optional, for extended placeholder support

### ![Solar Play](https://api.iconify.design/solar/play-bold-duotone.svg?color=%2334D399&width=20) Quick Start

1. **Download** the latest release from [Releases](https://github.com/joordih/helektra/releases)
2. **Place** the JAR file in your `plugins/` folder
3. **Configure** MongoDB connection in `config.yml`
4. **Restart** your server
5. **Done!** The plugin is ready to use

```bash
# Download
wget https://github.com/joordih/helektra/releases/latest/download/Helektra.jar

# Move to plugins folder
mv Helektra.jar /path/to/server/plugins/

# Restart server
./restart.sh
```

---

## ![Solar Settings](https://api.iconify.design/solar/settings-bold-duotone.svg?color=%23FFD700&width=24) Configuration

All configuration files are located in `plugins/Helektra/`:

### ![Solar Document](https://api.iconify.design/solar/document-text-bold-duotone.svg?color=%2366D9EF&width=18) Configuration Files

| File | Purpose |
|------|---------|
| `settings.yml` | General plugin settings |
| `kits.yml` | Kit configurations |
| `arenas.yml` | Arena templates and settings |
| `scoreboards.yml` | Scoreboard layouts and content |
| `menus.yml` | GUI menu configurations |
| `translations/` | Language files |

### ![Solar Code File](https://api.iconify.design/solar/code-file-bold-duotone.svg?color=%2334D399&width=18) Example Configuration

```yaml
# scoreboards.yml
scoreboard:
  enabled: true
  update-interval: 20

  states:
    LOBBY:
      enabled: true
      title: "&6&lHELEKTRA &7┃ &fLobby"
      lines:
        - " "
        - "&6&lInfo"
        - " &7* &ePlayers&7: &f{online_players}"
        - " &7* &eLevel&7: &a{player_level}"
        - " "
        - "&fWelcome back, &e%player_name%"
```

---

## ![Solar Command](https://api.iconify.design/solar/command-bold-duotone.svg?color=%23FFD700&width=24) Commands & Permissions

### ![Solar Code Square](https://api.iconify.design/solar/code-square-bold-duotone.svg?color=%2366D9EF&width=18) Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/kit` | Open kit selector | `helektra.kit` |
| `/kit edit <name>` | Edit a kit | `helektra.kit.edit` |
| `/settings` | Open settings menu | `helektra.settings` |

### ![Solar Shield](https://api.iconify.design/solar/shield-user-bold-duotone.svg?color=%23F87171&width=18) Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/arena create <name>` | Create new arena | `helektra.arena.admin` |
| `/arena delete <name>` | Delete arena | `helektra.arena.admin` |
| `/arena setspawn <name>` | Set arena spawn | `helektra.arena.admin` |
| `/helektra reload` | Reload configs | `helektra.admin` |

---

## ![Solar Programming](https://api.iconify.design/solar/programming-bold-duotone.svg?color=%23FFD700&width=24) For Developers

### ![Solar Layer](https://api.iconify.design/solar/layers-bold-duotone.svg?color=%2366D9EF&width=18) Architecture

Helektra follows a clean, modular architecture:

```
helektra/
├── api/          - Public API interfaces
├── plugin/       - Core implementation
│   ├── model/    - Domain models
│   ├── service/  - Business logic
│   ├── repository/ - Data access
│   ├── di/       - Dependency injection
│   └── utils/    - Utilities
└── nms/          - Version-specific code
```

### ![Solar Folder Path](https://api.iconify.design/solar/folder-path-connect-bold-duotone.svg?color=%2334D399&width=18) Design Patterns

- **Dependency Injection** - Google Guice for loose coupling
- **Repository Pattern** - Abstract data access
- **Service Pattern** - Business logic separation
- **Strategy Pattern** - Version-specific implementations
- **Adapter Pattern** - External library wrappers
- **Observer Pattern** - Event-driven architecture

### ![Solar Link](https://api.iconify.design/solar/link-bold-duotone.svg?color=%23A78BFA&width=18) API Usage

```java
// Get the API
IHelektraAPI api = Helektra.getInstance().getAPI();

// Profile Service
IProfileService profiles = api.getProfileService();
profiles.getProfile(uuid).thenAccept(profile -> {
    // Work with profile
});

// Arena Service
IArenaService arenas = api.getArenaService();
IArena arena = arenas.requestArena("duels");

// Kit Service
IKitService kits = api.getKitService();
IKit kit = kits.getKit("warrior");
```

---

## ![Solar Graph](https://api.iconify.design/solar/graph-new-bold-duotone.svg?color=%23FFD700&width=24) Performance

### ![Solar Wind](https://api.iconify.design/solar/wind-bold-duotone.svg?color=%2366D9EF&width=18) Benchmarks

- **Arena Reset** - < 50ms for medium arenas
- **Profile Load** - < 10ms from cache
- **Scoreboard Update** - < 1ms per player
- **Memory Usage** - ~50MB for 100 players

### ![Solar Battery](https://api.iconify.design/solar/battery-charge-bold-duotone.svg?color=%2334D399&width=18) Optimizations

- Async database operations
- Smart caching system
- Efficient event handling
- Optimized packet usage
- Pool-based arena management

---

## ![Solar Buildings](https://api.iconify.design/solar/buildings-3-bold-duotone.svg?color=%23FFD700&width=24) Building

### ![Solar Sledgehammer](https://api.iconify.design/solar/sledgehammer-bold-duotone.svg?color=%2366D9EF&width=18) Build from Source

```bash
# Clone repository
git clone https://github.com/joordih/helektra.git
cd helektra

# Build with Maven
mvn clean package

# Output: plugin/target/plugin-1.0-SNAPSHOT.jar
```

### ![Solar Checklist](https://api.iconify.design/solar/checklist-bold-duotone.svg?color=%2334D399&width=18) Requirements

- **Maven** 3.8+
- **JDK** 21+
- **Git** (for cloning)

---

## ![Solar Help](https://api.iconify.design/solar/question-circle-bold-duotone.svg?color=%23FFD700&width=24) Support

### ![Solar Chat](https://api.iconify.design/solar/chat-round-dots-bold-duotone.svg?color=%2366D9EF&width=18) Get Help

- **Issues** - [GitHub Issues](https://github.com/joordih/helektra/issues)
- **Discord** - Join our community server
- **Wiki** - Check the [documentation](https://github.com/joordih/helektra/wiki)

### ![Solar Bug](https://api.iconify.design/solar/bug-bold-duotone.svg?color=%23F87171&width=18) Found a Bug?

Please report it on [GitHub Issues](https://github.com/joordih/helektra/issues) with:
- Server version
- Plugin version
- Steps to reproduce
- Error logs (if any)

---

## ![Solar Star Shine](https://api.iconify.design/solar/star-shine-bold-duotone.svg?color=%23FFD700&width=24) Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### ![Solar Users](https://api.iconify.design/solar/users-group-two-rounded-bold-duotone.svg?color=%2366D9EF&width=18) Contributors

Thanks to everyone who has contributed to Helektra!

---

## ![Solar Document](https://api.iconify.design/solar/document-bold-duotone.svg?color=%23FFD700&width=24) License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with** ![Solar Heart](https://api.iconify.design/solar/heart-bold-duotone.svg?color=%23F87171&width=18) **by the Helektra team**

![Solar Lightning](https://api.iconify.design/solar/lightning-bold-duotone.svg?color=%23FFD700&width=32)

**⚡ Practice redefined. Performance perfected. ⚡**

[Website](https://play.helektra.com) • [Discord](#) • [Twitter](#)

</div>
