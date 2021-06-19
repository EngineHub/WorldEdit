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
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.test.ResourceLockKeys;
import com.sk89q.worldedit.util.test.VariedVectorGenerator;
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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

@Execution(ExecutionMode.CONCURRENT)
@ResourceLock(ResourceLockKeys.WORLDEDIT_PLATFORM)
@DisplayName("An ordered block map")
class BlockMapTest {

    private static final Platform MOCKED_PLATFORM = mock(Platform.class);

    @BeforeAll
    static void setupFakePlatform() {
        when(MOCKED_PLATFORM.getRegistries()).thenReturn(new BundledRegistries() {
        });
        when(MOCKED_PLATFORM.getCapabilities()).thenReturn(
            Stream.of(Capability.values())
                .collect(Collectors.toMap(Function.identity(), __ -> Preference.NORMAL))
        );
        PlatformManager platformManager = WorldEdit.getInstance().getPlatformManager();
        platformManager.register(MOCKED_PLATFORM);
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());

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

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("throws ClassCastException if invalid argument to get")
    void throwsFromGetOnInvalidArgument() {
        BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
        assertThrows(ClassCastException.class, () -> map.get(""));
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("is empty")
        void isEmpty() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(0, map.size());
        }

        @Test
        @DisplayName("is equal to another empty map")
        void isEqualToEmptyMap() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(ImmutableMap.of(), map);
        }

        @Test
        @DisplayName("has the same hashCode as another empty map")
        void isHashCodeEqualToEmptyMap() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(ImmutableMap.of().hashCode(), map.hashCode());
        }

        @Test
        @DisplayName("returns `null` from get")
        void returnsNullFromGet() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertNull(map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("contains no keys")
        void containsNoKeys() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(0, map.keySet().size());
            assertFalse(map.containsKey(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("contains no values")
        void containsNoValues() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(0, map.values().size());
            assertFalse(map.containsValue(air));
        }

        @Test
        @DisplayName("contains no entries")
        void containsNoEntries() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(0, map.entrySet().size());
        }

        @Test
        @DisplayName("returns the default value from getOrDefault")
        void returnsDefaultFromGetOrDefault() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(air, map.getOrDefault(BlockVector3.ZERO, air));
        }

        @Test
        @DisplayName("never calls the forEach action")
        void neverCallsForEachAction() throws Exception {
            try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.forEach(biConsumer);
                verifyNoMoreInteractions(biConsumer);
            }
        }

        @Test
        @DisplayName("never calls the replaceAll function")
        void neverCallsReplaceAllFunction() throws Exception {
            try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.replaceAll(biFunction);
                verifyNoMoreInteractions(biFunction);
            }
        }

        @Test
        @DisplayName("inserts on putIfAbsent")
        void insertOnPutIfAbsent() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertNull(map.putIfAbsent(BlockVector3.ZERO, air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("remove(key) returns null")
        void removeKeyReturnsNull() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertNull(map.remove(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("remove(key, value) returns false")
        void removeKeyValueReturnsFalse() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertFalse(map.remove(BlockVector3.ZERO, air));
        }

        @Test
        @DisplayName("does nothing on replace")
        void doesNothingOnReplace() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertNull(map.replace(BlockVector3.ZERO, air));
            assertEquals(0, map.size());
            assertFalse(map.replace(BlockVector3.ZERO, null, air));
            assertEquals(0, map.size());
        }

        @Test
        @DisplayName("inserts on computeIfAbsent")
        void insertOnComputeIfAbsent() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(air, map.computeIfAbsent(BlockVector3.ZERO, k -> air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("inserts on compute")
        void insertOnCompute() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertEquals(air, map.compute(BlockVector3.ZERO, (k, v) -> air));
            assertEquals(1, map.size());
            assertEquals(air, map.get(BlockVector3.ZERO));
        }

        @Test
        @DisplayName("does nothing on computeIfPresent")
        void doesNothingOnComputeIfPresent() {
            BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
            assertNull(map.computeIfPresent(BlockVector3.ZERO, (k, v) -> air));
            assertEquals(0, map.size());
        }

        @Test
        @DisplayName("inserts on merge, without calling merge function")
        void insertsOnMerge() throws Exception {
            try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                assertEquals(air, map.merge(BlockVector3.ZERO, air, mergeFunction));
                assertEquals(1, map.size());
                assertEquals(air, map.get(BlockVector3.ZERO));
                verifyNoMoreInteractions(mergeFunction);
            }
        }

    }

    @Nested
    @DisplayName("after having an entry added")
    class AfterEntryAdded {

        private final VariedVectorGenerator generator = new VariedVectorGenerator();

        @Test
        @DisplayName("has a size of one")
        void hasSizeOne() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(1, map.size());
            });
        }

        @Test
        @DisplayName("is equal to another map with the same entry")
        void isEqualToSimilarMap() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(ImmutableMap.of(vec, air), map);
            });
        }

        @Test
        @DisplayName("is not equal to another map with a different key")
        void isNotEqualToDifferentKeyMap() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNotEquals(ImmutableMap.of(nonMatch, air), map);
            });
        }

        @Test
        @DisplayName("is not equal to another map with a different value")
        void isNotEqualToDifferentValueMap() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNotEquals(ImmutableMap.of(vec, oakWood), map);
            });
        }

        @Test
        @DisplayName("is not equal to an empty map")
        void isNotEqualToEmptyMap() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNotEquals(ImmutableMap.of(), map);
            });
        }

        @Test
        @DisplayName("has the same hashCode as another map with the same entry")
        void isHashCodeEqualToSimilarMap() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(ImmutableMap.of(vec, air).hashCode(), map.hashCode());
            });
        }

        @Test
        @DisplayName("has a different hashCode from another map with a different key")
        void isHashCodeNotEqualToDifferentKeyMap() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                assumeFalse(vec.hashCode() == nonMatch.hashCode(),
                    "Vectors have equivalent hashCodes, maps will too.");
                map.put(vec, air);
                assertNotEquals(ImmutableMap.of(nonMatch, air).hashCode(), map.hashCode());
            });
        }

        @Test
        @DisplayName("has a different hashCode from another map with a different value")
        void isHashCodeNotEqualToDifferentValueMap() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNotEquals(ImmutableMap.of(vec, oakWood).hashCode(), map.hashCode());
            });
        }

        @Test
        @DisplayName("has a different hashCode from an empty map")
        void isHashCodeNotEqualToEmptyMap() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNotEquals(ImmutableMap.of().hashCode(), map.hashCode());
            });
        }

        @Test
        @DisplayName("returns value from get")
        void returnsValueFromGet() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(air, map.get(vec));
            });
        }

        @Test
        @DisplayName("returns `null` from get with different key")
        void returnsValueFromGetDifferentKey() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNotEquals(air, map.get(nonMatch));
            });
        }

        @Test
        @DisplayName("contains the key")
        void containsTheKey() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(1, map.keySet().size());
                assertTrue(map.keySet().contains(vec));
                assertTrue(map.containsKey(vec));
            });
        }

        @Test
        @DisplayName("contains the value")
        void containsTheValue() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(1, map.values().size());
                assertTrue(map.values().contains(air));
                assertTrue(map.containsValue(air));
            });
        }

        @Test
        @DisplayName("contains the entry")
        void containsTheEntry() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(1, map.entrySet().size());
                assertEquals(new AbstractMap.SimpleImmutableEntry<>(vec, air), map.entrySet().iterator().next());
            });
        }

        @Test
        @DisplayName("returns the provided value from getOrDefault")
        void returnsProvidedFromGetOrDefault() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(air, map.getOrDefault(vec, oakWood));
            });
        }

        @Test
        @DisplayName("returns the default value from getOrDefault with a different key")
        void returnsDefaultFromGetOrDefaultWrongKey() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(oakWood, map.getOrDefault(nonMatch, oakWood));
            });
        }

        @Test
        @DisplayName("calls the forEach action once")
        void neverCallsForEachAction() {
            generator.makeVectorsStream().sequential().forEach(vec -> {
                try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
                    BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                    map.put(vec, air);
                    map.forEach(biConsumer);
                    verify(biConsumer).accept(vec, air);
                    verifyNoMoreInteractions(biConsumer);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });
        }

        @Test
        @DisplayName("replaces value using replaceAll")
        void neverCallsReplaceAllFunction() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                map.replaceAll((v, b) -> oakWood);
                assertEquals(oakWood, map.get(vec));
            });
        }

        @Test
        @DisplayName("does not insert on `putIfAbsent`")
        void noInsertOnPutIfAbsent() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(air, map.putIfAbsent(vec, oakWood));
                assertEquals(1, map.size());
                assertEquals(air, map.get(vec));
            });
        }

        @Test
        @DisplayName("inserts on `putIfAbsent` to a different key")
        void insertOnPutIfAbsentDifferentKey() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNull(map.putIfAbsent(nonMatch, oakWood));
                assertEquals(2, map.size());
                assertEquals(air, map.get(vec));
                assertEquals(oakWood, map.get(nonMatch));
            });
        }

        @Test
        @DisplayName("remove(key) returns the old value")
        void removeKeyReturnsOldValue() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(air, map.remove(vec));
                assertEquals(0, map.size());
            });
        }

        @Test
        @DisplayName("keySet().remove(key) removes the entry from the map")
        void keySetRemovePassesThrough() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertTrue(map.keySet().remove(vec));
                assertEquals(0, map.size());
            });
        }

        @Test
        @DisplayName("entrySet().iterator().remove() removes the entry from the map")
        void entrySetIteratorRemovePassesThrough() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                Iterator<Map.Entry<BlockVector3, BaseBlock>> iterator = map.entrySet().iterator();
                assertTrue(iterator.hasNext());
                Map.Entry<BlockVector3, BaseBlock> entry = iterator.next();
                assertEquals(entry.getKey(), vec);
                iterator.remove();
                assertEquals(0, map.size());
            });
        }

        @Test
        @DisplayName("remove(nonMatch) returns null")
        void removeNonMatchingKeyReturnsNull() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertNull(map.remove(nonMatch));
                assertEquals(1, map.size());
            });
        }

        @Test
        @DisplayName("remove(key, value) returns true")
        void removeKeyValueReturnsTrue() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertTrue(map.remove(vec, air));
                assertEquals(0, map.size());
            });
        }

        @Test
        @DisplayName("remove(key, value) returns false for wrong value")
        void removeKeyValueReturnsFalseWrongValue() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertFalse(map.remove(vec, oakWood));
                assertEquals(1, map.size());
            });
        }

        @Test
        @DisplayName("replaces value at key")
        void replacesValueAtKey() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);

                assertEquals(air, map.replace(vec, oakWood));
                assertEquals(1, map.size());
                assertEquals(oakWood, map.get(vec));

                assertTrue(map.replace(vec, oakWood, air));
                assertEquals(1, map.size());
                assertEquals(air, map.get(vec));
            });
        }

        @Test
        @DisplayName("does not replace value at different key")
        void doesNotReplaceAtDifferentKey() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);

                assertNull(map.replace(nonMatch, oakWood));
                assertEquals(1, map.size());
                assertEquals(air, map.get(vec));

                assertFalse(map.replace(nonMatch, air, oakWood));
                assertEquals(1, map.size());
                assertEquals(air, map.get(vec));
            });
        }

        @Test
        @DisplayName("does not insert on computeIfAbsent")
        void doesNotInsertComputeIfAbsent() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(air, map.computeIfAbsent(vec, k -> {
                    assertEquals(vec, k);
                    return oakWood;
                }));
                assertEquals(1, map.size());
                assertEquals(air, map.get(vec));
            });
        }

        @Test
        @DisplayName("inserts on computeIfAbsent with different key")
        void insertsOnComputeIfAbsentDifferentKey() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(oakWood, map.computeIfAbsent(nonMatch, k -> {
                    assertEquals(nonMatch, k);
                    return oakWood;
                }));
                assertEquals(2, map.size());
                assertEquals(air, map.get(vec));
                assertEquals(oakWood, map.get(nonMatch));
            });
        }

        @Test
        @DisplayName("replaces on compute")
        void replaceOnCompute() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
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
            });
        }

        @Test
        @DisplayName("inserts on compute with different key")
        void insertOnComputeDifferentKey() {
            generator.makePairedVectorsStream().forEach(pair -> {
                BlockVector3 vec = pair.first;
                BlockVector3 nonMatch = pair.second;
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
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
            });
        }

        @Test
        @DisplayName("replaces on computeIfPresent")
        void replacesOnComputeIfPresent() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
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
            });
        }

        @Test
        @DisplayName("inserts on merge, with call to merge function")
        void insertsOnMerge() {
            generator.makeVectorsStream().forEach(vec -> {
                BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
                map.put(vec, air);
                assertEquals(oakWood, map.merge(vec, oakWood, (o, n) -> {
                    assertEquals(air, o);
                    assertEquals(oakWood, n);
                    return n;
                }));
                assertEquals(1, map.size());
                assertEquals(oakWood, map.get(vec));
            });
        }

    }

    @Test
    @DisplayName("contains all inserted vectors")
    void containsAllInsertedVectors() {
        BlockMap<BaseBlock> map = BlockMap.createForBaseBlock();
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
