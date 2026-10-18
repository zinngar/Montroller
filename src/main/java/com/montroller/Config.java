package com.montroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static Config instance;
    // Static so Gson neither serialises them nor needs reflective access to java.io.File.
    private static final File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "montroller.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean gyroEnabled = false;
    private float gyroSensitivity = 1.0f;
    private float leftStickDeadzone = 0.25f;
    private float rightStickDeadzone = 0.25f;
    private float rightStickSensitivity = 1.0f;
    private Map<String, String> mappings = defaultMappings();

    // Gson calls this constructor when loading, so it must not load the config itself.
    private Config() {
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    private static Map<String, String> defaultMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("a", "key.jump");
        mappings.put("b", "key.drop");
        mappings.put("x", "key.swapOffhand");
        mappings.put("y", "key.inventory");
        mappings.put("back", "key.playerlist");
        mappings.put("start", "montroller.pause");
        mappings.put("leftstick", "key.sprint");
        mappings.put("rightstick", "key.sneak");
        mappings.put("leftshoulder", "montroller.hotbar_prev");
        mappings.put("rightshoulder", "montroller.hotbar_next");
        mappings.put("dpup", "key.togglePerspective");
        mappings.put("dpdown", "key.chat");
        mappings.put("lefttrigger", "key.use");
        mappings.put("righttrigger", "key.attack");
        return mappings;
    }

    public void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Config loadedConfig = gson.fromJson(reader, Config.class);
                if (loadedConfig != null) {
                    this.gyroEnabled = loadedConfig.gyroEnabled;
                    this.gyroSensitivity = loadedConfig.gyroSensitivity;
                    this.leftStickDeadzone = loadedConfig.leftStickDeadzone;
                    this.rightStickDeadzone = loadedConfig.rightStickDeadzone;
                    this.rightStickSensitivity = loadedConfig.rightStickSensitivity;
                    if (loadedConfig.mappings != null && !loadedConfig.mappings.isEmpty()) {
                        this.mappings = loadedConfig.mappings;
                    }
                }
            } catch (IOException e) {
                Montroller.LOGGER.error("Failed to load Montroller config", e);
            }
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            Montroller.LOGGER.error("Failed to save Montroller config", e);
        }
    }

    public boolean isGyroEnabled() {
        return gyroEnabled;
    }

    public void setGyroEnabled(boolean gyroEnabled) {
        this.gyroEnabled = gyroEnabled;
    }

    public float getGyroSensitivity() {
        return gyroSensitivity;
    }

    public void setGyroSensitivity(float gyroSensitivity) {
        this.gyroSensitivity = gyroSensitivity;
    }

    public float getLeftStickDeadzone() {
        return leftStickDeadzone;
    }

    public void setLeftStickDeadzone(float leftStickDeadzone) {
        this.leftStickDeadzone = leftStickDeadzone;
    }

    public float getRightStickDeadzone() {
        return rightStickDeadzone;
    }

    public void setRightStickDeadzone(float rightStickDeadzone) {
        this.rightStickDeadzone = rightStickDeadzone;
    }

    public float getRightStickSensitivity() {
        return rightStickSensitivity;
    }

    public void setRightStickSensitivity(float rightStickSensitivity) {
        this.rightStickSensitivity = rightStickSensitivity;
    }

    public String getMapping(String buttonName) {
        return mappings.getOrDefault(buttonName, "");
    }

    public void setMapping(String buttonName, String action) {
        mappings.put(buttonName, action);
    }
}
