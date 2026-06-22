package com.duox.cobblemonutils.config;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.duox.cobblemonutils.CobblemonUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen {
    private static List<String> cachedSpecies;

    public Screen createConfigScreen(Screen parent) {
        return new NativeConfigScreen(parent, getSpeciesValues());
    }

    private List<String> getSpeciesValues() {
        if (cachedSpecies != null) return cachedSpecies;

        try {
            TreeSet<String> sorted = new TreeSet<>();
            PokemonSpecies.INSTANCE.getImplemented().forEach(s -> sorted.add(s.getName().toLowerCase(Locale.ROOT)));
            cachedSpecies = new ArrayList<>(sorted);
            return cachedSpecies;
        } catch (Exception e) {
            CobblemonUtils.LOGGER.error("Failed to load Cobblemon species list", e);
            return Collections.emptyList();
        }
    }

    private static final class NativeConfigScreen extends Screen {
        private static final int ROW_H = 24;
        private static final int START_Y = 34;
        private static final int GAP = 18;

        private final Screen parent;
        private final Config config = ConfigManager.getConfig();
        private final List<String> speciesValues;
        private EditBox speciesBox;
        private String speciesPrefix = "";
        private int leftX;
        private int leftControlX;
        private int rightX;
        private int rightControlX;
        private int controlW;
        private int speciesY;
        private int selectedSpeciesY;
        private int selectedSpeciesScroll;

        NativeConfigScreen(Screen parent, List<String> speciesValues) {
            super(Component.literal("CobblemonUtils"));
            this.parent = parent;
            this.speciesValues = speciesValues;
        }

        @Override
        protected void init() {
            int margin = Math.max(24, this.width / 36);
            int columnW = Math.max(260, (this.width - margin * 2 - GAP) / 2);
            int labelW = Math.min(150, columnW / 2);
            controlW = Math.max(96, Math.min(180, columnW - labelW - 12));
            leftX = margin;
            leftControlX = leftX + labelW + 12;
            rightX = leftX + columnW + GAP;
            if (rightX + columnW > this.width - margin) rightX = leftX;
            rightControlX = rightX + labelW + 12;

            int y = START_Y;
            addBool(leftControlX, "Master switch. Disable mod without losing settings.", y, () -> config.enablePokeFinder, v -> config.enablePokeFinder = v); y += ROW_H;
            addBool(leftControlX, "Show notifications for matching Pokémon.", y, () -> config.enableNotifications, v -> config.enableNotifications = v); y += ROW_H;
            addEnum(leftControlX, "Where notifications appear.", y); y += ROW_H + 6;

            addBool(leftControlX, "Glow around matching Pokémon.", y, () -> config.enableGlowing, v -> config.enableGlowing = v); y += ROW_H;
            addBool(leftControlX, "Line from player to matching Pokémon.", y, () -> config.enableTraceRay, v -> config.enableTraceRay = v); y += ROW_H;
            addBool(leftControlX, "Vertical beam over matching Pokémon.", y, () -> config.enableBeaconBeam, v -> config.enableBeaconBeam = v); y += ROW_H + 6;

            addBool(leftControlX, "Ignore species already owned.", y, () -> config.ignoreOwned, v -> config.ignoreOwned = v); y += ROW_H;
            addBool(leftControlX, "Highlight shiny Pokémon.", y, () -> config.highlightShinies, v -> config.highlightShinies = v); y += ROW_H;
            addBool(leftControlX, "Highlight legendary Pokémon.", y, () -> config.highlightLegendaries, v -> config.highlightLegendaries = v); y += ROW_H + 6;

            addBool(leftControlX, "Show overlay info for matching Pokémon.", y, () -> config.showOverworldInfo, v -> config.showOverworldInfo = v); y += ROW_H;
            addBool(leftControlX, "Show IV values in overlay.", y, () -> config.showIVs, v -> config.showIVs = v); y += ROW_H;
            addBool(leftControlX, "Show catch chance in overlay.", y, () -> config.showCatchRate, v -> config.showCatchRate = v); y += ROW_H;
            addInt(leftControlX, "Overlay X Position", "Overlay X offset from left.", y, () -> config.overlayX, v -> config.overlayX = v, 0, 2000); y += ROW_H;
            addInt(leftControlX, "Overlay Y Position", "Overlay Y offset from top.", y, () -> config.overlayY, v -> config.overlayY = v, 0, 1000);

            addSpecies(START_Y);
            addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(this.width - 84, this.height - 28, 60, 20).build());
        }

        private void addBool(int x, String tooltip, int y, Supplier<Boolean> get, Consumer<Boolean> set) {
            Button button = Button.builder(Component.literal(get.get() ? "On" : "Off"), b -> {
                set.accept(!get.get());
                ConfigManager.save();
                b.setMessage(Component.literal(get.get() ? "On" : "Off"));
            }).bounds(x, y, controlW, 20).build();
            button.setTooltip(Tooltip.create(Component.literal(tooltip)));
            addRenderableWidget(button);
        }

        private void addEnum(int x, String tooltip, int y) {
            Button button = Button.builder(Component.literal(config.notificationType.name()), b -> {
                Config.NotificationType[] values = Config.NotificationType.values();
                config.notificationType = values[(config.notificationType.ordinal() + 1) % values.length];
                ConfigManager.save();
                b.setMessage(Component.literal(config.notificationType.name()));
            }).bounds(x, y, controlW, 20).build();
            button.setTooltip(Tooltip.create(Component.literal(tooltip)));
            addRenderableWidget(button);
        }

        private void addInt(int x, String label, String tooltip, int y, Supplier<Integer> get, Consumer<Integer> set, int min, int max) {
            EditBox box = new EditBox(this.font, x, y, controlW, 20, Component.literal(label));
            box.setValue(String.valueOf(get.get()));
            box.setResponder(s -> {
                try {
                    set.accept(Math.max(min, Math.min(max, Integer.parseInt(s))));
                    ConfigManager.save();
                } catch (NumberFormatException ignored) {
                }
            });
            box.setTooltip(Tooltip.create(Component.literal(tooltip)));
            addRenderableWidget(box);
        }

        private void addSpecies(int y) {
            speciesY = y;
            int addW = 45;
            int removeW = 20;
            int fieldW = Math.max(96, controlW - addW - 6);
            speciesBox = new EditBox(this.font, rightControlX, y, fieldW, 20, Component.literal("Species Filter"));
            speciesBox.setValue(speciesPrefix);
            speciesBox.setResponder(s -> speciesPrefix = s.toLowerCase(Locale.ROOT));
            speciesBox.setTooltip(Tooltip.create(Component.literal("Only highlight these species. Empty list matches every species. Press Tab to autocomplete.")));
            addRenderableWidget(speciesBox);

            Button add = Button.builder(Component.literal("Add"), b -> addSpeciesValue()).bounds(rightControlX + fieldW + 6, y, addW, 20).build();
            add.setTooltip(Tooltip.create(Component.literal("Add typed species to filter.")));
            addRenderableWidget(add);

            int shown = 0;
            for (String species : speciesValues) {
                if (speciesPrefix.isEmpty() || !species.startsWith(speciesPrefix) || config.specificSpecies.contains(species)) continue;
                Button suggestion = Button.builder(Component.literal(species), b -> addSpeciesValue(species)).bounds(rightControlX, y + ROW_H * (shown + 1), controlW, 20).build();
                suggestion.setTooltip(Tooltip.create(Component.literal("Add " + species + " to filter.")));
                addRenderableWidget(suggestion);
                if (++shown == 5) break;
            }

            selectedSpeciesY = y + ROW_H * (shown + 2);
            int maxRows = Math.max(1, (this.height - selectedSpeciesY - 70) / ROW_H);
            selectedSpeciesScroll = Math.max(0, Math.min(selectedSpeciesScroll, Math.max(0, config.specificSpecies.size() - maxRows)));
            for (int i = 0; i < Math.min(maxRows, config.specificSpecies.size() - selectedSpeciesScroll); i++) {
                final int index = i + selectedSpeciesScroll;
                int rowY = selectedSpeciesY + ROW_H * i;
                Button item = Button.builder(Component.literal(config.specificSpecies.get(index)), b -> {}).bounds(rightControlX, rowY, controlW - removeW - 6, 20).build();
                item.setTooltip(Tooltip.create(Component.literal(config.specificSpecies.get(index))));
                addRenderableWidget(item);

                Button remove = Button.builder(Component.literal("×"), b -> {
                    if (index < config.specificSpecies.size()) {
                        config.specificSpecies.remove(index);
                        ConfigManager.save();
                        refreshWidgets();
                    }
                }).bounds(rightControlX + controlW - removeW, rowY, removeW, 20).build();
                remove.setTooltip(Tooltip.create(Component.literal("Remove species from filter.")));
                addRenderableWidget(remove);
            }
        }

        private void addSpeciesValue() {
            addSpeciesValue(speciesBox.getValue().trim().toLowerCase(Locale.ROOT));
        }

        private void addSpeciesValue(String value) {
            if (value.isEmpty()) return;
            if (!config.specificSpecies.contains(value)) {
                config.specificSpecies.add(value);
                ConfigManager.save();
            }
            speciesPrefix = "";
            refreshWidgets();
        }

        private void refreshWidgets() {
            clearWidgets();
            init();
        }

        private void refreshSpeciesBox() {
            refreshWidgets();
            if (speciesBox != null) speciesBox.setFocused(true);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean speciesFocused = speciesBox != null && speciesBox.isFocused();
            if (speciesFocused && keyCode == InputConstants.KEY_TAB) {
                String match = firstSpeciesMatch();
                if (match != null) speciesPrefix = match;
                refreshSpeciesBox();
                return true;
            }
            if (speciesFocused && keyCode == InputConstants.KEY_RETURN) {
                addSpeciesValue();
                return true;
            }
            boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
            if (speciesFocused && handled) refreshSpeciesBox();
            return handled;
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            boolean speciesFocused = speciesBox != null && speciesBox.isFocused();
            boolean handled = super.charTyped(codePoint, modifiers);
            if (speciesFocused && handled) refreshSpeciesBox();
            return handled;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (mouseX >= rightControlX && mouseX <= rightControlX + controlW && mouseY >= selectedSpeciesY) {
                selectedSpeciesScroll = Math.max(0, selectedSpeciesScroll + (delta < 0 ? 1 : -1));
                refreshWidgets();
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        private String firstSpeciesMatch() {
            if (speciesPrefix.isEmpty()) return null;
            for (String species : speciesValues) if (species.startsWith(speciesPrefix)) return species;
            return null;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics);
            super.render(graphics, mouseX, mouseY, partialTick);
            graphics.drawString(this.font, this.title, leftX, 12, 0xFFFFFF, false);
            drawLabels(graphics, leftX, START_Y, new String[] {
                    "Enable PokeFinder", "Enable Notifications", "Notification Style", "Outline (Glowing)", "Trace Ray", "Beacon Beam",
                    "Skip Owned Pokémon", "Find Shinies", "Find Legendaries", "Show Pokemon Info", "Show IVs", "Show Catch Rate",
                    "Overlay X Position", "Overlay Y Position"
            });
            graphics.drawString(this.font, "Species Filter", rightX, speciesY + 6, 0xE0E0E0, false);
            graphics.drawString(this.font, "Selected Species", rightX, selectedSpeciesY + 6, 0xE0E0E0, false);
        }

        private void drawLabels(GuiGraphics graphics, int x, int y, String[] labels) {
            for (String label : labels) {
                graphics.drawString(this.font, label, x, y + 6, 0xE0E0E0, false);
                y += ROW_H;
                if (label.equals("Notification Style") || label.equals("Beacon Beam") || label.equals("Find Legendaries")) y += 6;
            }
        }

        @Override
        public void onClose() {
            ConfigManager.save();
            this.minecraft.setScreen(parent);
        }
    }
}
