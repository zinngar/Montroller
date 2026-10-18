package com.montroller;

import com.badlogic.gdx.controllers.Controller;
import com.montroller.mixin.MouseAccessor;
import org.libsdl.SDL;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    private static final int REFRESH_RATE = 16;
    private static final Map<String, KeyMapping> keyMappings = new HashMap<>();
    private static SDL2ControllerManager controllerManager;
    private static final Map<String, Boolean> wasConnected = new HashMap<>();

    private static KeyMapping keyForward;
    private static KeyMapping keyBack;
    private static KeyMapping keyLeft;
    private static KeyMapping keyRight;

    public static void start() {
        populateKeyMappings();
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
							Minecraft.getInstance().execute(() -> {
								Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal(controllerName + " connected"));
							});
						} else if (!isConnected && wasConnected.getOrDefault(controllerName, false)) {
							Minecraft.getInstance().execute(() -> {
								Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal(controllerName + " disconnected"));
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
										KeyMapping keyMapping = keyMappings.get(action);
										if (keyMapping != null) {
											Minecraft.getInstance().execute(() -> {
												keyMapping.setDown(isPressed);
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

    private static void populateKeyMappings() {
        Minecraft client = Minecraft.getInstance();
        for (KeyMapping keyMapping : client.options.keyMappings) {
            keyMappings.put(keyMapping.getName(), keyMapping);
        }
        keyForward = client.options.keyUp;
        keyBack = client.options.keyDown;
        keyLeft = client.options.keyLeft;
        keyRight = client.options.keyRight;
    }

    private static void handleAxes(SDL2Controller controller, Config config) {
        float leftX = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX);
        float leftY = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY);
        float rightX = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTX);
        float rightY = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTY);

        float leftDeadzone = config.getLeftStickDeadzone();
        float rightDeadzone = config.getRightStickDeadzone();

        // Left stick movement
        Minecraft.getInstance().execute(() -> {
            if (Math.abs(leftY) > leftDeadzone) {
                keyForward.setDown(leftY < -leftDeadzone);
                keyBack.setDown(leftY > leftDeadzone);
            } else {
                keyForward.setDown(false);
                keyBack.setDown(false);
            }
            if (Math.abs(leftX) > leftDeadzone) {
                keyLeft.setDown(leftX < -leftDeadzone);
                keyRight.setDown(leftX > leftDeadzone);
            } else {
                keyLeft.setDown(false);
                keyRight.setDown(false);
            }
        });

        // Right stick look
        if (Math.abs(rightX) > rightDeadzone || Math.abs(rightY) > rightDeadzone) {
            float sensitivity = config.getRightStickSensitivity();
            double dx = rightX * sensitivity * 10;
            double dy = rightY * sensitivity * 10;

            Minecraft.getInstance().execute(() -> {
                MouseAccessor mouse = (MouseAccessor) Minecraft.getInstance().mouseHandler;
                // onMove reads the relative dx/dy while the cursor is grabbed (in-game camera)
                // and the absolute x/y in menus, so feed it both.
                mouse.invokeOnMove(Minecraft.getInstance().getWindow().handle(),
                        Minecraft.getInstance().mouseHandler.xpos() + dx,
                        Minecraft.getInstance().mouseHandler.ypos() + dy,
                        dx, dy);
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

				Minecraft.getInstance().execute(() -> {
					MouseAccessor mouse = (MouseAccessor) Minecraft.getInstance().mouseHandler;
					double dx = gyroY * sensitivity * 10;
					double dy = gyroX * sensitivity * 10;
					mouse.invokeOnMove(Minecraft.getInstance().getWindow().handle(),
							Minecraft.getInstance().mouseHandler.xpos() + dx,
							Minecraft.getInstance().mouseHandler.ypos() + dy,
							dx, dy);
				});
			}
		}
		*/
	}

    public static SDL2ControllerManager getControllerManager() {
        return controllerManager;
    }
}
