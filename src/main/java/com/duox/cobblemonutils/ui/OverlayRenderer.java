package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.CobblemonUtils;
import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.duox.cobblemonutils.utils.CatchRateCalculator;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OverlayRenderer {
    private static Pokemon currentTarget = null;

    private static final int COLOR_BG = 0xAA1E1E24;
    private static final int COLOR_BORDER = 0x55FFFFFF;
    private static final int COLOR_TEXT_TITLE = 0xFFFFFF;
    private static final int COLOR_TEXT_SUB = 0xAAAAAA;

    // Cache for findWildPokemonEntity: avoid re-scanning every tick when battle unchanged
    private static Pokemon cachedBattlePokemon = null;
    private static java.util.UUID cachedBattleId = null;

    // Cache for type weakness: avoid recomputing every frame when target unchanged
    private static Pokemon cachedWeaknessTarget = null;
    private static List<TypeInfo> cachedW4x = List.of();
    private static List<TypeInfo> cachedW2x = List.of();

    // --- TYPE CHART DATA ---
    private record TypeInfo(String name, String shortName, int color, ResourceLocation icon) {}

    private static final TypeInfo[] TYPE_DATA = {
            new TypeInfo("NORMAL", "NOR", 0xFFFFFF, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/normal.png")),
            new TypeInfo("FIGHTING", "FIG", 0xAA0000, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/fighting.png")),
            new TypeInfo("FLYING", "FLY", 0xAAAAFF, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/flying.png")),
            new TypeInfo("POISON", "POI", 0xAA55AA, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/poison.png")),
            new TypeInfo("GROUND", "GRO", 0xFFAA55, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/ground.png")),
            new TypeInfo("ROCK", "ROC", 0xAAAA55, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/rock.png")),
            new TypeInfo("BUG", "BUG", 0xAAFF55, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/bug.png")),
            new TypeInfo("GHOST", "GHO", 0x5500AA, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/ghost.png")),
            new TypeInfo("STEEL", "STE", 0xAAAAAA, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/steel.png")),
            new TypeInfo("FIRE", "FIR", 0xFF5555, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/fire.png")),
            new TypeInfo("WATER", "WAT", 0x5555FF, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/water.png")),
            new TypeInfo("GRASS", "GRA", 0x55FF55, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/grass.png")),
            new TypeInfo("ELECTRIC", "ELE", 0xFFFF55, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/electric.png")),
            new TypeInfo("PSYCHIC", "PSY", 0xFF55AA, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/psychic.png")),
            new TypeInfo("ICE", "ICE", 0x55FFFF, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/ice.png")),
            new TypeInfo("DRAGON", "DRA", 0x5555AA, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/dragon.png")),
            new TypeInfo("DARK", "DAR", 0x555555, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/dark.png")),
            new TypeInfo("FAIRY", "FAI", 0xFF55FF, ResourceLocation.fromNamespaceAndPath("cobblemonutils", "textures/gui/types/fairy.png"))
    };

    private static final Map<String, Integer> TYPE_ID_MAP = buildTypeIdMap();

    private static Map<String, Integer> buildTypeIdMap() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < TYPE_DATA.length; i++) {
            map.put(TYPE_DATA[i].name(), i);
        }
        return Map.copyOf(map);
    }

    private static final double[][] TYPE_CHART = {
            {1,1,1,1,1,0.5,1,0,0.5,1,1,1,1,1,1,1,1,1}, // NORMAL
            {2,1,0.5,0.5,1,2,0.5,0,2,1,1,1,1,0.5,2,1,2,0.5}, // FIGHTING
            {1,2,1,1,1,0.5,2,1,0.5,1,1,2,0.5,1,1,1,1,1}, // FLYING
            {1,1,1,0.5,0.5,0.5,1,0.5,0,1,1,2,1,1,1,1,1,2}, // POISON
            {1,1,0,2,1,2,0.5,1,2,2,1,0.5,2,1,1,1,1,1}, // GROUND
            {1,0.5,2,1,0.5,1,2,1,0.5,2,1,1,1,1,2,1,1,1}, // ROCK
            {1,0.5,0.5,0.5,1,1,1,0.5,0.5,0.5,1,2,1,2,1,1,2,0.5}, // BUG
            {0,1,1,1,1,1,1,2,1,1,1,1,1,2,1,1,0.5,1}, // GHOST
            {1,1,1,1,1,2,1,1,0.5,0.5,0.5,1,0.5,1,2,1,1,2}, // STEEL
            {1,1,1,1,1,0.5,2,1,2,0.5,0.5,2,1,1,2,0.5,1,1}, // FIRE
            {1,1,1,1,2,2,1,1,1,2,0.5,0.5,1,1,1,0.5,1,1}, // WATER
            {1,1,0.5,0.5,2,2,0.5,1,0.5,0.5,2,0.5,1,1,1,0.5,1,1}, // GRASS
            {1,1,2,1,0,1,1,1,1,1,2,0.5,0.5,1,1,0.5,1,1}, // ELECTRIC
            {1,2,1,2,1,1,1,1,0.5,1,1,1,1,0.5,1,1,0,1}, // PSYCHIC
            {1,1,2,1,2,1,1,1,0.5,0.5,0.5,2,1,1,0.5,2,1,1}, // ICE
            {1,1,1,1,1,1,1,1,0.5,1,1,1,1,1,1,2,1,0}, // DRAGON
            {1,0.5,1,1,1,1,1,2,1,1,1,1,1,2,1,1,0.5,0.5}, // DARK
            {1,2,1,0.5,1,1,1,1,0.5,0.5,1,1,1,1,1,2,2,1}  // FAIRY
    };

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        currentTarget = resolveTarget(client, battle);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            render(event.getGuiGraphics());
        }
    }

    private static void render(GuiGraphics g) {
        Config config = ConfigManager.getConfig();
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.level == null || !config.showOverworldInfo || currentTarget == null) {
            return;
        }

        Font font = client.font;
        int padding = 8;
        int width = 170;

        List<TypeInfo> w4x;
        List<TypeInfo> w2x;

        if (config.showTypes) {
            if (currentTarget == cachedWeaknessTarget) {
                w4x = cachedW4x;
                w2x = cachedW2x;
            } else {
                cachedWeaknessTarget = currentTarget;
                cachedW4x = new ArrayList<>();
                cachedW2x = new ArrayList<>();
                w4x = cachedW4x;
                w2x = cachedW2x;

                ElementalType type1 = currentTarget.getPrimaryType();
                ElementalType type2 = currentTarget.getSecondaryType();
                int id1 = getTypeId(type1.getName().toUpperCase(Locale.ROOT));
                int id2 = (type2 != null) ? getTypeId(type2.getName().toUpperCase(Locale.ROOT)) : -1;

                if (id1 != -1) {
                    for (int i = 0; i < 18; i++) {
                        double dmg = TYPE_CHART[i][id1];
                        if (id2 != -1) dmg *= TYPE_CHART[i][id2];

                        if (dmg == 4.0) cachedW4x.add(TYPE_DATA[i]);
                        else if (dmg == 2.0) cachedW2x.add(TYPE_DATA[i]);
                    }
                }
            }
        } else {
            w4x = List.of();
            w2x = List.of();
        }

        int height = padding * 2 + 12; // Header
        height += 16; // HP Bar

        if (config.showTypes) {
            height += 14; // Type icon
            if (!w4x.isEmpty()) height += 14; // 4x icons
            if (!w2x.isEmpty()) height += 14; // 2x icons
            if (w4x.isEmpty() && w2x.isEmpty()) height += 14; // None
            height += 2;
        }

        if (config.showIVs) height += 38;
        if (config.showCatchRate) height += 14;

        int x = config.overlayX;
        int y = config.overlayY;

        // Panel
        g.fill(x, y, x + width, y + height, COLOR_BG);
        drawBorder(g, x, y, width, height, COLOR_BORDER);

        int currentY = y + padding;

        currentY = drawHeader(g, font, currentTarget, x + padding, currentY, width - padding * 2);
        currentY = drawHPBar(g, font, currentTarget, x + padding, currentY, width - padding * 2);

        if (config.showTypes) {
            currentY = drawElementsAndWeakness(g, font, currentTarget, w4x, w2x, x + padding, currentY, width - padding * 2);
        }
        if (config.showIVs) {
            currentY = drawStatsGrid(g, font, "IVs", currentTarget.getIvs().getOrDefault(Stats.HP), currentTarget.getIvs().getOrDefault(Stats.ATTACK), currentTarget.getIvs().getOrDefault(Stats.DEFENCE), currentTarget.getIvs().getOrDefault(Stats.SPECIAL_ATTACK), currentTarget.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE), currentTarget.getIvs().getOrDefault(Stats.SPEED), x + padding, currentY, width - padding * 2);        }
        if (config.showCatchRate) {
            drawCatchRate(g, client, font, currentTarget, x + padding, currentY, width - padding * 2);
        }
    }

    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int drawHeader(GuiGraphics g, Font font, Pokemon target, int x, int y, int width) {
        String name = target.getSpecies().getName();
        int level = target.getLevel();
        boolean shiny = target.getShiny();

        var gender = target.getGender();
        String genderIcon = switch (gender) { case MALE -> "♂"; case FEMALE -> "♀"; default -> "⚥"; };
        int genderColor = switch (gender) { case MALE -> 0x55AAFF; case FEMALE -> 0xFF55AA; default -> 0xAAAAAA; };

        String prefix = shiny ? "✨ " : "";
        int nameColor = shiny ? 0xFFD700 : COLOR_TEXT_TITLE;

        g.drawString(font, prefix + name, x, y, nameColor);
        int nameWidth = font.width(prefix + name);
        g.drawString(font, " " + genderIcon, x + nameWidth, y, genderColor);

        String lvText = "Lv." + level;
        int lvWidth = font.width(lvText);
        g.drawString(font, lvText, x + width - lvWidth, y, 0x55FF55);

        return y + 14;
    }

    private static int drawHPBar(GuiGraphics g, Font font, Pokemon target, int x, int y, int width) {
        int maxHp = target.getHp();
        int curHp = target.getCurrentHealth();
        float hpPercent = maxHp <= 0 ? 0.0f : Math.max(0.0f, Math.min(1.0f, (float) curHp / maxHp));

        g.drawString(font, "HP: " + curHp + "/" + maxHp, x, y, COLOR_TEXT_SUB);

        int barHeight = 4;
        int barY = y + 10;
        g.fill(x, barY, x + width, barY + barHeight, 0xFF333333);

        int fillColor = hpPercent > 0.5f ? 0xFF33FF33 : (hpPercent > 0.2f ? 0xFFFFFF33 : 0xFFFF3333);
        int fillWidth = (int) (width * hpPercent);
        if (fillWidth > 0) g.fill(x, barY, x + fillWidth, barY + barHeight, fillColor);

        return y + 20;
    }

    private static int drawElementsAndWeakness(GuiGraphics g, Font font, Pokemon target, List<TypeInfo> w4x, List<TypeInfo> w2x, int x, int y, int width) {
        ElementalType type1 = target.getPrimaryType();
        ElementalType type2 = target.getSecondaryType();

        int iconSize = 12;
        int iconSpacing = iconSize + 2;

        // --- Render Types ---
        g.drawString(font, "Type:", x, y, COLOR_TEXT_SUB);
        int currentX = x + font.width("Type: ");
        int imgYOffset = y - 2;

        int id1 = getTypeId(type1.getName().toUpperCase(Locale.ROOT));
        if (id1 == -1) return y;
        g.blit(TYPE_DATA[id1].icon(), currentX, imgYOffset, 0, 0, iconSize, iconSize, iconSize, iconSize);
        currentX += iconSpacing;

        if (type2 != null && !type2.getName().equals(type1.getName())) {
            g.drawString(font, "/", currentX, y, COLOR_TEXT_SUB);
            currentX += font.width("/") + 2;
            int id2 = getTypeId(type2.getName().toUpperCase(Locale.ROOT));
            if (id2 != -1) g.blit(TYPE_DATA[id2].icon(), currentX, imgYOffset, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        // --- Render Weakness ---
        int weakY = y + 14;
        boolean hasWeakness = false;

        if (!w4x.isEmpty()) {
            g.drawString(font, "Weak (4x):", x, weakY, COLOR_TEXT_SUB);
            currentX = x + font.width("Weak (4x): ");
            for (TypeInfo info : w4x) {
                g.blit(info.icon(), currentX, weakY - 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
                currentX += iconSpacing;
            }
            weakY += 14;
            hasWeakness = true;
        }

        if (!w2x.isEmpty()) {
            g.drawString(font, "Weak (2x):", x, weakY, COLOR_TEXT_SUB);
            currentX = x + font.width("Weak (2x): ");
            for (TypeInfo info : w2x) {
                g.blit(info.icon(), currentX, weakY - 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
                currentX += iconSpacing;
            }
            weakY += 14;
            hasWeakness = true;
        }

        if (!hasWeakness) {
            g.drawString(font, "Weak:", x, weakY, COLOR_TEXT_SUB);
            g.drawString(font, "None", x + font.width("Weak: "), weakY, COLOR_TEXT_TITLE);
            weakY += 14;
        }

        return weakY + 2;
    }
    private static int getTypeId(String name) {
        return TYPE_ID_MAP.getOrDefault(name, -1);
    }

    private static int drawStatsGrid(GuiGraphics g, Font font, String title, int hp, int atk, int def, int spa, int spd, int spe, int x, int y, int width) {
        int total = hp + atk + def + spa + spd + spe;
        int max = 186; // Max IVs
        int percent = (int) (((float)total / max) * 100);

        g.drawString(font, title + " - ", x, y, COLOR_TEXT_TITLE);
        g.drawString(font, percent + "%", x + font.width(title + " - "), y, getIvColor((percent * 31) / 100));

        int startY = y + 12;
        int colW = width / 3;

        drawStat(g, font, "HP", hp, x, startY);
        drawStat(g, font, "Atk", atk, x + colW, startY);
        drawStat(g, font, "Def", def, x + colW * 2, startY);

        drawStat(g, font, "SpA", spa, x, startY + 12);
        drawStat(g, font, "SpD", spd, x + colW, startY + 12);
        drawStat(g, font, "Spe", spe, x + colW * 2, startY + 12);

        return startY + 26;
    }

    private static void drawStat(GuiGraphics g, Font font, String label, int value, int x, int y) {
        g.drawString(font, label + ":", x, y, COLOR_TEXT_SUB);
        int valX = x + font.width(label + ": ");
        int color = getIvColor(value);
        g.drawString(font, String.valueOf(value), valX, y, color);
    }

    private static int getIvColor(int iv) {
        if (iv == 31) return 0xFF55FF55;
        if (iv >= 25) return 0xFF55FFFF;
        if (iv >= 15) return 0xFFFFFF55;
        if (iv >= 5)  return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private static void drawCatchRate(GuiGraphics g, Minecraft client, Font font, Pokemon target, int x, int y, int width) {
        ItemStack held = client.player.getMainHandItem();
        if (!(held.getItem() instanceof PokeBallItem)) held = client.player.getOffhandItem();

        Item info = held.getItem();
        if (info instanceof PokeBallItem ball) {
            CatchRateCalculator.CatchInfo catchInfo = CatchRateCalculator.calculateCatch(client.player, ball.getPokeBall(), target);

            g.drawString(font, "Catch:", x, y, COLOR_TEXT_SUB);
            String percentText = String.format("%.1f%%", catchInfo.chancePercent());
            g.drawString(font, percentText, x + font.width("Catch: "), y, getCatchRateColor(catchInfo.chancePercent()));

            String shakes = "(≈ " + catchInfo.expectedShakes() + " shakes)";
            g.drawString(font, shakes, x + width - font.width(shakes), y, COLOR_TEXT_SUB);
        }
    }

    private static int getCatchRateColor(float chancePercent) {
        if (chancePercent >= 99.0F) return 0xFF55FF55;
        if (chancePercent >= 50.0F) return 0xFF55FFFF;
        if (chancePercent >= 20.0F) return 0xFFFFFF55;
        if (chancePercent >= 5.0F)  return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private static Pokemon resolveTarget(Minecraft client, ClientBattle battle) {
        Pokemon target = null;

        if (battle != null) {
            target = findWildPokemonEntity(client, battle);
        } else {
            HitResult hit = client.hitResult;
            if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof PokemonEntity pokemonEntity) {
                target = pokemonEntity.getPokemon();
            }
        }

        if (target != null && target.isPlayerOwned() && client.player != null && client.player.getUUID().equals(target.getOwnerUUID())) {
            try {
                var party = com.cobblemon.mod.common.client.CobblemonClient.INSTANCE.getStorage().getMyParty();

                if (party != null) {
                    for (Pokemon p : party) {
                        if (p != null && p.getUuid().equals(target.getUuid())) {
                            return p;
                        }
                    }
                }
            } catch (Exception e) {
                CobblemonUtils.LOGGER.error("Failed to resolve party Pokémon target", e);
            }
        }

        return target;
    }

    private static Pokemon findWildPokemonEntity(Minecraft client, ClientBattle battle) {
        if (battle == null || client.level == null) return null;

        java.util.UUID currentBattleId = battle.getBattleId();

        if (cachedBattlePokemon != null && currentBattleId.equals(cachedBattleId)) {
            return cachedBattlePokemon;
        }

        for (Entity e : client.level.entitiesForRendering()) {
            if (e instanceof PokemonEntity pe) {
                Pokemon pokemon = pe.getPokemon();
                if (pe.getBattleId() != null && pe.getBattleId().equals(currentBattleId) && !pokemon.isPlayerOwned()) {
                    cachedBattleId = currentBattleId;
                    cachedBattlePokemon = pokemon;
                    return pokemon;
                }
            }
        }

        cachedBattleId = currentBattleId;
        cachedBattlePokemon = null;
        return null;
    }
}