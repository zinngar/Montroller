package com.montroller;

import com.badlogic.gdx.controllers.Controller;
import com.montroller.mixin.MouseAccessor;
import org.libsdl.SDL;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    private static final int REFRESH_RATE = 16;
    private static final Map<String, KeyBinding> keyBindings = new HashMap<>();
    private static SDL2ControllerManager controllerManager;
    private static final Map<String, Boolean> wasConnected = new HashMap<>();

    private static KeyBinding keyForward;
    private static KeyBinding keyBack;
    private static KeyBinding keyLeft;
    private static KeyBinding keyRight;

    public static void start() {
        populateKeyBindings();
        Config config = Config.getInstance();
        SDL.SDL_SetHint("SDL_JOYSTICK_HIDAPI_STEAM", "1");
        SDL.SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_INPUT", "1");
        SDL.SDL_Init(SDL.SDL_INIT_GAMECONTROLLER | SDL.SDL_INIT_SENSOR);
        controllerManager = new SDL2ControllerManager();

        new Thread(() -> {
            while (true) {
				try {
					controllerManager.pollState();
					for (Controller controller : controllerManager.getControllers()) {
						SDL2Controller sdlController = (SDL2Controller) controller;
						boolean isConnected = sdlController.isConnected();
						String controllerName = sdlController.getName();

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

						if (isConnected) {
							// Handle button presses
							// We iterate over GameController buttons to match the configuration UI
							for (int i = 0; i < SDL.SDL_CONTROLLER_BUTTON_MAX; i++) {
								try {
									boolean isPressed = sdlController.getButton(i);
									String buttonName = SDL.SDL_GameControllerGetStringForButton(i);
									if (buttonName == null || buttonName.isEmpty()) continue;

									String action = config.getMapping(buttonName);
									if (action != null && !action.isEmpty()) {
										KeyBinding keyBinding = keyBindings.get(action);
										if (keyBinding != null) {
											MinecraftClient.getInstance().execute(() -> {
												keyBinding.setPressed(isPressed);
											});
										}
									}
								} catch (Exception e) {
									// Ignore buttons that are not supported by the controller
								}
							}

							// Handle axes
							handleAxes(sdlController, config);

							// Handle gyro
							if (config.isGyroEnabled()) {
								handleGyro(sdlController, config);
							}
						}
					}
				} catch (Exception e) {
					Montroller.LOGGER.error("Error polling controllers", e);
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
        keyForward = client.options.forwardKey;
        keyBack = client.options.backKey;
        keyLeft = client.options.leftKey;
        keyRight = client.options.rightKey;
    }

    private static void handleAxes(SDL2Controller controller, Config config) {
        float leftX = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX);
        float leftY = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY);
        float rightX = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTX);
        float rightY = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTY);

        float leftDeadzone = config.getLeftStickDeadzone();
        float rightDeadzone = config.getRightStickDeadzone();

        // Left stick movement
        MinecraftClient.getInstance().execute(() -> {
            if (Math.abs(leftY) > leftDeadzone) {
                keyForward.setPressed(leftY < -leftDeadzone);
                keyBack.setPressed(leftY > leftDeadzone);
            } else {
                keyForward.setPressed(false);
                keyBack.setPressed(false);
            }
            if (Math.abs(leftX) > leftDeadzone) {
                keyLeft.setPressed(leftX < -leftDeadzone);
                keyRight.setPressed(leftX > leftDeadzone);
            } else {
                keyLeft.setPressed(false);
                keyRight.setPressed(false);
            }
        });

        // Right stick look
        if (Math.abs(rightX) > rightDeadzone || Math.abs(rightY) > rightDeadzone) {
            float sensitivity = config.getRightStickSensitivity();
            double dx = rightX * sensitivity * 10;
            double dy = rightY * sensitivity * 10;

            MinecraftClient.getInstance().execute(() -> {
                MouseAccessor mouse = (MouseAccessor) MinecraftClient.getInstance().mouse;
                mouse.invokeOnCursorPos(MinecraftClient.getInstance().getWindow().getHandle(),
                        MinecraftClient.getInstance().mouse.getX() + dx,
                        MinecraftClient.getInstance().mouse.getY() + dy);
            });
        }
    }

	private static void handleGyro(SDL2Controller controller, Config config) {
		// Gyro support via SDL_GameControllerGetSensor is currently unavailable
		// because the sdl2gdx:1.0.5 library lacks the necessary JNI bindings.
		/*
		long sensor = SDL.SDL_GameControllerGetSensor(controller.joystick.getGameController(), SDL.SDL_SENSOR_GYRO);
		if (sensor != 0) {
			SDL.SDL_SensorSetEnabled(sensor, true);
			float[] gyroData = new float[3];
			if (SDL.SDL_SensorGetData(sensor, gyroData, 3) == 0) {
				float gyroX = gyroData[0];
				float gyroY = gyroData[1];
				float sensitivity = config.getGyroSensitivity();

				MinecraftClient.getInstance().execute(() -> {
					MouseAccessor mouse = (MouseAccessor) MinecraftClient.getInstance().mouse;
					mouse.invokeOnCursorPos(MinecraftClient.getInstance().getWindow().getHandle(),
							MinecraftClient.getInstance().mouse.getX() + gyroY * sensitivity * 10,
							MinecraftClient.getInstance().mouse.getY() + gyroX * sensitivity * 10);
				});
			}
		}
		*/
	}

    public static SDL2ControllerManager getControllerManager() {
        return controllerManager;
    }
}
