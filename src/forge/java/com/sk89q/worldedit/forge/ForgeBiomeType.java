package com.sk89q.worldedit.forge;

import net.minecraft.world.biome.BiomeGenBase;

import com.sk89q.worldedit.BiomeType;

public class ForgeBiomeType implements BiomeType {
    private BiomeGenBase biome;

    public ForgeBiomeType(BiomeGenBase biome) {
        this.biome = biome;
    }

    public String getName() {
        return this.biome.biomeName;
    }
}