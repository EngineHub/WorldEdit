/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.biome;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Basic storage object to represent a given biome.
 */
public class BaseBiome {

    private int id;

    /**
     * Create a new biome with the given biome ID.
     *
     * @param id the biome ID
     */
    public BaseBiome(int id) {
        this.id = id;
    }

    /**
     * Create a clone of the given biome.
     *
     * @param biome the biome to clone
     */
    public BaseBiome(BaseBiome biome) {
        checkNotNull(biome);
        this.id = biome.getId();
    }

    /**
     * Get the biome ID.
     *
     * @return the biome ID
     */
    public int getId() {
        return id;
    }

    /**
     * Set the biome id.
     *
     * @param id the biome ID
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseBiome baseBiome = (BaseBiome) o;

        return id == baseBiome.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
