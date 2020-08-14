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

package com.sk89q.worldedit.util.collection;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.test.VariedVectorGenerator;
import com.sk89q.worldedit.util.test.VariedVectors;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.BundledRegistries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("An ordered block map")
class BlockMapTest {

    private static final Platform MOCKED_PLATFORM = mock(Platform.class);

    @BeforeAll
    static void setupFakePlatform() {
        when(MOCKED_PLATFORM.getRegistries()).thenReturn(new BundledRegistries() {
        });
        when(MOCKED_PLATFORM.getCapabilities()).thenReturn(ImmutableMap.of(
            Capability.WORLD_EDITING, Preference.PREFERRED,
            Capability.GAME_HOOKS, Preference.PREFERRED
        ));
        PlatformManager platformManager = WorldEdit.getInstance().getPlatformManager();
        platformManager.register(MOCKED_PLATFORM);

        registerBlock("minecraft:air");
        registerBlock("minecraft:oak_wood");
    }

    @AfterAll
    static void tearDownFakePlatform() throws Exception {
        WorldEdit.getInstance().getPlatformManager().unregister(MOCKED_PLATFORM);
        Field map = Registry.class.getDeclaredField("map");
        map.setAccessible(true);
        ((Map<?, ?>) map.get(BlockType.REGISTRY)).clear();
    }

    private static void registerBlock(String id) {
        BlockType.REGISTRY.register(id, new BlockType(id));
    }

    @Mock
    private BiFunction<? super BlockVector3, ? super BaseBlock, ? extends BaseBlock> biFunction;
    @Mock
    private BiFunction<? super BaseBlock, ? super BaseBlock, ? extends BaseBlock> mergeFunction;
    @Mock
    private BiConsumer<? super BlockVector3, ? super BaseBlock> biConsumer;

    private final BaseBlock air = checkNotNull(BlockTypes.AIR).getDefaultState().toBaseBlock();
    private final BaseBlock oakWood = checkNotNull(BlockTypes.OAK_WOOD).getDefaultState().toBaseBlock();

    private final BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void tearDown() {
        map.clear();
    }

