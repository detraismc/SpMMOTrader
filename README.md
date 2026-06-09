# SpContentQuests

A Minecraft quest plugin inspired by FTB Quests. Guides players through server content with tiered quests, objectives, rewards, and a custom GUI.

## Features

- Quest categories with their own GUI
- 14+ objective types (break, craft, kill, etc.) + plugin integrations
- Quest requirements (quest completion, permissions, PlaceholderAPI)
- Fully customizable GUI with sounds, titles, and custom items
- Reward system with console/player commands
- SQLite & MySQL database
- Integrations: MMOItems, ItemsAdder, MythicMobs, Slimefun, PlaceholderAPI

## Quick Start

1. Place `SpContentQuests.jar` in `plugins/`
2. Restart server
3. Configure categories in `plugins/SpContentQuests/category/` and quests in `plugins/SpContentQuests/quests/`
4. Use `/questbook` or `/spcontentquests open <category>`

## 📖 [Full Wiki](https://github.com/detraismc/SpContentQuests/wiki)

Detailed documentation for everything — configuration, categories, quests, objective types, commands, integrations, and more.

## Building

Requirements: Java 21+, Maven

```bash
mvn clean package
```

The jar will be in `target/`.

## License

Open-source. Feel free to contribute, fork, or modify.
