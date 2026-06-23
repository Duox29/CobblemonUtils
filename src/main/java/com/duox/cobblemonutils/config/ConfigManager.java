package com.duox.cobblemonutils.config;

import com.duox.cobblemonutils.CobblemonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "cobblemonutils.json");
    private static Config config;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, Config.class);
                if (config == null) {
                    config = new Config();
                    save();
                } else if (normalize()) {
                    save();
                }
            } catch (Exception e) {
                CobblemonUtils.LOGGER.error("Failed to load CobblemonUtils config", e);
                config = new Config();
                save();
            }
        } else {
            config = new Config();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            CobblemonUtils.LOGGER.error("Failed to save CobblemonUtils config", e);
        }
    }

    private static boolean normalize() {
        boolean changed = false;
        if (config.specificSpecies == null) {
            config.specificSpecies = new ArrayList<>();
            changed = true;
        }
        if (config.notificationType == null) {
            config.notificationType = Config.NotificationType.CHAT;
            changed = true;
        }
        return changed;
    }

    public static Config getConfig() {
        return config;
    }
}