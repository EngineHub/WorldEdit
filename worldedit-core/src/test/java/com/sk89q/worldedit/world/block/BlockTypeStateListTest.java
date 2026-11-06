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
import com.sk89q.worldedit.registry.state.AbstractProperty;
import com.sk89q.worldedit.registry.state.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockTypeStateListTest {

    private static final Property<Boolean> POWERED = new TestProperty<>("powered", Arrays.asList(false, true));
    private static final Property<String> FACING = new TestProperty<>(
        "facing", Arrays.asList("east", "north", "south", "west")
    );

    @Test
    void zeroPropertyBlockUsesOneImmutableCanonicalState() {
        TestBlockType type = new TestBlockType("test:empty");

        assertEquals(1, type.getAllStates().size());
        BlockState state = type.getDefaultState();
        assertSame(state, type.getAllStates().get(0));
        assertSame(state, type.getState(Collections.emptyMap()));
        assertSame(state, state.with(POWERED, true));
        assertThrows(UnsupportedOperationException.class, () -> type.getAllStates().clear());
        assertThrows(UnsupportedOperationException.class, () -> state.getStates().put(POWERED, true));
    }

    @Test
    void onePropertyTransitionsReturnCanonicalStatesAndRejectInvalidValues() {
        TestBlockType type = new TestBlockType("test:powered", POWERED);
        BlockState unpowered = type.getDefaultState();
        BlockState powered = type.getState(stateMap(POWERED, true));

        assertEquals(2, type.getAllStates().size());
        assertSame(powered, unpowered.with(POWERED, true));
        assertSame(unpowered, powered.with(POWERED, false));
        assertSame(powered, powered.with(POWERED, true));

        Property<Boolean> equivalentProperty = new TestProperty<>("powered", Arrays.asList(false, true));
        assertSame(powered, unpowered.with(equivalentProperty, true));

        Property<String> unsupportedProperty = new TestProperty<>("mode", Arrays.asList("on", "off"));
        assertSame(unpowered, unpowered.with(unsupportedProperty, "on"));
    }

    @Test
    void multiplePropertiesUseStridesAndRetainForgeDefaultValues() {
        TestBlockType type = new TestBlockType(
            "test:oriented",
            state -> state.with(FACING, "north"),
            FACING,
            POWERED
        );

        BlockState defaultState = type.getState(stateMap(FACING, "north", POWERED, false));
        assertSame(defaultState, type.getDefaultState());

        BlockState transitioned = defaultState.with(POWERED, true).with(FACING, "west");
        BlockState lookedUp = type.getState(stateMap(FACING, "west", POWERED, true));
        assertSame(lookedUp, transitioned);
        assertSame(lookedUp, type.getState(lookedUp.getStates()));
        assertEquals(lookedUp, transitioned);
        assertEquals(lookedUp.hashCode(), transitioned.hashCode());
        assertThrows(UnsupportedOperationException.class, () -> lookedUp.getStates().clear());

        @SuppressWarnings("unchecked")
        Property<Object> facingAsObject = (Property<Object>) (Property<?>) FACING;
        assertSame(lookedUp, lookedUp.with(facingAsObject, "invalid"));
        assertSame(lookedUp, lookedUp.with(facingAsObject, null));
    }

    @Test
    void lookupReportsMissingExtraNullAndUnsupportedPropertyValues() {
        TestBlockType type = new TestBlockType("test:invalid", FACING, POWERED);

        IllegalArgumentException missing = assertThrows(
            IllegalArgumentException.class,
            () -> type.getState(stateMap(POWERED, false))
        );
        assertTrue(missing.getMessage().contains("Missing properties"));

        Map<Property<?>, Object> extraState = new LinkedHashMap<>();
        extraState.put(FACING, "north");
        extraState.put(POWERED, false);
        extraState.put(new TestProperty<>("unknown", Collections.singletonList("value")), "value");
        IllegalArgumentException extra = assertThrows(
            IllegalArgumentException.class,
            () -> type.getState(extraState)
        );
        assertTrue(extra.getMessage().contains("Extra properties"));

        Map<Property<?>, Object> nullState = new HashMap<>();
        nullState.put(FACING, null);
        nullState.put(POWERED, false);
        IllegalArgumentException nullValue = assertThrows(
            IllegalArgumentException.class,
            () -> type.getState(nullState)
        );
        assertTrue(nullValue.getMessage().contains("Null value for property facing"));

        IllegalArgumentException unsupported = assertThrows(
            IllegalArgumentException.class,
            () -> type.getState(stateMap(FACING, "diagonal", POWERED, false))
        );
        assertTrue(unsupported.getMessage().contains("Invalid value for property facing"));
    }

    @Test
    void builtFuzzyStatesDoNotTrackLaterBuilderChanges() {
        TestBlockType type = new TestBlockType("test:fuzzy", POWERED);
        FuzzyBlockState.Builder builder = FuzzyBlockState.builder()
            .type(type)
            .withProperty(POWERED, true);
        FuzzyBlockState fuzzy = builder.build();

        builder.reset();

        assertEquals(Boolean.TRUE, fuzzy.getState(POWERED));
        assertSame(type.getState(stateMap(POWERED, true)), fuzzy.toImmutableState());
    }

    @Test
    void largePropertyProductsUseCheckedStateCounts() {
        Property<Integer> level = new TestProperty<>(
            "level", IntStream.range(0, 256).boxed().collect(java.util.stream.Collectors.toList())
        );
        TestBlockType type = new TestBlockType("test:many_states", level, POWERED, FACING);

        assertEquals(2_048, type.getAllStates().size());
        BlockState last = type.getState(stateMap(level, 255, POWERED, true, FACING, "west"));
        assertSame(type.getAllStates().get(2_047), last);

        List<Integer> overflowingValues = IntStream.range(0, 50_000)
            .boxed()
            .collect(java.util.stream.Collectors.toList());
        TestBlockType overflowingType = new TestBlockType(
            "test:too_many_states",
            new TestProperty<>("first", overflowingValues),
            new TestProperty<>("second", overflowingValues)
        );
        assertThrows(ArithmeticException.class, () -> new DefaultBlockTypeStateList(overflowingType));
    }

    private static final class TestBlockType extends BlockType {
        private final Map<String, Property<?>> propertyMap;

        private TestBlockType(String id, Property<?>... properties) {
            this(id, null, properties);
        }

        private TestBlockType(
            String id,
            Function<BlockState, BlockState> defaultValues,
            Property<?>... properties
        ) {
            super(id, defaultValues);
            Map<String, Property<?>> map = new LinkedHashMap<>();
            for (Property<?> property : properties) {
                map.put(property.getName(), property);
            }
            this.propertyMap = Collections.unmodifiableMap(map);
        }

        @Override
        public Map<String, ? extends Property<?>> getPropertyMap() {
            return propertyMap;
        }

        @Override
        public List<? extends Property<?>> getProperties() {
            return ImmutableList.copyOf(propertyMap.values());
        }
    }

    private static Map<Property<?>, Object> stateMap(Object... propertyValues) {
        Map<Property<?>, Object> state = new LinkedHashMap<>();
        for (int i = 0; i < propertyValues.length; i += 2) {
            state.put((Property<?>) propertyValues[i], propertyValues[i + 1]);
        }
        return state;
    }

    private static final class TestProperty<T> extends AbstractProperty<T> {
        private TestProperty(String name, List<T> values) {
            super(name, new ArrayList<>(values));
        }

        @Override
        public T getValueFor(String string) {
            for (T value : getValues()) {
                if (value.toString().equals(string)) {
                    return value;
                }
            }
            return null;
        }
    }
}
