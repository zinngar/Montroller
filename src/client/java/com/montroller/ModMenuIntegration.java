package com.montroller;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = Config.getInstance();
            SDL2ControllerManager controllerManager = ControllerManager.getControllerManager();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("Montroller Settings"))
                    .setSavingRunnable(config::save);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            List<String> keyBindingIds = new ArrayList<>();
            keyBindingIds.add("unbound");
            keyBindingIds.addAll(Arrays.stream(MinecraftClient.getInstance().options.allKeys)
                    .map(KeyBinding::getId)
                    .collect(Collectors.toList()));

            for (SDL2Controller controller : controllerManager.getControllers()) {
                ConfigCategory category = builder.getOrCreateCategory(Text.of(controller.getName()));
                for (int i = 0; i < controller.getNumButtons(); i++) {
                    addButtonMapping(category, entryBuilder, controller.getButtonName(i), config, keyBindingIds.toArray(new String[0]));
                }
            }

            ConfigCategory gyroCategory = builder.getOrCreateCategory(Text.of("Gyro"));
            gyroCategory.addEntry(entryBuilder.startBooleanToggle(Text.of("Enable Gyro"), config.isGyroEnabled())
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> config.setGyroEnabled(newValue))
                    .build());
            gyroCategory.addEntry(entryBuilder.startIntSlider(Text.of("Gyro Sensitivity"), (int) (config.getGyroSensitivity() * 100), 1, 200)
                    .setDefaultValue(100)
                    .setSaveConsumer(newValue -> config.setGyroSensitivity(newValue / 100.0f))
                    .build());

            return builder.build();
        };
    }

    private void addButtonMapping(ConfigCategory category, ConfigEntryBuilder entryBuilder, String button, Config config, String[] keyBindingIds) {
        category.addEntry(entryBuilder.startSelector(Text.of(button + " Button"), keyBindingIds, config.getMapping(button))
                .setDefaultValue("unbound")
                .setSaveConsumer(newValue -> config.setMapping(button, newValue))
                .build());
    }
}
