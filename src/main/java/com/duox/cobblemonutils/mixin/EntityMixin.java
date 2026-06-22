package com.duox.cobblemonutils.mixin;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.duox.cobblemonutils.ui.PokeFinderFilter;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(
            method = "isCurrentlyGlowing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cobblemonutils$injectIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PokemonEntity pokemonEntity) {
            Config config = ConfigManager.getConfig();
            if (config.enablePokeFinder && config.enableGlowing) {
                int color = PokeFinderFilter.getHighlightColor(pokemonEntity);
                if (color != 0) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Inject(
            method = "getTeamColor",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cobblemonutils$injectGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof PokemonEntity pokemonEntity) {
            Config config = ConfigManager.getConfig();
            if (config.enablePokeFinder && config.enableGlowing) {
                int color = PokeFinderFilter.getHighlightColor(pokemonEntity);
                if (color != 0) {
                    cir.setReturnValue(color & 0xFFFFFF);
                }
            }
        }
    }
}