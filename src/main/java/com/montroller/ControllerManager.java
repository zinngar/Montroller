package com.montroller;

import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.enums.XInputButton;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import com.github.strikerx3.jxinput.listener.SimpleXInputDeviceListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    private static final int REFRESH_RATE = 16; // in milliseconds
    private static boolean wasConnected = false;
    private static final Map<String, KeyBinding> keyBindings = new HashMap<>();

    public static void start() {
        populateKeyBindings();
        Config config = Config.getInstance();
        new Thread(() -> {
            while (true) {
                try {
                    XInputDevice[] devices = XInputDevice.getAllDevices();
                    boolean isConnected = devices.length > 0;

                    if (isConnected && !wasConnected) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Controller connected"));
                        });
                        devices[0].addListener(new SimpleXInputDeviceListener() {
                            @Override
                            public void buttonChanged(XInputButton button, boolean pressed) {
                                String action = config.getMapping(button.name());
                                if (action != null && !action.isEmpty()) {
                                    KeyBinding keyBinding = keyBindings.get(action);
                                    if (keyBinding != null) {
                                        MinecraftClient.getInstance().execute(() -> {
                                            keyBinding.setPressed(pressed);
                                        });
                                    }
                                }
                            }
                        });
                    } else if (!isConnected && wasConnected) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Controller disconnected"));
                        });
                    }

                    wasConnected = isConnected;
                    if (isConnected) {
                        devices[0].poll();
                    }
                    Thread.sleep(REFRESH_RATE);
                } catch (XInputNotLoadedException | InterruptedException e) {
                    Montroller.LOGGER.error("Error in controller detection thread", e);
                }
            }
        }).start();
    }

    private static void populateKeyBindings() {
        MinecraftClient client = MinecraftClient.getInstance();
        for (KeyBinding keyBinding : client.options.allKeys) {
            keyBindings.put(keyBinding.getTranslationKey(), keyBinding);
        }
    }
}
