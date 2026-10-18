package com.montroller;

import com.montroller.mixin.KeyMappingAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLSensor;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads gamepads through the SDL3 that Minecraft itself ships (via LWJGL), so it runs on
 * every platform the game does. All SDL calls happen on the render thread, which is the
 * thread Minecraft pumps SDL events on.
 */
public final class ControllerManager {
    public static final String HOTBAR_NEXT = "montroller.hotbar_next";
    public static final String HOTBAR_PREV = "montroller.hotbar_prev";
    public static final String PAUSE = "montroller.pause";
    public static final List<String> VIRTUAL_ACTIONS = List.of(HOTBAR_NEXT, HOTBAR_PREV, PAUSE);

    public static final String LEFT_TRIGGER = "lefttrigger";
    public static final String RIGHT_TRIGGER = "righttrigger";

    private static final float AXIS_MAX = 32767f;
    private static final float TRIGGER_THRESHOLD = 0.5f;
    /** Mouse pixels per second at full right-stick deflection and sensitivity 1. */
    private static final double STICK_LOOK_SPEED = 625;
    /** Mouse pixels per radian of controller rotation; about 1:1 at default mouse sensitivity. */
    private static final double GYRO_PIXELS_PER_RADIAN = 380;

    private static final Map<Integer, Long> gamepads = new LinkedHashMap<>(); // joystick id -> SDL_Gamepad*
    private static final Map<String, KeyMapping> keyMappings = new HashMap<>();
    private static Set<String> activeActions = new HashSet<>();
    private static boolean initialized;
    private static long lastLookNanos;

    private ControllerManager() {}

