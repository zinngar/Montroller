package com.montroller;

import org.libsdl.SDL;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    private static final int REFRESH_RATE = 16; // in milliseconds
    private static final Map<String, KeyBinding> keyBindings = new HashMap<>();
    private static SDL2ControllerManager controllerManager;
    private static final Map<String, Boolean> wasConnected = new HashMap<>();

    public static void start() {
        populateKeyBindings();
        Config config = Config.getInstance();
        SDL.SDL_Init(SDL.SDL_INIT_GAMECONTROLLER | SDL.SDL_INIT_SENSOR);
        controllerManager = new SDL2ControllerManager();

        new Thread(() -> {
            while (true) {
                controllerManager.pollState();
                for (SDL2Controller controller : controllerManager.getControllers()) {
                    boolean isConnected = controller.isConnected();
                    String controllerName = controller.getName();

                    if (isConnected && !wasConnected.getOrDefault(controllerName, false)) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(controllerName + " connected"));
                        });
                    } else if (!isConnected && wasConnected.getOrDefault(controllerName, false)) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(controllerName + " disconnected"));
                        });
                    }
                    wasConnected.put(controllerName, isConnected);

                    // Handle button presses
                    for (int i = 0; i < controller.getNumButtons(); i++) {
                        boolean isPressed = controller.getButton(i);
                        String action = config.getMapping(controller.getButtonName(i));
                        if (action != null && !action.isEmpty()) {
                            KeyBinding keyBinding = keyBindings.get(action);
                            if (keyBinding != null) {
                                MinecraftClient.getInstance().execute(() -> {
                                    keyBinding.setPressed(isPressed);
                                });
                            }
                        }
                    }

                    // Handle gyro data
                    if (config.isGyroEnabled()) {
                        handleGyro(controller, config);
                    }
                }

                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    Montroller.LOGGER.error("Error in controller detection thread", e);
                }
            }
        }).start();
    }

    private static void populateKeyBindings() {
        MinecraftClient client = MinecraftClient.getInstance();
        for (KeyBinding keyBinding : client.options.allKeys) {
            keyBindings.put(keyBinding.getId(), keyBinding);
        }
    }

    private static void handleGyro(SDL2Controller controller, Config config) {
        float[] gyroData = new float[3];
        if (controller.getSensorData(gyroData)) {
            float gyroX = gyroData[0];
            float gyroY = gyroData[1];
            float sensitivity = config.getGyroSensitivity();

            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().mouse.onCursorPos(MinecraftClient.getInstance().getWindow().getHandle(),
                        MinecraftClient.getInstance().mouse.getX() + gyroY * sensitivity * 10,
                        MinecraftClient.getInstance().mouse.getY() + gyroX * sensitivity * 10);
            });
        }
    }

    public static SDL2ControllerManager getControllerManager() {
        return controllerManager;
    }
}
