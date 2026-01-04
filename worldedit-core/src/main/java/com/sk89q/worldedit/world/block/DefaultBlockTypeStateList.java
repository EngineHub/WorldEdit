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

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.sk89q.worldedit.registry.state.Property;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Collection;
import java.util.Map;

// Suppress Immutable: Properly annotating BlockState with Immutable is for the future
@SuppressWarnings("Immutable")
final class DefaultBlockTypeStateList extends BlockTypeStateList {
    /**
     * Pre-calculated information about a property.
     *
     * @param property the property
     * @param values the possible values, as an immutable list with reference equality semantics
     * @param stride the stride for this property in the state index calculation, i.e.
     *     how many states are represented by earlier properties in the list that we must skip for each index increment
     */
    @Immutable
    // Suppress Immutable: Properly annotating Property<?> with Immutable is for the future
    @SuppressWarnings("Immutable")
    private record PropertyEntry(
        Property<?> property,
        ImmutableList<?> values,
        int stride
    ) {
        int getOffsetForValueOrInvalid(Object value) {
            int valueIndex = values.indexOf(value);
            return valueIndex == -1 ? -1 : valueIndex * stride;
        }
    }

    /*
    This class assembles all block states into a single array.
    The order of the states is such that all the first property's values are iterated first,
    then the second property's values, and so on.
    This allows for fast index calculation by multiplying the value index by the stride for each property.
    For example, given [color: [red, green], shape: [cube, sphere, pyramid]]:
    The states would be ordered as:
    0: color=red, shape=cube (0 + 0*2)
    1: color=green, shape=cube (1 + 0*2)
    2: color=red, shape=sphere (0 + 1*2)
    3: color=green, shape=sphere (1 + 1*2)
    4: color=red, shape=pyramid (0 + 2*2)
    5: color=green, shape=pyramid (1 + 2*2)
     */

    private final ImmutableList<PropertyEntry> propertyEntries;
    private final ImmutableList<BlockState> states;

    DefaultBlockTypeStateList(BlockType blockType) {
        Collection<? extends Property<?>> properties = blockType.getProperties();
        ImmutableList.Builder<PropertyEntry> propertyEntriesBuilder =
            ImmutableList.builderWithExpectedSize(properties.size());
        int nextStride = 1;
        for (Property<?> property : properties) {
            propertyEntriesBuilder.add(new PropertyEntry(
                property,
                ImmutableList.copyOf(property.values()),
                nextStride
            ));
            nextStride = Math.multiplyExact(nextStride, property.values().size());
        }
        this.propertyEntries = propertyEntriesBuilder.build();
        this.states = createStates(nextStride, blockType);
    }

    private ImmutableList<BlockState> createStates(int totalStates, BlockType blockType) {
        int[] propertyValueCounts = new int[propertyEntries.size()];

        // We can share the propsArray across all states since it will never differ
        Property<?>[] propsArray = new Property<?>[propertyEntries.size()];
        for (int i = 0; i < propsArray.length; i++) {
            propsArray[i] = propertyEntries.get(i).property;
        }

        // Values array caches the current values for each property
        Object[] valuesArray = new Object[propertyEntries.size()];
        for (int i = 0; i < valuesArray.length; i++) {
            valuesArray[i] = propertyEntries.get(i).values.getFirst();
        }

        ImmutableList.Builder<BlockState> statesBuilder = ImmutableList.builderWithExpectedSize(totalStates);
        for (int i = 0; i < totalStates; i++) {
            // Create the BlockState
            statesBuilder.add(new BlockState(
                blockType,
                Object2ObjectMaps.unmodifiable(
                    // No need to clone propsArray, but we need a new valuesArray each time
                    new Object2ObjectArrayMap<>(propsArray, valuesArray.clone())
                ),
                i
            ));

            if (i + 1 >= totalStates) {
                break;
            }
            prepareNextValueInSlot(propertyValueCounts, valuesArray, 0);
        }
        return statesBuilder.build();
    }

    private void prepareNextValueInSlot(int[] propertyValueCounts, Object[] valuesArray, int index) {
        PropertyEntry entry = propertyEntries.get(index);
        propertyValueCounts[index]++;
        if (propertyValueCounts[index] >= entry.values.size()) {
            // Reset this property and increment the next one
            propertyValueCounts[index] = 0;
            valuesArray[index] = entry.values.getFirst();
            Verify.verify(index + 1 < propertyValueCounts.length, "Tried to increment past last property");
            prepareNextValueInSlot(propertyValueCounts, valuesArray, index + 1);
        } else {
            // Set the next value for this property
            valuesArray[index] = entry.values.get(propertyValueCounts[index]);
        }
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
        int index = 0;
        for (PropertyEntry entry : propertyEntries) {
            Object value = state.get(entry.property);
            if (value == null) {
                throw new IllegalArgumentException("Missing or null value for property " + entry.property.name());
            }
            int offset = entry.getOffsetForValueOrInvalid(value);
            if (offset == -1) {
                throw new IllegalArgumentException("Invalid value for property " + entry.property.name() + ": " + value);
            }
            index += offset;
        }
        return index;
    }

    @Override
    public int updateIndexOrInvalid(int currentIndex, Property<?> property, Object oldValue, Object newValue) {
        if (currentIndex < 0 || currentIndex >= size()) {
            return -1;
        }
        for (PropertyEntry entry : propertyEntries) {
            if (entry.property == property) {
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
