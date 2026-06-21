package com.duox.cobblemonutils.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public boolean enablePokeFinder = true;

    // Highlight Modes
    public boolean enableGlowing = true;
    public boolean enableTraceRay = false;
    public boolean enableBeaconBeam = false;

    // Filters
    public boolean ignoreOwned = true;
    public boolean highlightShinies = true;
    public boolean highlightLegendaries = true;
    public List<String> specificSpecies = new ArrayList<>();

    // Notifications
    public boolean enableNotifications = true;
    public NotificationType notificationType = NotificationType.ACTION_BAR;

    // Overlay
    public boolean showOverworldInfo = true;
    public boolean showIVs = true;
    public boolean showCatchRate = true;
    public int overlayX = 10;
    public int overlayY = 10;

    public enum NotificationType {
        CHAT,
        ACTION_BAR,
        TOAST
    }
}