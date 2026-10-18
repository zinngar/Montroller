# Montroller

A universal controller mod for Minecraft (Fabric).

## Features

*   Support for a wide range of controllers, including Xbox, PlayStation, Nintendo Switch Pro and Steam controllers.
*   Works on every platform Minecraft runs on — Windows, Linux, and macOS on both Apple Silicon and Intel — using the SDL3 library that Minecraft itself ships. No extra native libraries.
*   Left stick to move, right stick to look around, triggers to attack and use.
*   Gyro aiming on controllers that have it (PlayStation, Switch Pro, Steam).
*   Ready to play out of the box with a default layout, and fully remappable in the settings menu (via Mod Menu).
*   Keyboard and mouse keep working alongside the controller.
*   Controller connection and disconnection notifications in chat.

## Default layout

| Input | Action |
|---|---|
| Left stick | Move |
| Right stick | Look |
| Right trigger | Attack / break |
| Left trigger | Use / place |
| A | Jump |
| B | Drop item |
| X | Swap offhand |
| Y | Inventory |
| LB / RB | Previous / next hotbar slot |
| Left stick click | Sprint |
| Right stick click | Sneak |
| D-pad up | Change perspective |
| D-pad down | Chat |
| Back | Player list |
| Start | Pause menu |

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
2.  Download `montroller-1.2.1.jar` from the [Releases](https://github.com/zinngar/Montroller/releases) page.
3.  Put it in your `.minecraft/mods` folder along with Fabric API (and Cloth Config + Mod Menu if you want the settings screen).
4.  Launch the game, plug in a controller, and set up your button mappings under **Mods → Montroller → Configure**.

## Known issues

*   Menus and inventory screens still need the mouse; the controller doesn't move the cursor there yet.

## Building

Requires JDK 25 or newer.

```bash
./gradlew build
```

The jar is written to `build/libs/`.

## Version history

*   **1.2.1** – Fixed a crash on startup when a controller is connected.
*   **1.2.0** – Works on all platforms including Apple Silicon (switched to Minecraft's built-in SDL3). Added triggers, hotbar switching, pause, gyro aiming and a default layout. Fixed keyboard conflicts, button presses not registering for attack/inventory/drop, and a crash when loading saved settings.
*   **1.1.0** – Updated to Minecraft 26.3. Fixed a crash on startup.
*   **1.0.0** – First release, for Minecraft 1.21.10.

## License

This project is licensed under the CC0-1.0 License.
