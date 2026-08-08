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

/**
 * Stores a list of common {@link FluidCategory FluidCategories}.
 *
 * @see FluidCategory
 */
@SuppressWarnings("unused")
public final class FluidCategories {
    public static final FluidCategory BUBBLE_COLUMN_CAN_OCCUPY = get("minecraft:bubble_column_can_occupy");
    public static final FluidCategory LAVA = get("minecraft:lava");
    public static final FluidCategory SUPPORTS_FROGSPAWN = get("minecraft:supports_frogspawn");
    public static final FluidCategory SUPPORTS_LILY_PAD = get("minecraft:supports_lily_pad");
    public static final FluidCategory SUPPORTS_SUGAR_CANE_ADJACENTLY = get("minecraft:supports_sugar_cane_adjacently");
    public static final FluidCategory WATER = get("minecraft:water");

    private FluidCategories() {
    }

    /**
     * Gets the {@link FluidCategory} associated with the given id.
     */
    public static FluidCategory get(String id) {
        FluidCategory entry = FluidCategory.REGISTRY.get(id);
        if (entry == null) {
            return new FluidCategory(id);
        }
        return entry;
    }
}
