# SpMMOTrader

A GUI-based trading plugin for MMOItems. Players can browse paginated shops and purchase items using money (Vault) and/or MMOItems as currency.

## Features

- Fully customizable GUI shops via YAML configuration
- Paginated item browsing with next/previous buttons
- Pay with money (Vault economy) and/or MMOItems
- Display MMOItems directly in the shop with their lore
- Custom clickable items in the GUI (e.g., go back, open other menus)
- All sounds, messages, and cooldown configurable in `config.yml`
- Auto-detect modules — just drop a `.yml` file in the `module/` folder

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/openmmotrader <module> [player]` | `mmotrader.admin` | Open a trader module |
| `/mmotraderreload` | `mmotrader.admin` | Reload all configurations |

## Configuration

Place module files in `plugins/SpMMOTrader/module/`. Each file defines a shop with items, costs, and GUI layout.

## Dependencies

- **MMOItems** (soft) — required for MMOItems item display and currency
- **Vault** (soft) — required for economy (money) payments

## Building

Requirements: Java 21+, Maven

```bash
mvn clean package
```
