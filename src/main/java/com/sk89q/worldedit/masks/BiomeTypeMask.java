package com.sk89q.worldedit.masks;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

public class BiomeTypeMask implements Mask {

    private Set<BiomeType> biomes;

    public BiomeTypeMask() {
        this(new HashSet<BiomeType>());
    }

    public BiomeTypeMask(Set<BiomeType> biomes) {
        this.biomes = biomes;
    }

    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

    public boolean matches(EditSession editSession, Vector pos) {
        BiomeType biome = editSession.getWorld().getBiome(pos.toVector2D());
        return biomes.contains(biome);
    }

}
