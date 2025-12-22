package com.montroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final File configFile = new File("config/montroller.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance;
    private Map<String, String> keyMappings = new HashMap<>();

    private Config() {}

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    private void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Config config = gson.fromJson(reader, Config.class);
                if (config != null) {
                    this.keyMappings = config.keyMappings;
                }
            } catch (IOException e) {
                Montroller.LOGGER.error("Error loading config", e);
            }
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            Montroller.LOGGER.error("Error saving config", e);
        }
    }

    public String getMapping(String button) {
        return keyMappings.getOrDefault(button, "");
    }

    public void setMapping(String button, String action) {
        keyMappings.put(button, action);
    }
}
