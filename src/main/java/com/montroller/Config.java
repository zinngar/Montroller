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
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean gyroEnabled = false;
    private float gyroSensitivity = 1.0f;
    private float leftStickDeadzone = 0.25f;
    private float rightStickDeadzone = 0.25f;
    private float rightStickSensitivity = 1.0f;
    private Map<String, String> mappings = new HashMap<>();

    private Config() {
        this.configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "montroller.json");
        load();
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
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
                    if (loadedConfig.mappings != null) {
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
