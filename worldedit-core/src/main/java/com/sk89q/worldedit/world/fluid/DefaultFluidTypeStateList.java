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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.registry.state.Property;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Collection;
import java.util.Map;

/**
 * The canonical fluid states for a fluid type.
 */
final class DefaultFluidTypeStateList extends FluidTypeStateList {
    private record PropertyEntry(Property<?> property, ImmutableList<?> values, int stride) {
        int offset(Object value) {
            int index = values.indexOf(value);
            return index == -1 ? -1 : index * stride;
        }
    }

    private final ImmutableList<PropertyEntry> properties;
    private final ImmutableList<FluidState> states;

    DefaultFluidTypeStateList(FluidType fluidType) {
        Collection<? extends Property<?>> fluidProperties = fluidType.getProperties();
        ImmutableList.Builder<PropertyEntry> entries = ImmutableList.builder();
        int totalStates = 1;
        for (Property<?> property : fluidProperties) {
            entries.add(new PropertyEntry(property, ImmutableList.copyOf(property.values()), totalStates));
            totalStates = Math.multiplyExact(totalStates, property.values().size());
        }
        this.properties = entries.build();
        this.states = createStates(fluidType, totalStates);
    }

    private ImmutableList<FluidState> createStates(FluidType type, int totalStates) {
        Property<?>[] keys = new Property<?>[properties.size()];
        Object[] values = new Object[properties.size()];
        int[] counters = new int[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            keys[i] = properties.get(i).property();
            values[i] = properties.get(i).values().getFirst();
        }
        ImmutableList.Builder<FluidState> result = ImmutableList.builderWithExpectedSize(totalStates);
        for (int i = 0; i < totalStates; i++) {
            result.add(new FluidState(type,
                Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(keys, values.clone())), i));
            for (int slot = 0; slot < properties.size(); slot++) {
                PropertyEntry property = properties.get(slot);
                if (++counters[slot] < property.values().size()) {
                    values[slot] = property.values().get(counters[slot]);
                    break;
                }
                counters[slot] = 0;
                values[slot] = property.values().getFirst();
            }
        }
        return result.build();
    }

    @Override
    public FluidState get(int index) {
        return states.get(index);
    }

    @Override
    public int size() {
        return states.size();
    }

    @Override
    int calculateIndex(Map<Property<?>, ?> state) {
        if (state.size() != properties.size()) {
            throw new IllegalArgumentException("State has incorrect number of properties.");
        }
        int index = 0;
        for (PropertyEntry property : properties) {
            if (!state.containsKey(property.property())) {
                throw new IllegalArgumentException("Missing property " + property.property().name());
            }
            int offset = property.offset(state.get(property.property()));
            if (offset == -1) {
                throw new IllegalArgumentException("Invalid value for property " + property.property().name());
            }
            index += offset;
        }
        return index;
    }

    @Override
    int updateIndexOrInvalid(int currentIndex, Property<?> property, Object oldValue, Object newValue) {
        if (currentIndex < 0 || currentIndex >= size()) {
            return -1;
        }
        for (PropertyEntry entry : properties) {
            if (entry.property() == property) {
                int oldOffset = entry.offset(oldValue);
                int newOffset = entry.offset(newValue);
                return oldOffset == -1 || newOffset == -1 ? -1 : currentIndex - oldOffset + newOffset;
            }
        }
        return -1;
    }
}
