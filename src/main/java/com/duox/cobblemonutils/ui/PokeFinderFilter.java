package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PokeFinderFilter {
    private static List<String> cachedSpeciesList;
    private static int cachedSpeciesHash;
    private static Set<String> cachedSpeciesSet = Set.of();

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

        String reason = getMatchReason(pokemon, config);
        if ("Shiny".equals(reason)) return 0xFFD700;
        if ("Legendary".equals(reason)) return 0xFF00FF;
        if ("Target".equals(reason)) return 0x00FFFF;
        return 0;
    }

    public static String getMatchReason(Pokemon pokemon, Config config) {
        if (config.highlightShinies && isPokemonShiny(pokemon)) return "Shiny";
        if (config.highlightLegendaries && pokemon.isLegendary()) return "Legendary";
        if (matchesSpecificSpecies(pokemon, config)) return "Target";
        return null;
    }

    private static boolean matchesSpecificSpecies(Pokemon pokemon, Config config) {
        if (config.specificSpecies.isEmpty()) return false;
        int hash = config.specificSpecies.hashCode();
        if (cachedSpeciesList != config.specificSpecies || cachedSpeciesHash != hash) {
            cachedSpeciesList = config.specificSpecies;
            cachedSpeciesHash = hash;
            cachedSpeciesSet = new HashSet<>();
            for (String s : config.specificSpecies) cachedSpeciesSet.add(s.trim().toLowerCase(Locale.ROOT));
        }
        return cachedSpeciesSet.contains(pokemon.getSpecies().getName().toLowerCase(Locale.ROOT));
    }

    private static boolean isPokemonShiny(Pokemon pokemon) {
        return pokemon.getShiny() || pokemon.getAspects().contains("shiny");
    }
}