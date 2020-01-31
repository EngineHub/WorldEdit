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

package com.sk89q.worldedit.internal.util;

import net.royawesome.jlibnoise.MathHelper;

public class BiomeMath {

    // From BiomeArray / BiomeContainer
    public static final int HORIZONTAL_SECTION_COUNT = (int) Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
    public static final int VERTICAL_SECTION_COUNT = (int)Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
    public static final int HORIZONTAL_BIT_MASK = (1 << HORIZONTAL_SECTION_COUNT) - 1;
    public static final int VERTICAL_BIT_MASK = (1 << VERTICAL_SECTION_COUNT) - 1;

    private BiomeMath() {
    }

    /**
     * Compute the index into the MC biome array.
     *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the index into the standard MC biome array
     */
    public static int computeBiomeIndex(int x, int y, int z) {
        int l = x & HORIZONTAL_BIT_MASK;
        int m = MathHelper.clamp(y, 0, VERTICAL_BIT_MASK);
        int n = z & HORIZONTAL_BIT_MASK;
        return m << HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT
            | n << HORIZONTAL_SECTION_COUNT
            | l;
    }
}
