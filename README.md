# Montroller

A universal controller mod for Minecraft (Fabric).

## Features

*   Support for a wide range of controllers, including Xbox, PlayStation, Nintendo Switch Pro and Steam controllers.
*   Left stick to move, right stick to look around.
*   In-game settings menu (via Mod Menu) for remapping buttons and adjusting stick deadzones and sensitivity.
*   Controller connection and disconnection notifications in chat.

## Requirements

| | Version |
|---|---|
| Minecraft | 26.3 |
| Fabric Loader | 0.19.5 or newer |
| Java | 25 or newer |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.163.0+26.3 |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.3.159 (optional, needed for the settings screen) |
| [Mod Menu](https://modrinth.com/mod/modmenu) | 21.0.0 (optional, needed for the settings screen) |

## Installation

1.  Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.3.
2.  Download `montroller-1.1.0.jar` from the [Releases](https://github.com/zinngar/Montroller/releases) page.
3.  Put it in your `.minecraft/mods` folder along with Fabric API (and Cloth Config + Mod Menu if you want the settings screen).
4.  Launch the game, plug in a controller, and set up your button mappings under **Mods → Montroller → Configure**.

## Known issues

*   **Apple Silicon Macs are not supported yet.** The bundled SDL library (sdl2gdx 1.0.5) only ships an Intel (x86_64) macOS build. Windows and Linux work.

## Building

Requires JDK 25 or newer.

```bash
./gradlew build
```

The jar is written to `build/libs/`.

## Version history

*   **1.1.0** – Updated to Minecraft 26.3. Fixed a crash on startup.
*   **1.0.0** – First release, for Minecraft 1.21.10.

## License

This project is licensed under the CC0-1.0 License.
