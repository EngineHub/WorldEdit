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

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.registry.state.Property;

import java.util.Map;

/**
 * {@link FluidTypeStateList} with only one possible state.
 */
final class SingletonFluidTypeStateList extends FluidTypeStateList {

    private final FluidState state;

    SingletonFluidTypeStateList(FluidType fluidType) {
        this.state = new FluidState(fluidType, Map.of(), 0);
    }

    @Override
    public FluidState get(int index) {
        Preconditions.checkElementIndex(index, 1);
        return state;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    int calculateIndex(Map<Property<?>, ?> state) {
        if (!state.isEmpty()) {
            throw new IllegalArgumentException("No properties expected for singleton state");
        }
        return 0;
    }

    @Override
    int updateIndexOrInvalid(int currentIndex, Property<?> property, Object oldValue, Object newValue) {
        return -1;
    }

}