    public static void start() {
        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_JOYSTICK_HIDAPI_STEAM, "1");
        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
        if (!SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_GAMEPAD | SDLInit.SDL_INIT_SENSOR)) {
            Montroller.LOGGER.error("Failed to initialise SDL gamepad support");
            return;
        }
        initialized = true;
        Montroller.LOGGER.info("Controller support started");

        for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
            keyMappings.put(keyMapping.getName(), keyMapping);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    /** Names of the buttons and triggers that can be mapped, in display order. */
    public static List<String> inputNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < SDLGamepad.SDL_GAMEPAD_BUTTON_COUNT; i++) {
            String name = SDLGamepad.SDL_GetGamepadStringForButton(i);
            if (name != null && !name.isEmpty()) names.add(name);
        }
        names.add(LEFT_TRIGGER);
        names.add(RIGHT_TRIGGER);
        return names;
    }

    public static List<String> connectedNames() {
        List<String> names = new ArrayList<>();
        for (long gamepad : gamepads.values()) {
            names.add(nameOf(gamepad));
        }
        return names;
    }

    private static void tick() {
        if (!initialized) return;
        refreshGamepads();

        Config config = Config.getInstance();
        Minecraft client = Minecraft.getInstance();
        Set<String> nowActive = new HashSet<>();

        for (long gamepad : gamepads.values()) {
            SDLGamepad.SDL_SetGamepadSensorEnabled(gamepad, SDLSensor.SDL_SENSOR_GYRO, config.isGyroEnabled());

            for (int i = 0; i < SDLGamepad.SDL_GAMEPAD_BUTTON_COUNT; i++) {
                if (SDLGamepad.SDL_GetGamepadButton(gamepad, i)) {
                    addMapped(nowActive, config, SDLGamepad.SDL_GetGamepadStringForButton(i));
                }
            }
            if (axis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER) > TRIGGER_THRESHOLD) {
                addMapped(nowActive, config, LEFT_TRIGGER);
            }
            if (axis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER) > TRIGGER_THRESHOLD) {
                addMapped(nowActive, config, RIGHT_TRIGGER);
            }

            float leftX = axis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX);
            float leftY = axis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY);
            float deadzone = config.getLeftStickDeadzone();
            if (leftY < -deadzone) nowActive.add(client.options.keyUp.getName());
            if (leftY > deadzone) nowActive.add(client.options.keyDown.getName());
            if (leftX < -deadzone) nowActive.add(client.options.keyLeft.getName());
            if (leftX > deadzone) nowActive.add(client.options.keyRight.getName());
        }

        // Only touch key mappings on a change, so the keyboard keeps working alongside the controller.
        for (String action : nowActive) {
            if (!activeActions.contains(action)) press(client, action);
        }
        for (String action : activeActions) {
            if (!nowActive.contains(action)) {
                KeyMapping keyMapping = keyMappings.get(action);
                if (keyMapping != null) keyMapping.setDown(false);
            }
        }
        activeActions = nowActive;
    }

    private static void addMapped(Set<String> actions, Config config, String inputName) {
        if (inputName == null) return;
        String action = config.getMapping(inputName);
        if (!action.isEmpty()) actions.add(action);
    }

    private static void press(Minecraft client, String action) {
        switch (action) {
            case HOTBAR_NEXT -> scrollHotbar(client, 1);
            case HOTBAR_PREV -> scrollHotbar(client, -1);
            case PAUSE -> {
                if (client.gui.screen() == null) client.pauseGame(false);
            }
            default -> {
                KeyMapping keyMapping = keyMappings.get(action);
                if (keyMapping != null) {
                    keyMapping.setDown(true);
                    // setDown alone doesn't register a click, which attack, inventory, drop etc. rely on.
                    KeyMappingAccessor accessor = (KeyMappingAccessor) keyMapping;
                    accessor.setClickCount(accessor.getClickCount() + 1);
                }
            }
        }
    }

    private static void scrollHotbar(Minecraft client, int direction) {
        if (client.player == null || client.gui.screen() != null) return;
        int slot = Math.floorMod(client.player.getInventory().getSelectedSlot() + direction, 9);
        client.player.getInventory().setSelectedSlot(slot);
    }

    private static void refreshGamepads() {
        Set<Integer> present = new HashSet<>();
        IntBuffer ids = SDLGamepad.SDL_GetGamepads();
        if (ids != null) {
            try {
                while (ids.hasRemaining()) present.add(ids.get());
            } finally {
                SDLStdinc.SDL_free(ids);
            }
        }

        gamepads.entrySet().removeIf(entry -> {
            if (present.contains(entry.getKey()) && SDLGamepad.SDL_GamepadConnected(entry.getValue())) return false;
            announce(nameOf(entry.getValue()) + " disconnected");
            SDLGamepad.SDL_CloseGamepad(entry.getValue());
            return true;
        });

        for (int id : present) {
            if (gamepads.containsKey(id)) continue;
            long gamepad = SDLGamepad.SDL_OpenGamepad(id);
            if (gamepad == 0) continue;
            gamepads.put(id, gamepad);
            announce(nameOf(gamepad) + " connected");
        }
    }

    /**
     * Camera movement for this frame, as mouse pixels. Called every frame from the mouse handler
     * so looking stays smooth at any frame rate.
     */
    public static double[] consumeLook() {
        long now = System.nanoTime();
        double seconds = lastLookNanos == 0 ? 0 : Math.min((now - lastLookNanos) / 1e9, 0.1);
        lastLookNanos = now;
        double dx = 0;
        double dy = 0;
        if (!initialized || seconds == 0) return new double[] {dx, dy};

        Config config = Config.getInstance();
        for (long gamepad : gamepads.values()) {
            float x = axis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX);
            float y = axis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY);
            double magnitude = Math.hypot(x, y);
            float deadzone = config.getRightStickDeadzone();
            if (magnitude > deadzone) {
                double scale = (Math.min(magnitude, 1) - deadzone) / (1 - deadzone) / magnitude;
                double speed = STICK_LOOK_SPEED * config.getRightStickSensitivity() * seconds;
                dx += x * scale * speed;
                dy += y * scale * speed;
            }

            if (config.isGyroEnabled() && SDLGamepad.SDL_GamepadHasSensor(gamepad, SDLSensor.SDL_SENSOR_GYRO)) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer gyro = stack.mallocFloat(3); // pitch, yaw, roll in rad/s
                    if (SDLGamepad.SDL_GetGamepadSensorData(gamepad, SDLSensor.SDL_SENSOR_GYRO, gyro)) {
                        double speed = GYRO_PIXELS_PER_RADIAN * config.getGyroSensitivity() * seconds;
                        dx -= gyro.get(1) * speed;
                        dy -= gyro.get(0) * speed;
                    }
                }
            }
        }
        return new double[] {dx, dy};
    }

    private static float axis(long gamepad, int axis) {
        return SDLGamepad.SDL_GetGamepadAxis(gamepad, axis) / AXIS_MAX;
    }

    private static String nameOf(long gamepad) {
        String name = SDLGamepad.SDL_GetGamepadName(gamepad);
        return name == null || name.isEmpty() ? "Controller" : name;
    }

    private static void announce(String message) {
        Montroller.LOGGER.info(message);
        Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal(message));
    }
}
