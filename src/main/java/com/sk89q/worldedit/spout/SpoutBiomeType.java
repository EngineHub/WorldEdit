package com.sk89q.worldedit.spout;

import org.spout.api.generator.biome.BiomeType;

/**
 * @author zml2008
 */
public class SpoutBiomeType extends com.sk89q.worldedit.BiomeType {
    private final BiomeType type;

    public SpoutBiomeType(BiomeType type) {
        super(type.getName().toLowerCase().replace(" ", ""));
        this.type = type;
    }

    public BiomeType getSpoutBiome() {
        return type;
    }
}
