package com.duox.cobblemonutils.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeybindManager {
    private static KeyMapping configKey;

    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        configKey = new KeyMapping("key.cobblemonutils.config", InputConstants.Type.KEYSYM, 67, "category.cobblemonutils.keys");
        event.register(configKey);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getInstance();
            while (configKey != null && configKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ConfigScreen().createConfigScreen(null));
                }
            }
        }
    }
}