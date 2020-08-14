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

package com.sk89q.worldedit.world.fluid;

import javax.annotation.Nullable;

/**
 * Stores a list of common Fluid String IDs.
 */
public final class FluidTypes {

    public static final FluidType EMPTY = register("minecraft:empty");
    public static final FluidType FLOWING_LAVA = register("minecraft:flowing_lava");
    public static final FluidType FLOWING_WATER = register("minecraft:flowing_water");
    public static final FluidType LAVA = register("minecraft:lava");
    public static final FluidType WATER = register("minecraft:water");

    private FluidTypes() {
    }

    private static FluidType register(final String id) {
        return register(new FluidType(id));
    }

    public static FluidType register(final FluidType fluid) {
        return FluidType.REGISTRY.register(fluid.getId(), fluid);
    }

    public static @Nullable FluidType get(final String id) {
        return FluidType.REGISTRY.get(id);
    }
}
