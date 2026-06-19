package com.duox.cobblemonutils.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public boolean enablePokeFinder = true;
    public boolean ignoreOwned = true;
    public boolean highlightShinies = true;
    public boolean highlightLegendaries = true;
    public List<String> specificSpecies = new ArrayList<>();

    public boolean showOverworldInfo = true;
    public boolean showIVs = true;
    public boolean showCatchRate = true;

    public int overlayX = 10;
    public int overlayY = 10;
}