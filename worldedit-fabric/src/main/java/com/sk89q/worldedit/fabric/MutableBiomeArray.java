package com.sk89q.worldedit.fabric;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;

/**
 * Interface over a {@link BiomeArray} as a mutable object.
 */
public interface MutableBiomeArray {

    /**
     * Hook into the given biome array, to allow edits on it.
     * @param biomeArray the biome array to edit
     * @return the mutable interface to the biome array
     */
    static MutableBiomeArray inject(BiomeArray biomeArray) {
        // It's Mixin'd
        return (MutableBiomeArray) biomeArray;
    }

    void setBiome(int x, int y, int z, Biome biome);

}
