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

package com.sk89q.worldedit.world.block;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.sk89q.worldedit.registry.state.Property;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An immutable state list whose property strides map directly to canonical states.
 */
// BlockState maps are immutable after construction, but Error Prone cannot infer that here.
@SuppressWarnings("Immutable")
final class DefaultBlockTypeStateList extends BlockTypeStateList {

    private static final class PropertyEntry {
        private final Property<?> property;
        private final ImmutableList<?> values;
        private final int stride;

        private PropertyEntry(Property<?> property, ImmutableList<?> values, int stride) {
            this.property = property;
            this.values = values;
            this.stride = stride;
        }

        private int getOffsetForValueOrInvalid(Object value) {
            int valueIndex = values.indexOf(value);
            return valueIndex == -1 ? -1 : valueIndex * stride;
        }
    }

    /*
     * The first property's values vary fastest. For properties [color: 2, shape: 3],
     * the indexes are color=0/shape=0 -> 0, color=1/shape=0 -> 1,
     * color=0/shape=1 -> 2, and color=1/shape=2 -> 5.
     */
    private final ImmutableList<PropertyEntry> propertyEntries;
    private final ImmutableList<BlockState> states;

    DefaultBlockTypeStateList(BlockType blockType) {
        List<Property<?>> properties = new ArrayList<>(blockType.getProperties());
        // Property map iteration order is platform-defined in 7.2.15. A stable order
        // keeps the state indexes deterministic across registrations and JVM runs.
        properties.sort(Comparator.comparing(Property::getName));

        ImmutableList.Builder<PropertyEntry> entriesBuilder =
            ImmutableList.builderWithExpectedSize(properties.size());
        int totalStates = 1;
        for (Property<?> property : properties) {
            ImmutableList<?> values = ImmutableList.copyOf(property.getValues());
            entriesBuilder.add(new PropertyEntry(property, values, totalStates));
            totalStates = Math.multiplyExact(totalStates, values.size());
        }
        this.propertyEntries = entriesBuilder.build();
        this.states = createStates(totalStates, blockType);
    }

    private ImmutableList<BlockState> createStates(int totalStates, BlockType blockType) {
        Property<?>[] properties = new Property<?>[propertyEntries.size()];
        Object[] currentValues = new Object[propertyEntries.size()];
        int[] valueIndexes = new int[propertyEntries.size()];
        for (int i = 0; i < propertyEntries.size(); i++) {
            PropertyEntry entry = propertyEntries.get(i);
            properties[i] = entry.property;
            currentValues[i] = entry.values.get(0);
        }

        ImmutableList.Builder<BlockState> statesBuilder =
            ImmutableList.builderWithExpectedSize(totalStates);
        for (int i = 0; i < totalStates; i++) {
            Map<Property<?>, Object> values = Object2ObjectMaps.unmodifiable(
                new Object2ObjectArrayMap<>(properties, currentValues.clone())
            );
            statesBuilder.add(new BlockState(blockType, values, i));

            if (i + 1 < totalStates) {
                advanceValues(valueIndexes, currentValues);
            }
        }
        return statesBuilder.build();
    }

    private void advanceValues(int[] valueIndexes, Object[] currentValues) {
        int propertyIndex = 0;
        while (propertyIndex < propertyEntries.size()) {
            PropertyEntry entry = propertyEntries.get(propertyIndex);
            int nextValueIndex = valueIndexes[propertyIndex] + 1;
            if (nextValueIndex < entry.values.size()) {
                valueIndexes[propertyIndex] = nextValueIndex;
                currentValues[propertyIndex] = entry.values.get(nextValueIndex);
                return;
            }

            valueIndexes[propertyIndex] = 0;
            currentValues[propertyIndex] = entry.values.get(0);
            propertyIndex++;
        }
        throw new IllegalStateException("Tried to increment past the last block property");
    }

    @Override
    public int size() {
        return states.size();
    }

    @Override
    public BlockState get(int index) {
        return states.get(index);
    }

    @Override
    public int calculateIndex(Map<Property<?>, ?> state) {
        if (state.size() != propertyEntries.size()) {
            throw new IllegalArgumentException(getDetailedPropertyMismatchException(state));
        }

        int index = 0;
        for (PropertyEntry entry : propertyEntries) {
            Object value = state.get(entry.property);
            if (value == null) {
                if (!state.containsKey(entry.property)) {
                    throw new IllegalArgumentException(getDetailedPropertyMismatchException(state));
                }
                throw new IllegalArgumentException("Null value for property " + entry.property.getName());
            }

            int offset = entry.getOffsetForValueOrInvalid(value);
            if (offset == -1) {
                throw new IllegalArgumentException(
                    "Invalid value for property " + entry.property.getName() + ": " + value
                );
            }
            index += offset;
        }
        return index;
    }

    private String getDetailedPropertyMismatchException(Map<Property<?>, ?> state) {
        Set<Property<?>> missingProperties = new LinkedHashSet<>();
        for (PropertyEntry entry : propertyEntries) {
            if (!state.containsKey(entry.property)) {
                missingProperties.add(entry.property);
            }
        }

        Set<Property<?>> extraProperties = new LinkedHashSet<>();
        for (Property<?> property : state.keySet()) {
            boolean known = false;
            for (PropertyEntry entry : propertyEntries) {
                if (entry.property.equals(property)) {
                    known = true;
                    break;
                }
            }
            if (!known) {
                extraProperties.add(property);
            }
        }

        StringBuilder errorMessage = new StringBuilder("State has incorrect number of properties.");
        if (!missingProperties.isEmpty()) {
            errorMessage.append(" Missing properties: ").append(missingProperties).append('.');
        }
        if (!extraProperties.isEmpty()) {
            errorMessage.append(" Extra properties: ").append(extraProperties).append('.');
        }
        return errorMessage.toString();
    }

    @Override
    public int updateIndexOrInvalid(
        int currentIndex,
        Property<?> property,
        Object oldValue,
        Object newValue
    ) {
        if (currentIndex < 0 || currentIndex >= size()) {
            return -1;
        }

        for (PropertyEntry entry : propertyEntries) {
            // Property.equals is name-based in 7.2.15; retain the lookup semantics of
            // the former Guava Table when callers pass an equivalent property instance.
            if (entry.property.equals(property)) {
                int oldOffset = entry.getOffsetForValueOrInvalid(oldValue);
                int newOffset = entry.getOffsetForValueOrInvalid(newValue);
                if (oldOffset == -1 || newOffset == -1) {
                    return -1;
                }
                return currentIndex - oldOffset + newOffset;
            }
        }
        return -1;
    }
}
