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

import org.spout.api.generator.biome.Biome;

/**
 * @author zml2008
 */
public class SpoutBiomeType implements com.sk89q.worldedit.BiomeType {
    private final Biome type;

    public SpoutBiomeType(Biome type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return type.getName().toLowerCase().replace(" ", "");
    }

    public Biome getSpoutBiome() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SpoutBiomeType && ((SpoutBiomeType)other).type.equals(this.type);
    }

    @Override
    public int hashCode() {
        return 31 + type.hashCode();
    }
}
