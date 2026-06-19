package com.duox.cobblemonutils.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public boolean enablePokeFinder = true;
    public boolean ignoreOwned = true;
    public boolean highlightShinies = true;
    public boolean highlightLegendaries = true;
    public int shinyColor = -10496;
    public int legendaryColor = -65281;
    public List<String> specificSpecies = new ArrayList<>();
    public int speciesColor = -16711681;
    public boolean showOverworldInfo = true;
    public boolean showIVs = true;
    public boolean showCatchRate = true;
}