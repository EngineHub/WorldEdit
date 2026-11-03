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

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("An int to block map")
class Int2BaseBlockMapTest extends BaseWorldEditTest {

    @BeforeAll
    static void setupFakePlatform() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void tearDownFakePlatform() {
        TestRegistries.unregisterBlocks();
    }

    private static BaseBlock withNbt(BlockType type, String value) {
        return type.getDefaultState().toBaseBlock(LinCompoundTag.builder().putString("dummy", value).build());
    }

    /**
     * Mixes blocks that are stored by state ID with blocks that carry NBT and
     * so have to be stored as objects.
     */
    private static BaseBlock[] blocks() {
        BlockType air = checkNotNull(BlockType.REGISTRY.get("minecraft:air"));
        BlockType wood = checkNotNull(BlockType.REGISTRY.get("minecraft:oak_wood"));
        BlockType chest = checkNotNull(BlockType.REGISTRY.get("minecraft:chest"));
        return new BaseBlock[]{
            air.getDefaultState().toBaseBlock(),
            wood.getDefaultState().toBaseBlock(),
            chest.getDefaultState().toBaseBlock(),
            withNbt(chest, "a"),
            withNbt(chest, "b"),
            withNbt(wood, "a"),
        };
    }

    private static Map<Integer, BaseBlock> snapshot(Int2ObjectMap<BaseBlock> map) {
        Map<Integer, BaseBlock> copy = new HashMap<>();
        for (Int2ObjectMap.Entry<BaseBlock> entry : map.int2ObjectEntrySet()) {
            assertNull(copy.put(entry.getIntKey(), entry.getValue()), "duplicate key in entry set");
        }
        return copy;
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6})
    @DisplayName("behaves like a HashMap under random operations")
    void behavesLikeHashMap(long seed) {
        Random random = new Random(seed);
        BaseBlock[] blocks = blocks();
        Int2BaseBlockMap map = new Int2BaseBlockMap();
        Map<Integer, BaseBlock> model = new HashMap<>();

        for (int i = 0; i < 4000; i++) {
            int key = random.nextInt(41) - 20;
            BaseBlock block = blocks[random.nextInt(blocks.length)];
            switch (random.nextInt(8)) {
                case 0, 1, 2 -> assertEquals(model.put(key, block), map.put(key, block), "put " + key);
                case 3 -> assertEquals(model.remove(key), map.remove(key), "remove " + key);
                case 4 -> assertEquals(model.get(key), map.get(key), "get " + key);
                case 5 -> {
                    assertEquals(model.containsKey(key), map.containsKey(key), "containsKey " + key);
                    assertEquals(model.containsValue(block), map.containsValue(block), "containsValue " + block);
                }
                case 6 -> {
                    int shift = random.nextInt(blocks.length);
                    map.replaceAll((k, v) -> blocks[Math.floorMod(indexOf(blocks, v) + shift + k, blocks.length)]);
                    model.replaceAll((k, v) -> blocks[Math.floorMod(indexOf(blocks, v) + shift + k, blocks.length)]);
                }
                default -> {
                    assertEquals(model.size(), map.size());
                    assertEquals(model, snapshot(map));
                }
            }
        }
        assertEquals(model.size(), map.size());
        assertEquals(model, snapshot(map));
    }

    private static int indexOf(BaseBlock[] blocks, BaseBlock block) {
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i].equals(block)) {
                return i;
            }
        }
        throw new AssertionError("unknown block " + block);
    }

    @Test
    @DisplayName("moves a key between its internal maps when the value kind changes")
    void overwritingAcrossStorageKinds() {
        BaseBlock[] blocks = blocks();
        Int2BaseBlockMap map = new Int2BaseBlockMap();

        assertNull(map.put(7, blocks[1]));
        assertEquals(blocks[1], map.put(7, blocks[3]));
        assertEquals(1, map.size());
        assertEquals(blocks[3], map.put(7, blocks[2]));
        assertEquals(1, map.size());
        assertEquals(blocks[2], map.get(7));
        assertEquals(blocks[2], map.remove(7));
        assertEquals(0, map.size());
        assertFalse(map.containsKey(7));

        assertNull(map.put(7, blocks[3]));
        assertEquals(blocks[3], map.put(7, blocks[4]));
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("supports removal through the entry iterator for both storage kinds")
    void iteratorRemove() {
        BaseBlock[] blocks = blocks();
        Int2BaseBlockMap map = new Int2BaseBlockMap();
        for (int i = 0; i < blocks.length; i++) {
            map.put(i - 3, blocks[i]);
        }

        ObjectIterator<Int2ObjectMap.Entry<BaseBlock>> iterator = map.int2ObjectEntrySet().iterator();
        int seen = 0;
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            seen++;
        }
        assertEquals(blocks.length, seen);
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    @DisplayName("handles negative and extreme keys")
    void extremeKeys() {
        BaseBlock[] blocks = blocks();
        Int2BaseBlockMap map = new Int2BaseBlockMap();
        int[] keys = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], blocks[i]);
        }
        for (int i = 0; i < keys.length; i++) {
            assertEquals(blocks[i], map.get(keys[i]));
        }
        assertEquals(keys.length, map.size());
    }
}
