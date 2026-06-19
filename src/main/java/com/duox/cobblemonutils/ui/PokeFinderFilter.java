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
        Pokemon pokemon = pokemonEntity.getPokemon();

        Config config = ConfigManager.getConfig();

        if (config.ignoreOwned && pokemon.isPlayerOwned()) {
            return 0;
        }

        return getHighlightColor(pokemon);
    }

    public static int getHighlightColor(Pokemon pokemon) {
        Config config = ConfigManager.getConfig();
        if (!config.enablePokeFinder) return 0;

        if (isPokemonShiny(pokemon)) {
            return 0xFFD700;
        }

        if (config.highlightLegendaries && pokemon.isLegendary()) {
            return 0xFF00FF;
        }

        if (!config.specificSpecies.isEmpty()) {
            String speciesName = pokemon.getSpecies().getName().toLowerCase();
            for (String s : config.specificSpecies) {
                if (s.trim().equalsIgnoreCase(speciesName)) {
                    return 0x00FFFF;
                }
            }
        }

        return 0;
    }

    private static boolean isPokemonShiny(Pokemon pokemon) {
        if (pokemon.getShiny()) return true;

        return pokemon.getAspects().contains("shiny");
    }
}