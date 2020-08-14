/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;

/**
 * Returns a {@link BiomeType} for a given position.
 */
public interface BiomePattern {

    /**
     * Return a {@link BiomeType} for the given position.
     *
     * @param position the position
     * @return a biome
     * @deprecated use {@link BiomePattern#applyBiome(BlockVector3)}
     */
    @Deprecated
    default BiomeType apply(BlockVector2 position) {
        return applyBiome(position.toBlockVector3());
    }

    /**
     * Return a {@link BiomeType} for the given position.
     *
     * @param position the position
     * @return a biome
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "apply",
        delegateParams = { BlockVector2.class }
    )
    default BiomeType applyBiome(BlockVector3 position) {
        DeprecationUtil.checkDelegatingOverride(getClass());

        return apply(position.toBlockVector2());
    }
}
