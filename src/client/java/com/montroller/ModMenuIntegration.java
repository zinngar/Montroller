package com.montroller;

import com.badlogic.gdx.controllers.Controller;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
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
                    .setTitle(Text.of("Montroller Configuration"));

            ConfigCategory generalCategory = builder.getOrCreateCategory(Text.of("General Settings"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            generalCategory.addEntry(entryBuilder.startFloatField(Text.of("Left Stick Deadzone"), config.getLeftStickDeadzone())
                    .setDefaultValue(0.25f)
                    .setSaveConsumer(config::setLeftStickDeadzone)
                    .build());
            generalCategory.addEntry(entryBuilder.startFloatField(Text.of("Right Stick Deadzone"), config.getRightStickDeadzone())
                    .setDefaultValue(0.25f)
                    .setSaveConsumer(config::setRightStickDeadzone)
                    .build());
            generalCategory.addEntry(entryBuilder.startFloatField(Text.of("Right Stick Sensitivity"), config.getRightStickSensitivity())
                    .setDefaultValue(1.0f)
                    .setSaveConsumer(config::setRightStickSensitivity)
                    .build());

            ConfigCategory gyroCategory = builder.getOrCreateCategory(Text.of("Gyro"));
            gyroCategory.addEntry(entryBuilder.startBooleanToggle(Text.of("Enable Gyro"), config.isGyroEnabled())
                    .setDefaultValue(false)
                    .setSaveConsumer(config::setGyroEnabled)
                    .build());
            gyroCategory.addEntry(entryBuilder.startFloatField(Text.of("Gyro Sensitivity"), config.getGyroSensitivity())
                    .setDefaultValue(1.0f)
                    .setSaveConsumer(config::setGyroSensitivity)
                    .build());


			// Controller mappings
			if (ControllerManager.getControllerManager() != null) {
				List<String> keyBindingIds = new ArrayList<>();
				keyBindingIds.add("");
				for (KeyBinding keyBinding : MinecraftClient.getInstance().options.allKeys) {
					keyBindingIds.add(keyBinding.getId());
				}

				for (Controller controller : ControllerManager.getControllerManager().getControllers()) {
					SDL2Controller sdlController = (SDL2Controller) controller;
					ConfigCategory category = builder.getOrCreateCategory(Text.of(sdlController.getName()));

					// We use GameController button names for better UX and consistent mapping
					for (int i = 0; i < SDL.SDL_CONTROLLER_BUTTON_MAX; i++) {
						final String buttonName = SDL.SDL_GameControllerGetStringForButton(i);
						if (buttonName != null && !buttonName.isEmpty()) {
							category.addEntry(entryBuilder.startStringDropdownMenu(Text.of(buttonName.toUpperCase()), config.getMapping(buttonName),
											value -> value.isEmpty() ? Text.of("Unbound") : Text.translatable(value))
									.setSelections(keyBindingIds)
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
