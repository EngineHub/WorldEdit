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

import com.sk89q.worldedit.registry.state.Property;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The fluid supplied by a block state.
 *
 * <p>A fluid state is derived from a block state by the active platform. This
 * allows blocks such as kelp and waterlogged blocks to supply fluid without
 * being fluid blocks themselves.</p>
 */
public class FluidState {

    private final FluidType type;
    private final Map<Property<?>, Object> values;
    private final int stateListIndex;

    protected FluidState(FluidType type, Map<Property<?>, Object> values, int stateListIndex) {
        this.type = checkNotNull(type);
        this.values = values;
        this.stateListIndex = stateListIndex;
    }

    /**
     * Gets the type of fluid supplied.
     *
     * @return the fluid type
     */
    public FluidType getType() {
        return type;
    }

    /**
     * Returns whether this state supplies no fluid.
     *
     * @return whether this state is empty
     */
    public boolean isEmpty() {
        return type == FluidTypes.EMPTY;
    }

    public <V> FluidState with(Property<V> property, V value) {
        Object currentValue = values.get(property);
        if (Objects.equals(currentValue, value)) {
            return this;
        }
        int newIndex = type.getInternalStateList().updateIndexOrInvalid(
            stateListIndex, property, currentValue, value);
        return newIndex == -1 ? this : type.getInternalStateList().get(newIndex);
    }

    @SuppressWarnings("unchecked")
    public <V> V getState(Property<V> property) {
        return (V) values.get(property);
    }

    public Map<Property<?>, Object> getStates() {
        return values;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FluidState fluidState)) {
            return false;
        }
        return type.equals(fluidState.type) && values.equals(fluidState.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, values);
    }

}
