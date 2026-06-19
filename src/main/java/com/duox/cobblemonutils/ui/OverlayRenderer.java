package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer {
    // Biến lưu trữ mục tiêu hiện tại, chỉ cập nhật mỗi Tick
    private static Pokemon currentTarget = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        // Quét tìm Pokemon được thực hiện ở đây thay vì nhồi nhét vào Frame Render
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

        // Thay thế hardcode bằng biến từ config
        int startX = config.overlayX;
        int startY = config.overlayY;

        startY = drawBasicInfo(g, client, currentTarget, startX, startY);

        if (config.showIVs) {
            startY = drawIVs(g, client, currentTarget, startX, startY);
        }

        if (config.showCatchRate) {
            drawCatchRate(g, client, currentTarget, startX, startY);
        }
    }

    private static int drawBasicInfo(GuiGraphics g, Minecraft client, Pokemon target, int x, int y) {
        String name = target.getSpecies().getName();
        int level = target.getLevel();
        boolean shiny = target.getShiny();

        String nameLine = (shiny ? "§e✨ " : "") + name + " §7Lv." + level;
        g.drawString(client.font, nameLine, x, y, 16777215);
        y += 10;

        String hpLine = "HP: " + target.getCurrentHealth() + " / " + target.getHp();
        g.drawString(client.font, hpLine, x, y, 16777215);
        y += 10;

        return y;
    }

    private static int drawIVs(GuiGraphics g, Minecraft client, Pokemon target, int x, int y) {
        int hpIv = target.getIvs().getOrDefault(Stats.HP);
        int atkIv = target.getIvs().getOrDefault(Stats.ATTACK);
        int defIv = target.getIvs().getOrDefault(Stats.DEFENCE);
        int spaIv = target.getIvs().getOrDefault(Stats.SPECIAL_ATTACK);
        int spdIv = target.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE);
        int speIv = target.getIvs().getOrDefault(Stats.SPEED);

        String ivLine = String.format("IVs: HP:%d A:%d D:%d SA:%d SD:%d S:%d", hpIv, atkIv, defIv, spaIv, spdIv, speIv);
        g.drawString(client.font, ivLine, x, y, 11184810);
        return y + 10;
    }

    private static void drawCatchRate(GuiGraphics g, Minecraft client, Pokemon target, int x, int y) {
        ItemStack held = client.player.getMainHandItem();
        if (!(held.getItem() instanceof PokeBallItem)) {
            held = client.player.getOffhandItem();
        }

        Item info = held.getItem();
        if (info instanceof PokeBallItem ball) {
            CatchInfo catchInfo = calculateCatch(client.player, ball.getPokeBall(), target);
            String catchLine = String.format("%s → %.1f%% (≈%d shakes)", ball.getPokeBall().getName().getPath().replace('_', ' '), catchInfo.chancePercent, catchInfo.expectedShakes);
            int color = catchInfo.chancePercent >= 99.0F ? 16766720 : (catchInfo.chancePercent >= 50.0F ? '\uff00' : (catchInfo.chancePercent >= 20.0F ? 16777045 : 16733525));
            g.drawString(client.font, catchLine, x, y, color);
        }
    }

    private static Pokemon resolveTarget(Minecraft client, ClientBattle battle) {
        if (battle != null) {
            Pokemon wild = findWildPokemonEntity(client, battle);
            if (wild != null) return wild;
        }

        HitResult hit = client.hitResult;
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof PokemonEntity pokemonEntity) {
                return pokemonEntity.getPokemon();
            }
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

    private static CatchInfo calculateCatch(Player thrower, PokeBall ball, Pokemon pokemon) {
        CatchRateModifier modifier = ball.getCatchRateModifier();
        if (modifier.isGuaranteed()) {
            return new CatchInfo(100.0F, 4);
        } else {
            int maxHp = pokemon.getHp();
            int curHp = pokemon.getCurrentHealth();
            float catchRate = (float) pokemon.getForm().getCatchRate();
            float inBattleMod = pokemon.getEntity() != null && pokemon.getEntity().getBattleId() != null ? 1.0F : 0.5F;
            boolean valid = modifier.isValid(thrower, pokemon);
            float ballBonus = valid ? modifier.value(thrower, pokemon) : 1.0F;
            float bonusStatus = 1.0F;
            PersistentStatusContainer statusContainer = pokemon.getStatus();
            if (statusContainer != null) {
                PersistentStatus s = statusContainer.getStatus();
                if (!(s instanceof SleepStatus) && !(s instanceof FrozenStatus)) {
                    if (s instanceof ParalysisStatus || s instanceof BurnStatus || s instanceof PoisonStatus || s instanceof PoisonBadlyStatus) {
                        bonusStatus = 1.5F;
                    }
                } else {
                    bonusStatus = 2.5F;
                }
            }

            int bonusLevel = pokemon.getLevel() < 13 ? Math.max((36 - 2 * pokemon.getLevel()) / 10, 1) : 1;
            float darkGrass = 1.0F;
            float base = (3.0F * (float) maxHp - 2.0F * (float) curHp) * darkGrass * catchRate * inBattleMod;
            float modified = applyBehavior(modifier.behavior(thrower, pokemon), base, ballBonus) / (3.0F * (float) maxHp);
            modified *= bonusStatus * (float) bonusLevel;
            if (modified >= 255.0F) {
                return new CatchInfo(100.0F, 4);
            } else {
                double shakeProb = 65536.0F / Math.pow(255.0F / modified, 0.1875F);
                double perShake = Math.min(1.0F, Math.max(0.0F, shakeProb / 65536.0F));
                double capture = Math.pow(perShake, 4.0F);
                int expected = (int) Math.round(perShake * 4.0F);
                return new CatchInfo((float) (capture * 100.0F), expected);
            }
        }
    }

    private static float applyBehavior(CatchRateModifier.Behavior behavior, float input, float value) {
        return switch (behavior) {
            case ADD -> input + value;
            case SUBTRACT -> input - value;
            case MULTIPLY -> input * value;
            case DIVIDE -> value == 0.0F ? input : input / value;
        };
    }

    private record CatchInfo(float chancePercent, int expectedShakes) {}
}