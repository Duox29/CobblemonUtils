package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.function.ToIntFunction;

public class PokeFinderFilter {
    private static volatile ToIntFunction<Pokemon> extension;

    public static void registerExtension(ToIntFunction<Pokemon> ext) {
        extension = ext;
    }

    public static int getHighlightColor(PokemonEntity pokemonEntity) {
        Config config = ConfigManager.getConfig();

        if (config.ignoreOwned && pokemonEntity.getPokemon().getOwnerUUID() != null) {
            return 0;
        } else {
            int color = getHighlightColor(pokemonEntity.getPokemon());
            if (color == 0) {
                return 0;
            } else {
                return LineOfSight.isVisible(pokemonEntity) ? color : 0;
            }
        }
    }

    public static int getHighlightColor(Pokemon pokemon) {
        Config config = ConfigManager.getConfig();
        if (!config.enablePokeFinder) {
            return 0;
        } else if (config.highlightShinies && pokemon.getShiny()) {
            return config.shinyColor;
        } else if (config.highlightLegendaries && pokemon.isLegendary()) {
            return config.legendaryColor;
        } else {
            if (!config.specificSpecies.isEmpty()) {
                String speciesName = pokemon.getSpecies().getName().toLowerCase();

                for (String s : config.specificSpecies) {
                    if (s.trim().toLowerCase().equals(speciesName)) {
                        return config.speciesColor;
                    }
                }
            }

            ToIntFunction<Pokemon> ext = extension;
            if (ext != null) {
                int extColor = ext.applyAsInt(pokemon);
                if (extColor != 0) {
                    return extColor;
                }
            }

            return 0;
        }
    }
}