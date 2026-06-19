package com.duox.cobblemonutils;

import com.duox.cobblemonutils.config.ConfigManager;
import com.duox.cobblemonutils.config.KeybindManager;
import com.duox.cobblemonutils.config.ConfigScreen;
import com.duox.cobblemonutils.ui.OverlayRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.client.ConfigScreenHandler;

@Mod(CobblemonUtils.MODID)
public class CobblemonUtils {
    public static final String MODID = "cobblemonutils";

    public CobblemonUtils() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

            // Load cấu hình trước
            ConfigManager.load();

            // Đăng ký các sự kiện khởi tạo của Forge Client
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeybindManager::registerKeybindings);

            // Đăng ký HUD Overlay vào sự kiện Forge Bus
            MinecraftForge.EVENT_BUS.register(OverlayRenderer.class);
            MinecraftForge.EVENT_BUS.register(KeybindManager.class);

            // Tích hợp màn hình cấu hình vào Forge Mod List GUI thay cho ModMenu
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, parent) -> new ConfigScreen().createConfigScreen(parent)
                    )
            );
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Khởi tạo các thành phần client side
    }
}