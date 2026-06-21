package com.duox.cobblemonutils.utils;

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.*;
import net.minecraft.world.entity.player.Player;

public class CatchRateCalculator {

    public record CatchInfo(float chancePercent, int expectedShakes) {}

    public static CatchInfo calculateCatch(Player thrower, PokeBall ball, Pokemon pokemon) {
        CatchRateModifier modifier = ball.getCatchRateModifier();
        if (modifier.isGuaranteed()) {
            return new CatchInfo(100.0F, 4);
        }

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
        float darkGrass = 1.0F; // Hardcoded in original
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

    private static float applyBehavior(CatchRateModifier.Behavior behavior, float input, float value) {
        return switch (behavior) {
            case ADD -> input + value;
            case SUBTRACT -> input - value;
            case MULTIPLY -> input * value;
            case DIVIDE -> value == 0.0F ? input : input / value;
        };
    }
}