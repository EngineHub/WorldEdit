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

import com.google.common.base.Function;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import javax.annotation.Nullable;

/**
 * Returns the name of a biome using a given {@code BiomeRegistry}.
 */
class BiomeName implements Function<BiomeType, String> {

    private final BiomeRegistry registry;

    /**
     * Create a new instance.
     *
     * @param registry the biome registry
     */
    BiomeName(BiomeRegistry registry) {
        checkNotNull(registry);
        this.registry = registry;
    }

    @Nullable
    @Override
    public String apply(BiomeType input) {
        BiomeData data = registry.getData(input);
        if (data != null) {
            return data.getName();
        } else {
            return null;
        }
    }

}
