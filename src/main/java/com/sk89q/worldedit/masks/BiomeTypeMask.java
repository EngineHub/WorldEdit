// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.masks;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

/**
 * A mask that matches a biome of a given list of biome types.
 */
public class BiomeTypeMask implements Mask {

    private final Set<BiomeType> biomes;

    /**
     * Create a new instance.
     */
    public BiomeTypeMask() {
        this(new HashSet<BiomeType>());
    }

    /**
     * Create a new instance with the given list of biomes.
     * 
     * @param biomes a list of bioems
     */
    public BiomeTypeMask(Set<BiomeType> biomes) {
        this.biomes = biomes;
    }

    /**
     * Get the list of biomes for this mask.
     * 
     * @return the list of biomes
     */
    public Set<BiomeType> getBiomes() {
        return biomes;
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

    /**
     * Checks to see if this mask matches the given position.
     * 
     * @param editSession the edit session
     * @param pos the position
     * @return true if there is a match
     */
    public boolean matches(EditSession editSession, Vector2D pos) {
        BiomeType biome = editSession.getWorld().getBiome(pos);
        return biomes.contains(biome);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return matches(editSession, pos.toVector2D());
    }
    
    @Override
    public String toString() {
        return String.format("BiomeTypeMask(biomes=%s)", biomes);
    }

}
