# 🎒 BackpackPlus

> Virtual per-player storage with permission-tiered sizes and multiple pages.

![Paper](https://img.shields.io/badge/Paper-1.21%2B-2196F3?style=for-the-badge&logo=minecraft&logoColor=white) ![Java](https://img.shields.io/badge/Java-21-E76F00?style=for-the-badge&logo=openjdk&logoColor=white) ![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white) ![License](https://img.shields.io/badge/License-MIT-3DA639?style=for-the-badge)

BackpackPlus gives every player portable storage whose size and page count come from permission tiers. Contents are serialised to Base64, saved on close and flushed periodically, and shrinking a player's row tier never silently eats their items.

## ✨ Features

- **Permission-tiered** rows (`backpackplus.rows.<n>`) and pages (`backpackplus.pages.<n>`)
- Multiple named pages per player
- **No item loss** when a row tier shrinks — overflow is returned or dropped per config
- Material blacklist enforced on both click and drag
- Backpack-inside-backpack nesting prevented via PDC check
- Inventory sorting that merges stacks and compacts empty slots
- Portable ender chest access, including other players' for admins
- Save-on-close plus periodic async flush; offline-owner access for admins

## ⌨️ Commands

| Command | Aliases | Description | Permission |
| --- | --- | --- | --- |
| `/backpack [page] | open <player> [page] | sort | pages | reload` | `bp`, `pack` | Opens your virtual backpack. | `backpackplus.use` |
| `/enderchest [player]` | `ec` | Opens your ender chest or another player's ender chest. | `backpackplus.enderchest` |

## 🔐 Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `backpackplus.use` | Allows opening own backpack. | `true` |
| `backpackplus.admin` | Allows opening other players' backpacks and using reload. | `op` |
| `backpackplus.enderchest` | Allows opening own ender chest via /ec. | `true` |
| `backpackplus.enderchest.others` | Allows opening other players' ender chests. | `op` |
| `backpackplus.rows.1` | Grants a 1-row backpack. | `false` |
| `backpackplus.rows.2` | Grants a 2-row backpack. | `false` |
| `backpackplus.rows.3` | Grants a 3-row backpack. | `true` |
| `backpackplus.rows.4` | Grants a 4-row backpack. | `false` |
| `backpackplus.rows.5` | Grants a 5-row backpack. | `false` |
| `backpackplus.rows.6` | Grants a 6-row backpack. | `op` |
| `backpackplus.pages.1` | Grants access to 1 backpack page. | `true` |
| `backpackplus.pages.2` | Grants access to 2 backpack pages. | `false` |
| `backpackplus.pages.3` | Grants access to 3 backpack pages. | `false` |
| `backpackplus.pages.4` | Grants access to 4 backpack pages. | `false` |
| `backpackplus.pages.5` | Grants access to 5 backpack pages. | `op` |

## ⚙️ Configuration

Everything is configurable in `config.yml`:

- `default-rows`, `max-rows`, `default-pages`, `max-pages`
- `blacklisted-materials`, `drop-overflow`
- `flush-interval-seconds`, view title templates
- a full `messages:` section

## 📦 Installation

1. Download the latest release jar, or build it yourself (see below).
2. No hard dependencies.
3. Drop the jar into your server's `plugins/` folder and restart.

## 🛠️ Building from source

Requires **JDK 21** and **Maven 3.9+**.

```bash
mvn clean package
```

The runnable jar is written to `target/BackpackPlus-1.0.0.jar`.

## 🧱 Architecture

Packages: `backpack` (backpack model with read-write lock, service, custom holder), `storage` (per-player YAML), `util` (Base64 serializer, sorter, messages), `listener`, `command`.

This project targets **Paper 1.21+** and Paper forks (Purpur, Pufferfish). All user-facing text uses the Adventure API (MiniMessage), which is native to Paper. The source is written to a senior standard with clear package separation and no code comments.

## 📄 License

Released under the MIT License. © 2026 CraftForge.
