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
 * Stores a list of categories of Block Types.
 */
public class FluidCategories {

    private FluidCategories() {
    }

    public static final FluidCategory LAVA = new FluidCategory("minecraft:lava");
    public static final FluidCategory WATER = new FluidCategory("minecraft:water");

    private static final Map<String, FluidCategory> categoryMapping = new HashMap<>();

    static {
        for (Field field : FluidCategories.class.getFields()) {
            if (field.getType() == FluidCategory.class) {
                try {
                    registerCategory((FluidCategory) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerCategory(FluidCategory fluidCategory) {
        if (categoryMapping.containsKey(fluidCategory.getId()) && !fluidCategory.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Existing category with this ID already registered");
        }

        categoryMapping.put(fluidCategory.getId(), fluidCategory);
    }

    @Nullable
    public static FluidCategory getFluidCategory(String id) {
        // If it has no namespace, assume minecraft.
        if (id != null && !id.contains(":")) {
            id = "minecraft:" + id;
        }
        return categoryMapping.get(id);
    }

    public static Collection<FluidCategory> values() {
        return categoryMapping.values();
    }
}
