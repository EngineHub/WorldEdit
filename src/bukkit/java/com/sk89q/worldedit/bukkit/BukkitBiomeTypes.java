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

package com.sk89q.worldedit.bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;

public class BukkitBiomeTypes implements BiomeTypes {

    public BukkitBiomeTypes() {
    }

    @Override
    public boolean has(String name) {
        try {
            BukkitBiomeType.valueOf(name.toUpperCase(Locale.ENGLISH));
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    @Override
    public BiomeType get(String name) throws UnknownBiomeTypeException {
        try {
            return BukkitBiomeType.valueOf(name.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exc) {
            throw new UnknownBiomeTypeException(name);
        }
    }

    @Override
    public List<BiomeType> all() {
        return Arrays.<BiomeType>asList(BukkitBiomeType.values());
    }

}
