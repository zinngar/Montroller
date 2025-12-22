package com.montroller;

import com.github.strikerx3.jxinput.enums.XInputButton;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = Config.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("Montroller Settings"))
                    .setSavingRunnable(config::save);

            ConfigCategory general = builder.getOrCreateCategory(Text.of("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            String[] keyBindingIds = Arrays.stream(MinecraftClient.getInstance().options.allKeys)
                    .map(KeyBinding::getId)
                    .collect(Collectors.toList()).toArray(new String[0]);

            // Add entries for each button
            for (XInputButton button : XInputButton.values()) {
                addButtonMapping(general, entryBuilder, button.name(), config, keyBindingIds);
            }

            return builder.build();
        };
    }

    private void addButtonMapping(ConfigCategory category, ConfigEntryBuilder entryBuilder, String button, Config config, String[] keyBindingIds) {
        category.addEntry(entryBuilder.startSelector(Text.of(button + " Button"), keyBindingIds, config.getMapping(button))
                .setDefaultValue(keyBindingIds[0])
                .setSaveConsumer(newValue -> config.setMapping(button, newValue))
                .build());
    }
}
