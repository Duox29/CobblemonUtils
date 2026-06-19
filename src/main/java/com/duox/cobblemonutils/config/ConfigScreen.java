package com.duox.cobblemonutils.config;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ConfigScreen {

    public Screen createConfigScreen(Screen parent) {
        Config config = ConfigManager.getConfig();
        List<String> speciesValues = speciesValues();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("CobblemonUtils"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("PokeFinder"))
                        .tooltip(Component.literal("Highlight wild Pokémon."))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Enable PokeFinder"))
                                .binding(true, () -> config.enablePokeFinder, (v) -> config.enablePokeFinder = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Ignore Owned Pokémon"))
                                .binding(true, () -> config.ignoreOwned, (v) -> config.ignoreOwned = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Highlight Shinies"))
                                .binding(true, () -> config.highlightShinies, (v) -> config.highlightShinies = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Shiny Color"))
                                .binding(new Color(-10496, true), () -> new Color(config.shinyColor, true), (v) -> config.shinyColor = v.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(Component.literal("Species Filter"))
                                .binding(new ArrayList<>(), () -> config.specificSpecies, (v) -> config.specificSpecies = new ArrayList<>(v))
                                .controller(opt -> DropdownStringControllerBuilder.create(opt).values(speciesValues).allowAnyValue(true))
                                .initial("pikachu")
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Overlay"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Overworld Info"))
                                .binding(true, () -> config.showOverworldInfo, (v) -> config.showOverworldInfo = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show IVs"))
                                .binding(true, () -> config.showIVs, (v) -> config.showIVs = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Catch Rate"))
                                .binding(true, () -> config.showCatchRate, (v) -> config.showCatchRate = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())
                        .build())
                .save(ConfigManager::save)
                .build()
                .generateScreen(parent);
    }

    private List<String> speciesValues() {
        try {
            TreeSet<String> sorted = new TreeSet<>();
            PokemonSpecies.INSTANCE.getImplemented().forEach((s) -> sorted.add(s.getName().toLowerCase()));
            return new ArrayList<>(sorted);
        } catch (Throwable var1) {
            return Collections.emptyList();
        }
    }
}