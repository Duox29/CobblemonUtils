package com.duox.cobblemonutils;

import com.duox.cobblemonutils.config.ConfigManager;
import com.duox.cobblemonutils.config.ConfigScreen;
import com.duox.cobblemonutils.config.KeybindManager;
import com.duox.cobblemonutils.ui.NotificationManager;
import com.duox.cobblemonutils.ui.OverlayRenderer;
import com.duox.cobblemonutils.ui.PokeFinderRenderer;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CobblemonUtils.MODID)
public class CobblemonUtils {
    public static final String MODID = "cobblemonutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobblemonUtils() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

            ConfigManager.load();

            modEventBus.addListener(KeybindManager::registerKeybindings);

            MinecraftForge.EVENT_BUS.register(OverlayRenderer.class);
            MinecraftForge.EVENT_BUS.register(KeybindManager.class);
            MinecraftForge.EVENT_BUS.register(NotificationManager.class);
            MinecraftForge.EVENT_BUS.register(PokeFinderRenderer.class);

            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, parent) -> new ConfigScreen().createConfigScreen(parent)
                    )
            );
        }
    }

}