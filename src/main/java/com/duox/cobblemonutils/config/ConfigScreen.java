package com.duox.cobblemonutils.config;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ConfigScreen {

    private static List<String> cachedSpecies = null;

    public Screen createConfigScreen(Screen parent) {
        Config config = ConfigManager.getConfig();
        List<String> speciesValues = getSpeciesValues();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("CobblemonUtils"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("PokeFinder"))
                        .tooltip(Component.literal("Highlight wild Pokémon."))

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Highlight Modes"))
                                .description(OptionDescription.of(Component.literal("Way to navigate pokemon.")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Enable PokeFinder"))
                                        .binding(true, () -> config.enablePokeFinder, (v) -> config.enablePokeFinder = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Outline (Glowing)"))
                                        .binding(true, () -> config.enableGlowing, (v) -> config.enableGlowing = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Trace Ray"))
                                        .binding(false, () -> config.enableTraceRay, (v) -> config.enableTraceRay = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Beacon Beam"))
                                        .binding(false, () -> config.enableBeaconBeam, (v) -> config.enableBeaconBeam = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Filters"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Skip owned Pokémon"))
                                        .binding(true, () -> config.ignoreOwned, (v) -> config.ignoreOwned = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Find Shinies"))
                                        .binding(true, () -> config.highlightShinies, (v) -> config.highlightShinies = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Find Legendaries"))
                                        .binding(true, () -> config.highlightLegendaries, (v) -> config.highlightLegendaries = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .build())

                        .group(ListOption.<String>createBuilder()
                                .name(Component.literal("Species Filter"))
                                .description(OptionDescription.of(Component.literal("Add Pokemon name.")))
                                .binding(new ArrayList<>(), () -> config.specificSpecies, (v) -> config.specificSpecies = new ArrayList<>(v))
                                .controller(opt -> DropdownStringControllerBuilder.create(opt).values(speciesValues).allowAnyValue(true))
                                .initial("name")
                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Notifications"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Enable Notifications"))
                                        .binding(true, () -> config.enableNotifications, (v) -> config.enableNotifications = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Config.NotificationType>createBuilder()
                                        .name(Component.literal("Notification Style"))
                                        .binding(Config.NotificationType.ACTION_BAR, () -> config.notificationType, (v) -> config.notificationType = v)
                                        .controller(opt -> EnumControllerBuilder.<Config.NotificationType>create(opt).enumClass(Config.NotificationType.class))
                                        .build())
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
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Overlay X Position"))
                                .binding(10, () -> config.overlayX, (v) -> config.overlayX = v)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0).max(2000))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Overlay Y Position"))
                                .binding(10, () -> config.overlayY, (v) -> config.overlayY = v)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0).max(1000))
                                .build())
                        .build())
                .save(ConfigManager::save)
                .build()
                .generateScreen(parent);
    }

    private List<String> getSpeciesValues() {
        if (cachedSpecies != null) return cachedSpecies;

        try {
            TreeSet<String> sorted = new TreeSet<>();
            PokemonSpecies.INSTANCE.getImplemented().forEach((s) -> sorted.add(s.getName().toLowerCase()));
            cachedSpecies = new ArrayList<>(sorted);
            return cachedSpecies;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}