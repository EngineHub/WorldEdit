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

package com.sk89q.worldedit.spout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;
import org.spout.api.generator.biome.BiomeGenerator;
import org.spout.api.generator.biome.Biome;

public class SpoutBiomeTypes implements BiomeTypes {
    private final Map<String, SpoutBiomeType> types = new HashMap<String, SpoutBiomeType>();

    @Override
    public boolean has(String name) {
        return types.containsKey(name.toLowerCase());
    }

    @Override
    public SpoutBiomeType get(String name) throws UnknownBiomeTypeException {
        if (!has(name)) {
            throw new UnknownBiomeTypeException(name);
        } else {
            return types.get(name.toLowerCase());
        }
    }

    public void registerBiomeTypes(BiomeGenerator generator) {
        for (Biome type : generator.getBiomes()) {
            final SpoutBiomeType weType = new SpoutBiomeType(type);
            if (!types.containsKey(weType.getName())) {
                types.put(weType.getName(), weType);
            }
        }
    }

    @Override
    public List<com.sk89q.worldedit.BiomeType> all() {
        return new ArrayList<com.sk89q.worldedit.BiomeType>(types.values());
    }
}
