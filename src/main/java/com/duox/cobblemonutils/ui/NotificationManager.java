package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NotificationManager {
    private static final Set<UUID> notifiedEntities = new HashSet<>();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof PokemonEntity pokemonEntity) {
            checkAndNotify(pokemonEntity);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            notifiedEntities.clear();
        }
    }

    private static void checkAndNotify(PokemonEntity entity) {
        Config config = ConfigManager.getConfig();
        if (!config.enablePokeFinder || !config.enableNotifications) return;

        Pokemon pokemon = entity.getPokemon();
        if (config.ignoreOwned && pokemon.isPlayerOwned()) return;

        UUID uuid = entity.getUUID();
        if (notifiedEntities.contains(uuid)) return;

        String reason = getMatchReason(pokemon, config);
        if (reason != null) {
            notifiedEntities.add(uuid);
            sendNotification(entity, pokemon, reason, config.notificationType);
        }
    }

    private static String getMatchReason(Pokemon pokemon, Config config) {
        if (pokemon.getShiny() || pokemon.getAspects().contains("shiny")) {
            return "Shiny";
        }
        if (config.highlightLegendaries && pokemon.isLegendary()) {
            return "Legendary";
        }
        if (!config.specificSpecies.isEmpty()) {
            String speciesName = pokemon.getSpecies().getName().toLowerCase();
            for (String s : config.specificSpecies) {
                if (s.trim().equalsIgnoreCase(speciesName)) {
                    return "Target";
                }
            }
        }
        return null;
    }

    private static void sendNotification(PokemonEntity entity, Pokemon pokemon, String reason, Config.NotificationType type) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        String name = pokemon.getSpecies().getName();
        int level = pokemon.getLevel();
        int x = entity.getBlockX();
        int y = entity.getBlockY();
        int z = entity.getBlockZ();

        Component title = Component.literal("§6★ PokeFinder ★");
        Component message = Component.literal(String.format("§e%s §f%s (Lv.%d) §7at [%d, %d, %d]", reason, name, level, x, y, z));
        switch (type) {
            case CHAT:
                client.player.sendSystemMessage(Component.literal("§8[§6PokeFinder§8] ").append(message));
                break;
            case ACTION_BAR:
                client.player.displayClientMessage(message, true);
                break;
            case TOAST:
                SystemToast.add(client.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, title, message);
                break;
        }
    }
}