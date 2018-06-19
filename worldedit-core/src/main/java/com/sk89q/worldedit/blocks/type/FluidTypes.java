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

package com.sk89q.worldedit.blocks.type;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Stores a list of common Fluid String IDs.
 */
public class FluidTypes {

    private FluidTypes() {
    }

    public static final FluidType EMPTY = new FluidType("minecraft:empty");
    public static final FluidType FLOWING_LAVA = new FluidType("minecraft:flowing_lava");
    public static final FluidType FLOWING_WATER = new FluidType("minecraft:flowing_water");
    public static final FluidType LAVA = new FluidType("minecraft:lava");
    public static final FluidType WATER = new FluidType("minecraft:water");


    private static final Map<String, FluidType> fluidMapping = new HashMap<>();

    static {
        for (Field field : FluidTypes.class.getFields()) {
            if (field.getType() == FluidType.class) {
                try {
                    registerFluid((FluidType) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerFluid(FluidType fluidType) {
        if (fluidMapping.containsKey(fluidType.getId()) && !fluidType.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Existing fluid with this ID already registered");
        }

        fluidMapping.put(fluidType.getId(), fluidType);
    }

    @Nullable
    public static FluidType getFluidType(String id) {
        // If it has no namespace, assume minecraft.
        if (id != null && !id.contains(":")) {
            id = "minecraft:" + id;
        }
        return fluidMapping.get(id);
    }

    public static Collection<FluidType> values() {
        return fluidMapping.values();
    }
}
