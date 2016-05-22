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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.sponge.nms.IDHelper;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.biome.BiomeType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to biome data in Sponge.
 */
class SpongeBiomeRegistry implements BiomeRegistry {

    @Nullable
    @Override
    public BaseBiome createFromId(int id) {
        return new BaseBiome(id);
    }

    @Override
    public List<BaseBiome> getBiomes() {
        List<BaseBiome> list = new ArrayList<BaseBiome>();
        for (BiomeType biome : Sponge.getGame().getRegistry().getAllOf(BiomeType.class)) {
            list.add(new BaseBiome(IDHelper.resolve(biome)));
        }
        return list;
    }

    @Nullable
    @Override
    public BiomeData getData(BaseBiome biome) {
        return new SpongeBiomeData(IDHelper.resolveBiome(biome.getId()));
    }

    private static class SpongeBiomeData implements BiomeData {
        private final BiomeType biome;

        /**
         * Create a new instance.
         *
         * @param biome the base biome
         */
        private SpongeBiomeData(BiomeType biome) {
            this.biome = biome;
        }

        @Override
        public String getName() {
            return biome.getName();
        }
    }

}