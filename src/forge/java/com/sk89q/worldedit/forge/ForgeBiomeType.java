package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.BiomeType;
import net.minecraft.world.biome.BiomeGenBase;

public class ForgeBiomeType implements BiomeType {
    private BiomeGenBase biome;

    public ForgeBiomeType(BiomeGenBase biome) {
        this.biome = biome;
    }

    public String getName() {
        return this.biome.biomeName;
    }
}