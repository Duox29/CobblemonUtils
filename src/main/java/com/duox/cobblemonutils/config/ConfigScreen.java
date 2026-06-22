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
                // CATEGORY 1 — GENERAL
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .tooltip(Component.literal("Master switch and notification settings."))

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Enable PokeFinder"))
                                .description(OptionDescription.of(Component.literal(
                                        "Master switch. Turn this off to disable the whole mod without losing your other settings.")))
                                .binding(true, () -> config.enablePokeFinder, (v) -> config.enablePokeFinder = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Notifications"))
                                .description(OptionDescription.of(Component.literal(
                                        "Get pinged when something worth catching shows up.")))
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

                // CATEGORY 2 — INDICATORS
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Indicators"))
                        .tooltip(Component.literal("Choose how wild Pokémon are visually highlighted."))

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("World Markers"))
                                .description(OptionDescription.of(Component.literal(
                                        "Visual effects rendered in the world on detected Pokémon.")))
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
                        .build())

                // CATEGORY 3 — FILTERS
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Filters"))
                        .tooltip(Component.literal("Decide which Pokémon actually trigger a highlight."))

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Rarity & Ownership"))
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
                                .description(OptionDescription.of(Component.literal(
                                        "Only highlight these species. Leave empty to match every species.")))
                                .binding(new ArrayList<>(), () -> config.specificSpecies, (v) -> config.specificSpecies = new ArrayList<>(v))
                                .controller(opt -> DropdownStringControllerBuilder.create(opt).values(speciesValues).allowAnyValue(true))
                                .initial("name")
                                .build())
                        .build())

                // CATEGORY 4 — OVERLAY (HUD)
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Overlay"))
                        .tooltip(Component.literal("Heads-up display shown over detected Pokémon."))

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Display Info"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Show Pokemon Info"))
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

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Position"))
                                .description(OptionDescription.of(Component.literal(
                                        "Pixel offset of the overlay from the top-left corner of the screen.")))
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