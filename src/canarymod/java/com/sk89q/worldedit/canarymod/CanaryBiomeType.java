package com.sk89q.worldedit.canarymod;

import java.util.Locale;

import com.sk89q.worldedit.BiomeType;

public enum CanaryBiomeType implements BiomeType {
    OCEAN(net.canarymod.api.world.BiomeType.OCEAN),
    PLAINS(net.canarymod.api.world.BiomeType.PLAINS),
    DESERT(net.canarymod.api.world.BiomeType.DESERT),
    EXTREME_HILLS(net.canarymod.api.world.BiomeType.HILLS_EXTREME),
    FOREST(net.canarymod.api.world.BiomeType.FOREST),
    TAIGA(net.canarymod.api.world.BiomeType.TAIGA),
    SWAMPLAND(net.canarymod.api.world.BiomeType.SWAMPLAND),
    RIVER(net.canarymod.api.world.BiomeType.RIVER),
    HELL(net.canarymod.api.world.BiomeType.HELL),
    SKY(net.canarymod.api.world.BiomeType.SKY),
    FROZEN_OCEAN(net.canarymod.api.world.BiomeType.OCEAN_FROZEN),
    FROZEN_RIVER(net.canarymod.api.world.BiomeType.RIVER_FROZEN),
    ICE_PLAINS(net.canarymod.api.world.BiomeType.PLAINS_ICE),
    ICE_MOUNTAINS(net.canarymod.api.world.BiomeType.MOUNTAINS_ICE),
    MUSHROOM_ISLAND(net.canarymod.api.world.BiomeType.MUSHROOM_ISLAND),
    MUSHROOM_SHORE(net.canarymod.api.world.BiomeType.MUSHROOM_ISLAND_SHORE),
    BEACH(net.canarymod.api.world.BiomeType.BEACH),
    DESERT_HILLS(net.canarymod.api.world.BiomeType.HILLS_DESERT),
    FOREST_HILLS(net.canarymod.api.world.BiomeType.HILLS_FOREST),
    TAIGA_HILLS(net.canarymod.api.world.BiomeType.HILLS_TAIGA),
    JUNGLE(net.canarymod.api.world.BiomeType.JUNGLE),
    JUNGLE_HILLS(net.canarymod.api.world.BiomeType.HILLS_JUNGLE);

    net.canarymod.api.world.BiomeType type;

    CanaryBiomeType(net.canarymod.api.world.BiomeType type) {
        this.type = type;
    }
    @Override
    public String getName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public net.canarymod.api.world.BiomeType getCanaryBiomeType() {
        return type;
    }

    public static CanaryBiomeType fromNative(net.canarymod.api.world.BiomeType type) {
        for(CanaryBiomeType t : values()) {
            if(t.getCanaryBiomeType() == type) {
                return t;
            }
        }
        return null;
    }
}
