# GeoWarp

A lightweight Paper plugin that lets players warp to **named places** instead of raw coordinates — using a simple geo hierarchy:

`/warp <Country> [City] [Street] [HouseNo]`

Works great for RP servers with countries, cities, landmarks, and addresses.

## Features

* Anchors at multiple levels: **Country → City → Street → Address**
* One command to add anchors at your current position
* Fast tab-completion (drills down by level)
* List browser for existing places
* Simple YAML storage (`plugins/GeoWarp/warps.yml`)
* One-click backup export of the current YAML

## Requirements

* **Paper** 1.21.x (tested on 1.21.8)
* **Java 21+**

## Installation

1. Download or build `GeoWarp-*.jar`.
2. Drop it into your server’s `plugins/` folder.
3. Start the server once to generate data files.

## Commands

```
/warp <Country> [City] [Street] [HouseNo]     # Teleport to an anchor or address
/warp add <Country> [City] [Street] [HouseNo] # Create an anchor/address at your current position
/warp list [Country] [City] [Street]          # Show existing entries at that level
/warp remove <Country> [City] [Street] [No]   # Remove an anchor or address
/warp export                                   # Save a timestamped backup of warps.yml
```

Examples:

```
/warp add GreatBritain
/warp add GreatBritain London
/warp add GreatBritain London MainStreet
/warp add GreatBritain London MainStreet 12
/warp GreatBritain London
/warp list GreatBritain
```

## Permissions

* `warp.use` — use `/warp` and `/warp list` (default: `true`)
* `warp.admin` — `/warp add`, `/warp remove`, `/warp export` (default: `op`)

## Data

* Stored at `plugins/GeoWarp/warps.yml`
* Backups at `plugins/GeoWarp/backups/warps-YYYY-MM-DD_HH-mm-ss.yml`

## Build (Gradle)

```bash
# Windows (PowerShell)
.\gradlew clean build

# Linux/macOS
./gradlew clean build
```

The JAR will be in `build/libs/`.

## Notes

* `/warp add` always uses the **player’s current location and rotation**.
* `Street` and `HouseNo` are optional — teleport works with any level you’ve anchored.

