package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.duox.cobblemonutils.utils.CatchRateCalculator;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer {
    private static Pokemon currentTarget = null;

    // Màu sắc mặc định thay cho các số magic
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final int COLOR_GRAY = 0xAAAAAA;

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

        int startY = config.overlayY;
        startY = drawBasicInfo(g, client, currentTarget, config.overlayX, startY);

        if (config.showIVs) {
            startY = drawIVs(g, client, currentTarget, config.overlayX, startY);
        }

        if (config.showCatchRate) {
            drawCatchRate(g, client, currentTarget, config.overlayX, startY);
        }
    }

    private static int drawBasicInfo(GuiGraphics g, Minecraft client, Pokemon target, int x, int y) {
        String name = target.getSpecies().getName();
        int level = target.getLevel();
        boolean shiny = target.getShiny();

        String nameLine = (shiny ? ChatFormatting.YELLOW + "✨ " : "") + ChatFormatting.WHITE + name + ChatFormatting.GRAY + " Lv." + level;
        g.drawString(client.font, nameLine, x, y, COLOR_WHITE);
        y += 10;

        String hpLine = "HP: " + target.getCurrentHealth() + " / " + target.getHp();
        g.drawString(client.font, hpLine, x, y, COLOR_WHITE);

        return y + 10;
    }

    private static int drawIVs(GuiGraphics g, Minecraft client, Pokemon target, int x, int y) {
        int hpIv = target.getIvs().getOrDefault(Stats.HP);
        int atkIv = target.getIvs().getOrDefault(Stats.ATTACK);
        int defIv = target.getIvs().getOrDefault(Stats.DEFENCE);
        int spaIv = target.getIvs().getOrDefault(Stats.SPECIAL_ATTACK);
        int spdIv = target.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE);
        int speIv = target.getIvs().getOrDefault(Stats.SPEED);

        String ivLine = String.format("IVs: HP:%d A:%d D:%d SA:%d SD:%d S:%d", hpIv, atkIv, defIv, spaIv, spdIv, speIv);
        g.drawString(client.font, ivLine, x, y, COLOR_GRAY);
        return y + 10;
    }

    private static void drawCatchRate(GuiGraphics g, Minecraft client, Pokemon target, int x, int y) {
        ItemStack held = client.player.getMainHandItem();
        if (!(held.getItem() instanceof PokeBallItem)) {
            held = client.player.getOffhandItem();
        }

        Item info = held.getItem();
        if (info instanceof PokeBallItem ball) {
            CatchRateCalculator.CatchInfo catchInfo = CatchRateCalculator.calculateCatch(client.player, ball.getPokeBall(), target);
            String ballName = ball.getPokeBall().getName().getPath().replace('_', ' ');
            String catchLine = String.format("%s → %.1f%% (≈%d shakes)", ballName, catchInfo.chancePercent(), catchInfo.expectedShakes());

            int color = getCatchRateColor(catchInfo.chancePercent());
            g.drawString(client.font, catchLine, x, y, color);
        }
    }

    private static int getCatchRateColor(float chancePercent) {
        if (chancePercent >= 99.0F) return 0xFFDD00; // Gold
        if (chancePercent >= 50.0F) return 0x00FF00; // Green
        if (chancePercent >= 20.0F) return 0xFFAA00; // Orange
        return 0xFF5555; // Red
    }

    private static Pokemon resolveTarget(Minecraft client, ClientBattle battle) {
        if (battle != null) {
            Pokemon wild = findWildPokemonEntity(client, battle);
            if (wild != null) return wild;
        }

        HitResult hit = client.hitResult;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof PokemonEntity pokemonEntity) {
            return pokemonEntity.getPokemon();
        }
        return null;
    }

    private static Pokemon findWildPokemonEntity(Minecraft client, ClientBattle battle) {
        if (battle == null || client.level == null) return null;

        for (Entity e : client.level.entitiesForRendering()) {
            if (e instanceof PokemonEntity pe) {
                Pokemon pokemon = pe.getPokemon();
                if (pe.getBattleId() != null && pe.getBattleId().equals(battle.getBattleId()) && !pokemon.isPlayerOwned()) {
                    return pokemon;
                }
            }
        }
        return null;
    }
}