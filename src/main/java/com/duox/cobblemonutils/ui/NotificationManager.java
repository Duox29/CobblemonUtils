package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class NotificationManager {
    private static final int MAX_NOTIFIED_ENTITIES = 1000;
    private static final Set<UUID> notifiedEntities = new LinkedHashSet<>();

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

        String reason = PokeFinderFilter.getMatchReason(pokemon, config);
        if (reason != null) {
            if (notifiedEntities.size() >= MAX_NOTIFIED_ENTITIES) {
                notifiedEntities.remove(notifiedEntities.iterator().next());
            }
            notifiedEntities.add(uuid);
            sendNotification(entity, pokemon, reason, config.notificationType);
        }
    }

    private static void sendNotification(PokemonEntity entity, Pokemon pokemon, String reason, Config.NotificationType type) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        String name = pokemon.getSpecies().getName();
        int level = pokemon.getLevel();

        MutableComponent title = Component.literal("★ PokeFinder ★").withStyle(ChatFormatting.GOLD);

        MutableComponent message = Component.literal(reason + " ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(name + " (Lv." + level + ") ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format("at [%d, %d, %d]", entity.getBlockX(), entity.getBlockY(), entity.getBlockZ())).withStyle(ChatFormatting.GRAY));

        switch (type) {
            case CHAT:
                Component prefix = Component.literal("[PokeFinder] ").withStyle(ChatFormatting.GOLD);
                client.player.sendSystemMessage(prefix.copy().append(message));
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