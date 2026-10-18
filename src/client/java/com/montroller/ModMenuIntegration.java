package com.montroller;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = Config.getInstance();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Montroller Configuration"));

            ConfigCategory generalCategory = builder.getOrCreateCategory(Component.literal("General Settings"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            generalCategory.addEntry(entryBuilder.startFloatField(Component.literal("Left Stick Deadzone"), config.getLeftStickDeadzone())
                    .setDefaultValue(0.25f)
                    .setSaveConsumer(config::setLeftStickDeadzone)
                    .build());
            generalCategory.addEntry(entryBuilder.startFloatField(Component.literal("Right Stick Deadzone"), config.getRightStickDeadzone())
                    .setDefaultValue(0.25f)
                    .setSaveConsumer(config::setRightStickDeadzone)
                    .build());
            generalCategory.addEntry(entryBuilder.startFloatField(Component.literal("Right Stick Sensitivity"), config.getRightStickSensitivity())
                    .setDefaultValue(1.0f)
                    .setSaveConsumer(config::setRightStickSensitivity)
                    .build());

            ConfigCategory gyroCategory = builder.getOrCreateCategory(Component.literal("Gyro"));
            gyroCategory.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Gyro"), config.isGyroEnabled())
                    .setDefaultValue(false)
                    .setSaveConsumer(config::setGyroEnabled)
                    .build());
            gyroCategory.addEntry(entryBuilder.startFloatField(Component.literal("Gyro Sensitivity"), config.getGyroSensitivity())
                    .setDefaultValue(1.0f)
                    .setSaveConsumer(config::setGyroSensitivity)
                    .build());


            List<String> connected = ControllerManager.connectedNames();
            generalCategory.addEntry(entryBuilder.startTextDescription(Component.literal(connected.isEmpty()
                    ? "No controller connected"
                    : "Connected: " + String.join(", ", connected))).build());

            // Mappings are shared by every controller, keyed by SDL's button name.
            List<String> actions = new ArrayList<>();
            actions.add("");
            actions.addAll(ControllerManager.VIRTUAL_ACTIONS);
            for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
                actions.add(keyMapping.getName());
            }

            ConfigCategory buttonsCategory = builder.getOrCreateCategory(Component.literal("Buttons"));
            for (String inputName : ControllerManager.inputNames()) {
                buttonsCategory.addEntry(entryBuilder.startStringDropdownMenu(Component.literal(inputName.toUpperCase()), config.getMapping(inputName),
                                value -> value.isEmpty() ? Component.literal("Unbound") : Component.translatable(value))
                        .setSelections(actions)
                        .setSuggestionMode(false)
                        .setSaveConsumer(newValue -> config.setMapping(inputName, newValue))
                        .build());
            }

            builder.setSavingRunnable(config::save);

            return builder.build();
        };
    }
}
