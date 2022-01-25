# ![Experience Economy](https://imgur.com/zSnzCKX.png)

An accurate experience point-based economy system for PaperMC 1.18.1+!

[![Maven](https://github.com/satyrnidae/xpeconomy/actions/workflows/maven.yml/badge.svg)](https://github.com/satyrnidae/xpeconomy/actions/workflows/maven.yml)
[![Downloads](https://cf.way2muchnoise.eu/full_565106_downloads.svg)](https://dev.bukkit.org/projects/experience-economy)

## Introduction

No more fiat currencies!

Tired of currencies that you have to use commands to check? Want your players to be able to get richer just by playing the game? Experience Economy might be for you!

### Key Features

- Supports offline player accounts
- Choose between points, levels, and per-hundred points economy modes
- Reflects experience spent at the Enchantment Table and Anvil
- Can save accounts as YAML or in a MySQL database
- Set a minimum starting balance for new players
- Options to manage experience bottles
- Robust command set for use and moderation

### Notes for Use

- Fractional currency is only supported for the PER_HUNDRED economy type. LEVELS economy cannot support fractional currency due to scaling
- Requires [Vault](https://dev.bukkit.org/projects/vault)

### Metrics
This plugin uses bStats to collect some analytics about your current system setup and plugin configuration. Metrics collection is optional. If you installed a previous version of the plugin, you are already opted-out of metrics collection. If you want to opt-out on version 1.3.0 or higher, set the "metrics" option in the configuration file to "false", or comment or remove the line. Thanks!

## Configuration

<details>

```yaml
# Database Settings: Set this to connect to a MySQL database for account storage.
# If Enabled is set to false, then file storage will be used instead.
mysql:
  # Whether the MySQL server backend should be enabled.
  # Defaults to false.
  enabled: false
  # The MySQL server IP or hostname.
  # Defaults to "localhost".
  hostname: localhost
  # The MySQL server port.
  # Defaults to 3306.
  port: 3306
  # The name of the database to use.
  # Defaults to "spigot".
  database: spigot
  # The MySQL user ID.
  # Defaults to "root".
  userID: root
  # The MySQL user password.
  # Defaults to "password".
  password: password
  # Options for the MySQL connection. Represented as key-value pairs.
  flags:
    - allowReconnect: false
      useSSL: false
  # Optional prefix for any created table's names.
  # Useful if you are only using a single database for multiple plugins.
  # Defaults to "xpeco".
  tablePrefix: xpeco
# The initial account balance for new player accounts.
# Defaults to zero.
startingBalance: 0
# The locale to use while translating chat messages.
# Defaults to "en_US".
locale: en_US
# The economy method to use.
# Defaults to "POINTS". Valid values are "POINTS", "LEVELS", and "PER_HUNDRED" (case insensitive)
economyMethod: points
# Experience Bottle options
experienceBottleOptions:
  # Whether experience bottle management should be enabled.
  # Defaults to false.
  enabled: false
  # A block material that will allow the user to fill experience bottles if they right-click it with an empty bottle.
  # List of valid values: https://papermc.io/javadocs/paper/1.18/org/bukkit/Material.html#enum-constant-summary
  # Defaults to air. If set to air, this functionality will be disabled.
  fillInteractBlock: enchanting_table
  # Whether bottles should never be thrown or not.
  # Players can always opt to use XP bottles instead of throwing them by sneaking.
  # Defaults to true.
  throwBottles: true
  # The default number of experience points per bottle.
  # Defaults to 10 points.
  pointsPerBottle: 7
  # Refunds thrown bottles
  # Defaults to false
  refundThrownBottles: false
# Whether to show debug output in the console.
# Defaults to false.
debug: false
```
</details>

## Permissions

<details>

### Permission Groups

- `xpeconomy.*`
  - Grants access to all xpeconomy permissions.
  - Default: false
- `xpeconomy.balance.*`
  - Grants access to all balance permissions.
  - Default: false
- `xpeconomy.balance.add.*`
  - Grants access to all balance adding permissions.
  - Default: false
- `xpeconomy.balance.deduct.*`
  - Grants a user all balance deducting permissions
  - Default: false
- `xpeconomy.balance.set.*`
  - Grants all set balance permissions
  - Default: false
- `xpeconomy.balance.sync.*`
  - Grants a user all sync permissions
  - Default: op
- `xpeconomy.balance.transfer.*`
  - Grants a user all transfer permissions
  - Default: false
- `xpeconomy.exempt.all`
  - Exempts a user from all commands which alter or query another user's account
  - Default: false
- `xpeconomy.exempt.bypass.all`
  - Allows a user to bypass all exemptions
  - Default: false
- `xpeconomy.experience.*`
  - Grants a user all experience subpermissions
  - Default: false
 
### Individual Permissions

- `xpeconomy.balance`
  - Grants the ability to check account balances
  - Default: true
- `xpeconomy.balance.add`
  - Grants the ability to add more XP to an account
  - Default: op
- `xpeconomy.balance.add.exempt`
  - Prevents the use of the add command on players or groups with this permission enabled
  - Default: false
- `xpeconomy.balance.add.exempt.bypass`
  - Allows a player or group to use the add command on a player or group member with the xpeconomy.balance.add.exempt permission
  - Default: false
- `xpeconomy.balance.add.others`
  - Allows a user to add a specific balance to another user's account
  - Default: op
- `xpeconomy.balance.exempt`
  - Prevents a user's account balance from being queried
  - Default: false
- `xpeconomy.balance.exempt.bypass`
  - Allows a user to bypass another user's exempt status for the balance command
  - Default: false
- `xpeconomy.balance.others`
  - Allows a user to check another user's account balance
  - Default: op
- `xpeconomy.balance.deduct`
  - Allows a player to remove an amount from an account
  - Default: op
- `xpeconomy.balance.deduct.exempt`
  - Prevents a user's account from being altered by the remove command
  - Default: false
- `xpeconomy.balance.deduct.exempt.bypass`
  - Allows a user to bypass another user's exemption status for the remove command
  - Default: false
- `xpeconomy.balance.deduct.others`
  - Allows a user to remove an amount from another user's account
  - Default: op 
- `xpeconomy.balance.set`
  - Allows a user to set an account's balance
  - Default: op
- `xpeconomy.balance.set.exempt`
  - Exempts a user from the set balance command
  - Default: false
- `xpeconomy.balance.set.exempt.bypass`
  - Allows a user to bypass another user's exempt status for the set balance command    
  - Default: false
- `xpeconomy.balance.set.others`
  - Allows a user to set another user's account balance
  - Default: op
- `xpeconomy.balance.sync`
  - Allows a user to sync an account's balance with the player's XP score
  - Default: op
- `xpeconomy.balance.sync.others`
  - Allows a user to sync another user's balance
  - Default: op
- `xpeconomy.balance.transfer`
  - Allows a user to force a transfer between two accounts
  - Default: op
- `xpeconomy.balance.transfer.exempt`
  - Prevents a user's account from being transferred from
  - Default: false
- `xpeconomy.balance.transfer.exempt.bypass`
  - Allows a user to transfer money from exempt accounts
  - Default: false
- `xpeconomy.experience`
  - Allows a user to query experience point counts
  - Default: true
- `xpeconomy.experience.exempt`
  - Prevents a user's experience points from being queried by the experience command
  - Default: false
- `xpeconomy.experience.exempt.bypass`
  - Allows a user to bypass another user's exempt status when
  - Default: false
- `xpeconomy.experience.others`
  - Allows a user to query other users' experience
  - Default: op
- `xpeconomy.pay`
  - Allows a user to pay other users
  - Default: true
- `xpeconomy.reload`
  - Allows a user to reload the plugin's configuration file.
  - Default: op
- `xpeconomy.bottle.fill`
  - Allows a user to fill glass bottles with experience
  - Default: true
- `xpeconomy.bottle.use`
  - Allows a user to use a Bottle o' Enchanting without throwing it
  - Default: true
- `xpeconomy.bottle.refund`
  - Allows a user to collect glass bottles from thrown Bottles o' Enchanting
  - Default: true

</details>

## Commands

<details>

- `/add`
  - Adds an amount to a player's balance
  - Usage: `/add AMOUNT [PLAYER]`
  - Aliases:
    - addbalance
    - addbal
- `/balance`
  - Displays a user's account balance
  - Usage: `/balance [PLAYER]`
  - Aliases:
    - bal
- `/deduct`
  - Deducts a given amount from a player's balance
  - Usage: `/deduct AMOUNT [PLAYER]`
  - Aliases:
    - remove
    - removebal
    - removebalance
    - deductbal
    - deductbalance
- `/experience`
  - Queries a player's experience balance
  - Usage: `/experience [PLAYER]`
  - Aliases:
    - exp
    - xp
- `/pay`
  - Pays another player an amount from the sender's balance. Cannot be executed via console
  - Usage: `/pay PLAYER AMOUNT`
- `/setbalance`
  - Sets a player's account balance to a desired value.
  - Usage: `/setbalance AMOUNT [PLAYER]`
  - Aliases:
    - setbal
- `syncxp`
  - Synchronizes a player's account balance with their XP level
  - Usage: `/syncxp [PLAYER]`
- `transfer`
  - Transfers balances from one player to another.
  - Usage: `/transfer AMOUNT PLAYER RECIPIENT`
    - aliases
      - xfer
- `xpeconomy`
  - Performs various tasks for the xpeconomy plugin
  - Usage: `/xpeconomy {about|add|balance|deduct|experience|help|pay|reload|set|sync|transfer} [ARGS...]`
  - Aliases:
    - xpe
    - xpeco

</details>
