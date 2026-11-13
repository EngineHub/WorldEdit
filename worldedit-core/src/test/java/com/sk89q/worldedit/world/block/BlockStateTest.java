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

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.test.TestBlockRegistry;
import com.sk89q.worldedit.util.test.TestRegistries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Block states with real properties")
class BlockStateTest extends BaseWorldEditTest {

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    static Stream<String> blockIds() {
        return TestBlockRegistry.blockIds().stream();
    }

    private static BlockType type(String id) {
        return Objects.requireNonNull(BlockType.REGISTRY.get(id), id);
    }

    @SuppressWarnings("unchecked")
    private static <V> Property<V> property(BlockType type, String name) {
        return (Property<V>) type.getPropertyMap().get(name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void stateCountIsProductOfPropertyValueCounts(String id) {
        BlockType type = type(id);
        long expected = 1;
        for (Property<?> property : type.getProperties()) {
            expected *= property.values().size();
        }
        assertEquals(expected, type.getAllStates().size());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void everyStateIsDistinctAndComplete(String id) {
        BlockType type = type(id);
        Set<Map<Property<?>, Object>> seen = new HashSet<>();
        for (BlockState state : type.getAllStates()) {
            assertEquals(type, state.getBlockType());
            assertEquals(type.getProperties().size(), state.getStates().size());
            for (Property<?> property : type.getProperties()) {
                assertTrue(property.values().contains(state.getState(property)),
                    () -> state + " has an invalid value for " + property.name());
            }
            assertTrue(seen.add(state.getStates()), () -> "duplicate state " + state);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void statesCanBeLookedUpByTheirProperties(String id) {
        BlockType type = type(id);
        for (BlockState state : type.getAllStates()) {
            assertSame(state, type.getState(new HashMap<>(state.getStates())));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void defaultStateUsesFirstValueOfEachProperty(String id) {
        BlockType type = type(id);
        BlockState defaultState = type.getDefaultState();
        assertSame(type.getAllStates().getFirst(), defaultState);
        for (Property<?> property : type.getProperties()) {
            assertEquals(property.values().getFirst(), defaultState.getState(property), property.name());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void withChangesOnlyTheGivenProperty(String id) {
        BlockType type = type(id);
        for (BlockState base : type.getAllStates()) {
            for (Property<?> property : type.getProperties()) {
                assertWithOnlyChanges(base, property);
            }
        }
    }

    private static <V> void assertWithOnlyChanges(BlockState base, Property<V> property) {
        for (V value : property.values()) {
            BlockState changed = base.with(property, value);
            assertEquals(value, changed.getState(property), () -> base + " with " + property.name() + "=" + value);
            for (Property<?> other : base.getBlockType().getProperties()) {
                if (!other.equals(property)) {
                    assertEquals(base.getState(other), changed.getState(other),
                        () -> base + " with " + property.name() + "=" + value + " changed " + other.name());
                }
            }
            // Setting a property back to its original value restores the original state
            assertSame(base, changed.with(property, base.getState(property)));
        }
    }

    @Test
    void withTheCurrentValueReturnsTheSameState() {
        BlockState state = type("minecraft:oak_stairs").getDefaultState();
        Property<Object> half = property(state.getBlockType(), "half");
        assertSame(state, state.with(half, state.getState(half)));
    }

    @Test
    void withIgnoresValuesAndPropertiesThatDoNotExist() {
        BlockState state = type("minecraft:oak_stairs").getDefaultState();
        Property<Object> half = property(state.getBlockType(), "half");
        Property<Object> axis = property(type("minecraft:oak_log"), "axis");

        assertSame(state, state.with(half, "sideways"), "a value the property does not have");
        assertSame(state, state.with(axis, "x"), "a property of another block");
    }

    @Test
    void propertiesOfDifferentTypesAreAvailable() {
        BlockType wire = type("minecraft:redstone_wire");
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            property(wire, "power").values());
        assertEquals(List.of(true, false), property(type("minecraft:lever"), "powered").values());
        assertEquals(List.of("compare", "subtract"), property(type("minecraft:comparator"), "mode").values());
        assertEquals(4, property(type("minecraft:repeater"), "delay").values().size());
    }

    @Test
    void blocksWithoutPropertiesHaveASingleState() {
        BlockType stone = type("minecraft:stone");
        assertEquals(1, stone.getAllStates().size());
        assertTrue(stone.getProperties().isEmpty());
    }

    @Test
    void unknownPropertyNamesAreRejected() {
        BlockType stairs = type("minecraft:oak_stairs");
        assertThrows(IllegalArgumentException.class, () -> stairs.getProperty("axis"));
    }

    @Nested
    @DisplayName("A fuzzy block state")
    class Fuzzy {

        @Test
        void withoutPropertiesMatchesEveryStateOfItsType() {
            BlockType stairs = type("minecraft:oak_stairs");
            FuzzyBlockState any = stairs.getFuzzyMatcher();
            for (BlockState state : stairs.getAllStates()) {
                assertTrue(state.equalsFuzzy(any), () -> state + " should match " + any);
                assertTrue(any.equalsFuzzy(state), () -> any + " should match " + state);
            }
            assertFalse(type("minecraft:stone_stairs").getDefaultState().equalsFuzzy(any));
        }

        @Test
        void onlyMatchesStatesWithTheRequestedValues() {
            BlockType stairs = type("minecraft:oak_stairs");
            Property<Object> facing = property(stairs, "facing");
            Property<Object> half = property(stairs, "half");
            Object north = facing.values().stream().filter(v -> v.toString().equals("NORTH")).findFirst().orElseThrow();

            FuzzyBlockState fuzzy = FuzzyBlockState.builder()
                .type(stairs)
                .withProperty(facing, north)
                .withProperty(half, "top")
                .build();

            long matches = 0;
            for (BlockState state : stairs.getAllStates()) {
                boolean expected = north.equals(state.getState(facing)) && "top".equals(state.getState(half));
                assertEquals(expected, state.equalsFuzzy(fuzzy), state::toString);
                if (expected) {
                    matches++;
                }
            }
            // 5 shapes x 2 waterlogged
            assertEquals(10, matches);
        }

        @Test
        void equalityOnlyConsidersTheRequestedProperties() {
            BlockType chest = type("minecraft:chest");
            Property<Object> chestType = property(chest, "type");
            FuzzyBlockState left = FuzzyBlockState.builder()
                .type(chest)
                .withProperty(chestType, "left")
                .build();
            for (BlockState state : chest.getAllStates()) {
                assertEquals("left".equals(state.getState(chestType)), state.equals(left), state::toString);
            }
        }

        @Test
        void fullStateFillsMissingPropertiesWithDefaults() {
            BlockType door = type("minecraft:oak_door");
            Property<Object> hinge = property(door, "hinge");
            FuzzyBlockState fuzzy = FuzzyBlockState.builder()
                .type(door)
                .withProperty(hinge, "right")
                .build();

            BlockState full = fuzzy.getFullState();
            assertEquals("right", full.getState(hinge));
            assertSame(full, fuzzy.toImmutableState());
            for (Property<?> property : door.getProperties()) {
                if (!property.equals(hinge)) {
                    assertEquals(door.getDefaultState().getState(property), full.getState(property), property.name());
                }
            }
        }

        @Test
        void builderWithoutPropertiesReturnsTheSharedMatcher() {
            BlockType torch = type("minecraft:redstone_torch");
            assertSame(torch.getFuzzyMatcher(), FuzzyBlockState.builder().type(torch).build());
            assertSame(torch.getFuzzyMatcher(), FuzzyBlockState.builder().type(torch.getDefaultState()).build());
        }

        @Test
        void builderRejectsPropertiesOfOtherBlocks() {
            BlockType stairs = type("minecraft:oak_stairs");
            Property<Object> axis = property(type("minecraft:oak_log"), "axis");
            FuzzyBlockState.Builder builder = FuzzyBlockState.builder().type(stairs);
            assertThrows(IllegalArgumentException.class, () -> builder.withProperty(axis, "x"));
        }

        @Test
        void builderRequiresTypeBeforeProperties() {
            Property<Object> axis = property(type("minecraft:oak_log"), "axis");
            FuzzyBlockState.Builder builder = FuzzyBlockState.builder();
            assertThrows(NullPointerException.class, () -> builder.withProperty(axis, "x"));
            assertThrows(NullPointerException.class, builder::build);
        }

        @Test
        void builderCanBeReset() {
            BlockType log = type("minecraft:oak_log");
            FuzzyBlockState.Builder builder = FuzzyBlockState.builder()
                .type(log)
                .withProperty(property(log, "axis"), "x");
            builder.reset();
            assertThrows(NullPointerException.class, builder::build);
        }

        @Test
        void withDoesNothing() {
            BlockType log = type("minecraft:oak_log");
            FuzzyBlockState fuzzy = FuzzyBlockState.builder()
                .type(log)
                .withProperty(property(log, "axis"), "x")
                .build();
            assertSame(fuzzy, fuzzy.with(property(log, "axis"), "z"));
        }
    }
}