    @Test
    @DisplayName("throws ClassCastException if invalid argument to get")
    void throwsFromGetOnInvalidArgument() {
        assertThrows(ClassCastException.class, () -> map.get(""));
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("is empty")
        void isEmpty() {
            assertEquals(0, map.size());
        }

        @Test
        @DisplayName("is equal to another empty map")
        void isEqualToEmptyMap() {
            assertEquals(ImmutableMap.of(), map);
        }

        @Test
        @DisplayName("has the same hashCode as another empty map")
        void isHashCodeEqualToEmptyMap() {
            assertEquals(ImmutableMap.of().hashCode(), map.hashCode());
        }

        @Test
        @DisplayName("returns `null` from get")
        void returnsNullFromGet() {
            assertNull(map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("contains no keys")
        void containsNoKeys() {
            assertEquals(0, map.keySet().size());
            assertFalse(map.containsKey(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("contains no values")
        void containsNoValues() {
            assertEquals(0, map.values().size());
            assertFalse(map.containsValue(air));
        }

        @Test
        @DisplayName("contains no entries")
        void containsNoEntries() {
            assertEquals(0, map.entrySet().size());
        }

        @Test
        @DisplayName("returns the default value from getOrDefault")
        void returnsDefaultFromGetOrDefault() {
            assertEquals(air, map.getOrDefault(BlockVector3.ZERO, air));
        }

        @Test
        @DisplayName("never calls the forEach action")
        void neverCallsForEachAction() {
            map.forEach(biConsumer);
            verifyNoMoreInteractions(biConsumer);
        }

        @Test
        @DisplayName("never calls the replaceAll function")
        void neverCallsReplaceAllFunction() {
            map.replaceAll(biFunction);
            verifyNoMoreInteractions(biFunction);
        }

        @Test
        @DisplayName("inserts on putIfAbsent")
        void insertOnPutIfAbsent() {
            assertNull(map.putIfAbsent(BlockVector3.ZERO, air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("remove(key) returns null")
        void removeKeyReturnsNull() {
            assertNull(map.remove(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("remove(key, value) returns false")
        void removeKeyValueReturnsFalse() {
            assertFalse(map.remove(BlockVector3.ZERO, air));
        }

        @Test
        @DisplayName("does nothing on replace")
        void doesNothingOnReplace() {
            assertNull(map.replace(BlockVector3.ZERO, air));
            assertEquals(0, map.size());
            assertFalse(map.replace(BlockVector3.ZERO, null, air));
            assertEquals(0, map.size());
        }

        @Test
        @DisplayName("inserts on computeIfAbsent")
        void insertOnComputeIfAbsent() {
            assertEquals(air, map.computeIfAbsent(BlockVector3.ZERO, k -> air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("inserts on compute")
        void insertOnCompute() {
            assertEquals(air, map.compute(BlockVector3.ZERO, (k, v) -> air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("does nothing on computeIfPresent")
        void doesNothingOnComputeIfPresent() {
            assertNull(map.computeIfPresent(BlockVector3.ZERO, (k, v) -> air));
            assertEquals(0, map.size());
        }

        @Test
        @DisplayName("inserts on merge, without calling merge function")
        void insertsOnMerge() {
            assertEquals(air, map.merge(BlockVector3.ZERO, air, mergeFunction));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
            verifyNoMoreInteractions(mergeFunction);
        }

    }

    @Nested
    @DisplayName("after having an entry added")
    class AfterEntryAdded {

        // Note: This section of tests would really benefit from
        // being able to parameterize classes. It's not part of JUnit
        // yet though: https://github.com/junit-team/junit5/issues/878

        @VariedVectors.Test
        @DisplayName("has a size of one")
        void hasSizeOne(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(1, map.size());
        }

        @VariedVectors.Test
        @DisplayName("is equal to another map with the same entry")
        void isEqualToSimilarMap(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(ImmutableMap.of(vec, air), map);
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("is not equal to another map with a different key")
        void isNotEqualToDifferentKeyMap(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertNotEquals(ImmutableMap.of(nonMatch, air), map);
        }

        @VariedVectors.Test
        @DisplayName("is not equal to another map with a different value")
        void isNotEqualToDifferentValueMap(BlockVector3 vec) {
            map.put(vec, air);
            assertNotEquals(ImmutableMap.of(vec, oakWood), map);
        }

        @VariedVectors.Test
        @DisplayName("is not equal to an empty map")
        void isNotEqualToEmptyMap(BlockVector3 vec) {
            map.put(vec, air);
            assertNotEquals(ImmutableMap.of(), map);
        }

        @VariedVectors.Test
        @DisplayName("has the same hashCode as another map with the same entry")
        void isHashCodeEqualToSimilarMap(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(ImmutableMap.of(vec, air).hashCode(), map.hashCode());
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("has a different hashCode from another map with a different key")
        void isHashCodeNotEqualToDifferentKeyMap(BlockVector3 vec, BlockVector3 nonMatch) {
            assumeFalse(vec.hashCode() == nonMatch.hashCode(),
                "Vectors have equivalent hashCodes, maps will too.");
            map.put(vec, air);
            assertNotEquals(ImmutableMap.of(nonMatch, air).hashCode(), map.hashCode());
        }

        @VariedVectors.Test
        @DisplayName("has a different hashCode from another map with a different value")
        void isHashCodeNotEqualToDifferentValueMap(BlockVector3 vec) {
            map.put(vec, air);
            assertNotEquals(ImmutableMap.of(vec, oakWood).hashCode(), map.hashCode());
        }

        @VariedVectors.Test
        @DisplayName("has a different hashCode from an empty map")
        void isHashCodeNotEqualToEmptyMap(BlockVector3 vec) {
            map.put(vec, air);
            assertNotEquals(ImmutableMap.of().hashCode(), map.hashCode());
        }

        @VariedVectors.Test
        @DisplayName("returns value from get")
        void returnsValueFromGet(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(air, map.get(vec));
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("returns `null` from get with different key")
        void returnsValueFromGet(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertNotEquals(air, map.get(nonMatch));
        }

        @VariedVectors.Test
        @DisplayName("contains the key")
        void containsTheKey(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(1, map.keySet().size());
            assertTrue(map.keySet().contains(vec));
            assertTrue(map.containsKey(vec));
        }

        @VariedVectors.Test
        @DisplayName("contains the value")
        void containsTheValue(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(1, map.values().size());
            assertTrue(map.values().contains(air));
            assertTrue(map.containsValue(air));
        }

        @VariedVectors.Test
        @DisplayName("contains the entry")
        void containsTheEntry(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(1, map.entrySet().size());
            assertEquals(new AbstractMap.SimpleImmutableEntry<>(vec, air), map.entrySet().iterator().next());
        }

        @VariedVectors.Test
        @DisplayName("returns the provided value from getOrDefault")
        void returnsProvidedFromGetOrDefault(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(air, map.getOrDefault(vec, oakWood));
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("returns the default value from getOrDefault with a different key")
        void returnsDefaultFromGetOrDefaultWrongKey(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertEquals(oakWood, map.getOrDefault(nonMatch, oakWood));
        }

        @VariedVectors.Test
        @DisplayName("calls the forEach action once")
        void neverCallsForEachAction(BlockVector3 vec) {
            map.put(vec, air);
            map.forEach(biConsumer);
            verify(biConsumer).accept(vec, air);
            verifyNoMoreInteractions(biConsumer);
        }

        @VariedVectors.Test
        @DisplayName("replaces value using replaceAll")
        void neverCallsReplaceAllFunction(BlockVector3 vec) {
            map.put(vec, air);
            map.replaceAll((v, b) -> oakWood);
            assertEquals(oakWood, map.get(vec));
        }

        @VariedVectors.Test
        @DisplayName("does not insert on `putIfAbsent`")
        void noInsertOnPutIfAbsent(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(air, map.putIfAbsent(vec, oakWood));
            assertEquals(1, map.size());
            assertEquals(air, map.get(vec));
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("inserts on `putIfAbsent` to a different key")
        void insertOnPutIfAbsentDifferentKey(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertNull(map.putIfAbsent(nonMatch, oakWood));
            assertEquals(2, map.size());
            assertEquals(air, map.get(vec));
            assertEquals(oakWood, map.get(nonMatch));
        }

        @VariedVectors.Test
        @DisplayName("remove(key) returns the old value")
        void removeKeyReturnsOldValue(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(air, map.remove(vec));
            assertEquals(0, map.size());
        }

        @VariedVectors.Test
        @DisplayName("keySet().remove(key) removes the entry from the map")
        void keySetRemovePassesThrough(BlockVector3 vec) {
            map.put(vec, air);
            assertTrue(map.keySet().remove(vec));
            assertEquals(0, map.size());
        }

        @VariedVectors.Test
        @DisplayName("entrySet().iterator().remove() removes the entry from the map")
        void entrySetIteratorRemovePassesThrough(BlockVector3 vec) {
            map.put(vec, air);
            Iterator<Map.Entry<BlockVector3, BaseBlock>> iterator = map.entrySet().iterator();
            assertTrue(iterator.hasNext());
            Map.Entry<BlockVector3, BaseBlock> entry = iterator.next();
            assertEquals(entry.getKey(), vec);
            iterator.remove();
            assertEquals(0, map.size());
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("remove(nonMatch) returns null")
        void removeNonMatchingKeyReturnsNull(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertNull(map.remove(nonMatch));
            assertEquals(1, map.size());
        }

        @VariedVectors.Test
        @DisplayName("remove(key, value) returns true")
        void removeKeyValueReturnsTrue(BlockVector3 vec) {
            map.put(vec, air);
            assertTrue(map.remove(vec, air));
            assertEquals(0, map.size());
        }

        @VariedVectors.Test
        @DisplayName("remove(key, value) returns false for wrong value")
        void removeKeyValueReturnsFalseWrongValue(BlockVector3 vec) {
            map.put(vec, air);
            assertFalse(map.remove(vec, oakWood));
            assertEquals(1, map.size());
        }

        @VariedVectors.Test
        @DisplayName("replaces value at key")
        void replacesValueAtKey(BlockVector3 vec) {
            map.put(vec, air);

            assertEquals(air, map.replace(vec, oakWood));
            assertEquals(1, map.size());
            assertEquals(oakWood, map.get(vec));

            assertTrue(map.replace(vec, oakWood, air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(vec));
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("does not replace value at different key")
        void doesNotReplaceAtDifferentKey(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);

            assertNull(map.replace(nonMatch, oakWood));
            assertEquals(1, map.size());
            assertEquals(air, map.get(vec));

            assertFalse(map.replace(nonMatch, air, oakWood));
            assertEquals(1, map.size());
            assertEquals(air, map.get(vec));
        }

        @VariedVectors.Test
        @DisplayName("does not insert on computeIfAbsent")
        void doesNotInsertComputeIfAbsent(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(air, map.computeIfAbsent(vec, k -> {
                assertEquals(vec, k);
                return oakWood;
            }));
            assertEquals(1, map.size());
            assertEquals(air, map.get(vec));
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("inserts on computeIfAbsent with different key")
        void insertsOnComputeIfAbsentDifferentKey(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertEquals(oakWood, map.computeIfAbsent(nonMatch, k -> {
                assertEquals(nonMatch, k);
                return oakWood;
            }));
            assertEquals(2, map.size());
            assertEquals(air, map.get(vec));
            assertEquals(oakWood, map.get(nonMatch));
        }

        @VariedVectors.Test
        @DisplayName("replaces on compute")
        void replaceOnCompute(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(oakWood, map.compute(vec, (k, v) -> {
                assertEquals(vec, k);
                assertEquals(air, v);
                return oakWood;
            }));
            assertEquals(1, map.size());
            assertEquals(oakWood, map.get(vec));
            assertNull(map.compute(vec, (k, v) -> null));
            assertEquals(0, map.size());
        }

        @VariedVectors.Test(provideNonMatching = true)
        @DisplayName("inserts on compute with different key")
        void insertOnComputeDifferentKey(BlockVector3 vec, BlockVector3 nonMatch) {
            map.put(vec, air);
            assertEquals(oakWood, map.compute(nonMatch, (k, v) -> {
                assertEquals(nonMatch, k);
                assertNull(v);
                return oakWood;
            }));
            assertEquals(2, map.size());
            assertEquals(air, map.get(vec));
            assertEquals(oakWood, map.get(nonMatch));
            assertNull(map.compute(nonMatch, (k, v) -> null));
            assertEquals(1, map.size());
            assertEquals(air, map.get(vec));
        }

        @VariedVectors.Test
        @DisplayName("replaces on computeIfPresent")
        void replacesOnComputeIfPresent(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(oakWood, map.computeIfPresent(vec, (k, v) -> {
                assertEquals(vec, k);
                assertEquals(air, v);
                return oakWood;
            }));
            assertEquals(1, map.size());
            assertEquals(oakWood, map.get(vec));
            assertNull(map.computeIfPresent(vec, (k, v) -> null));
            assertEquals(0, map.size());
        }

        @VariedVectors.Test
        @DisplayName("inserts on merge, with call to merge function")
        void insertsOnMerge(BlockVector3 vec) {
            map.put(vec, air);
            assertEquals(oakWood, map.merge(vec, oakWood, (o, n) -> {
                assertEquals(air, o);
                assertEquals(oakWood, n);
                return n;
            }));
            assertEquals(1, map.size());
            assertEquals(oakWood, map.get(vec));
        }

    }

    @Test
    @DisplayName("contains all inserted vectors")
    void containsAllInsertedVectors() {
        Set<BlockVector3> allVectors = new VariedVectorGenerator()
            .makeVectorsStream()
            .collect(Collectors.toSet());
        for (BlockVector3 vec : allVectors) {
            map.put(vec, air);
        }
        assertEquals(allVectors.size(), map.size());
        assertEquals(allVectors, map.keySet());
        for (Map.Entry<BlockVector3, BaseBlock> entry : map.entrySet()) {
            assertTrue(allVectors.contains(entry.getKey()));
            assertEquals(air, entry.getValue());
        }
    }

}
