package com.montroller;

import com.badlogic.gdx.controllers.Controller;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.libsdl.SDL;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


			// Controller mappings
			if (ControllerManager.getControllerManager() != null) {
				List<String> keyMappingIds = new ArrayList<>();
				keyMappingIds.add("");
				for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
					keyMappingIds.add(keyMapping.getName());
				}

				for (Controller controller : ControllerManager.getControllerManager().getControllers()) {
					SDL2Controller sdlController = (SDL2Controller) controller;
					ConfigCategory category = builder.getOrCreateCategory(Component.literal(sdlController.getName()));

					// We use GameController button names for better UX and consistent mapping
					for (int i = 0; i < SDL.SDL_CONTROLLER_BUTTON_MAX; i++) {
						final String buttonName = SDL.SDL_GameControllerGetStringForButton(i);
						if (buttonName != null && !buttonName.isEmpty()) {
							category.addEntry(entryBuilder.startStringDropdownMenu(Component.literal(buttonName.toUpperCase()), config.getMapping(buttonName),
											value -> value.isEmpty() ? Component.literal("Unbound") : Component.translatable(value))
									.setSelections(keyMappingIds)
									.setSuggestionMode(false)
									.setSaveConsumer(newValue -> config.setMapping(buttonName, newValue))
									.build());
						}
					}
				}
			}

            builder.setSavingRunnable(config::save);

            return builder.build();
        };
    }
}
