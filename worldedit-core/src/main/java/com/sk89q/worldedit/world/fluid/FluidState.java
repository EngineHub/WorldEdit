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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The fluid supplied by a block state.
 *
 * <p>A fluid state is derived from a block state by the active platform. This
 * allows blocks such as kelp and waterlogged blocks to supply fluid without
 * being fluid blocks themselves.</p>
 *
 * @param type the type of fluid supplied
 */
public record FluidState(FluidType type) {

    public static final FluidState EMPTY = new FluidState(FluidTypes.EMPTY);

    public FluidState {
        checkNotNull(type);
    }

    /**
     * Returns whether this state supplies no fluid.
     *
     * @return whether this state is empty
     */
    public boolean isEmpty() {
        return type == FluidTypes.EMPTY;
    }

}
