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

package com.sk89q.worldedit.world.fluid;

import javax.annotation.Nullable;

/**
 * Stores a list of categories of Block Types.
 */
public final class FluidCategories {

    public static final FluidCategory LAVA = register("minecraft:lava");
    public static final FluidCategory WATER = register("minecraft:water");

    private FluidCategories() {
    }

    private static FluidCategory register(final String id) {
        return register(new FluidCategory(id));
    }

    public static FluidCategory register(final FluidCategory tag) {
        return FluidCategory.REGISTRY.register(tag.getId(), tag);
    }

    public static @Nullable FluidCategory get(final String id) {
        return FluidCategory.REGISTRY.get(id);
    }
}
