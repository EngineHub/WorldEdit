/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.PlayerDirection;

import java.util.HashMap;
import java.util.Map;

/**
 * Block types.
 *
 * {@deprecated Please use {@link com.sk89q.worldedit.world.block.BlockType }}
 */
@Deprecated
public enum BlockType {

    ;

    private static final Map<Integer, PlayerDirection> dataAttachments = new HashMap<>();
    private static final Map<Integer, PlayerDirection> nonDataAttachments = new HashMap<>();
    static {
        nonDataAttachments.put(BlockID.SAPLING, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.LONG_GRASS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DEAD_BUSH, PlayerDirection.DOWN);
        for (int offset = 0; offset < 16; offset += 8) {
            dataAttachments.put(typeDataKey(BlockID.PISTON_EXTENSION, offset + 0), PlayerDirection.UP);
            dataAttachments.put(typeDataKey(BlockID.PISTON_EXTENSION, offset + 1), PlayerDirection.DOWN);
            addCardinals(BlockID.PISTON_EXTENSION, offset + 2, offset + 5, offset + 3, offset + 4);
        }
        nonDataAttachments.put(BlockID.YELLOW_FLOWER, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.RED_FLOWER, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.BROWN_MUSHROOM, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.RED_MUSHROOM, PlayerDirection.DOWN);
        for (int blockId : new int[] { BlockID.TORCH, BlockID.REDSTONE_TORCH_ON, BlockID.REDSTONE_TORCH_OFF }) {
            dataAttachments.put(typeDataKey(blockId, 0), PlayerDirection.DOWN);
            dataAttachments.put(typeDataKey(blockId, 5), PlayerDirection.DOWN); // According to the minecraft wiki, this one is history. Keeping both, for now...
            addCardinals(blockId, 4, 1, 3, 2);
        }
        nonDataAttachments.put(BlockID.REDSTONE_WIRE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CROPS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.SIGN_POST, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.WOODEN_DOOR, PlayerDirection.DOWN);
        addCardinals(BlockID.LADDER, 2, 5, 3, 4);
        addCardinals(BlockID.WALL_SIGN, 2, 5, 3, 4);
        for (int offset = 0; offset < 16; offset += 8) {
            addCardinals(BlockID.LEVER, offset + 4, offset + 1, offset + 3, offset + 2);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 5), PlayerDirection.DOWN);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 6), PlayerDirection.DOWN);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 7), PlayerDirection.UP);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 0), PlayerDirection.UP);
        }
        nonDataAttachments.put(BlockID.STONE_PRESSURE_PLATE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.IRON_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.WOODEN_PRESSURE_PLATE, PlayerDirection.DOWN);
        // redstone torches: see torches
        for (int offset = 0; offset < 16; offset += 8) {
            addCardinals(BlockID.STONE_BUTTON, offset + 4, offset + 1, offset + 3, offset + 2);
            addCardinals(BlockID.WOODEN_BUTTON, offset + 4, offset + 1, offset + 3, offset + 2);
        }
        dataAttachments.put(typeDataKey(BlockID.STONE_BUTTON, 0), PlayerDirection.UP);
        dataAttachments.put(typeDataKey(BlockID.STONE_BUTTON, 5), PlayerDirection.DOWN);
        dataAttachments.put(typeDataKey(BlockID.WOODEN_BUTTON, 0), PlayerDirection.UP);
        dataAttachments.put(typeDataKey(BlockID.WOODEN_BUTTON, 5), PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CACTUS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REED, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CAKE_BLOCK, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REDSTONE_REPEATER_OFF, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REDSTONE_REPEATER_ON, PlayerDirection.DOWN);
        for (int offset = 0; offset < 16; offset += 4) {
            addCardinals(BlockID.TRAP_DOOR, offset + 0, offset + 3, offset + 1, offset + 2);
            addCardinals(BlockID.IRON_TRAP_DOOR, offset + 0, offset + 3, offset + 1, offset + 2);
        }
        nonDataAttachments.put(BlockID.PUMPKIN_STEM, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.MELON_STEM, PlayerDirection.DOWN);
        // vines are complicated, but I'll list the single-attachment variants anyway
        dataAttachments.put(typeDataKey(BlockID.VINE, 0), PlayerDirection.UP);
        addCardinals(BlockID.VINE, 1, 2, 4, 8);
        nonDataAttachments.put(BlockID.NETHER_WART, PlayerDirection.DOWN);
        for (int offset = 0; offset < 16; offset += 4) {
            addCardinals(BlockID.COCOA_PLANT, offset + 0, offset + 1, offset + 2, offset + 3);
        }
        for (int offset = 0; offset < 16; offset += 4) {
            addCardinals(BlockID.TRIPWIRE_HOOK, offset + 2, offset + 3, offset + 0, offset + 1);
        }
        nonDataAttachments.put(BlockID.TRIPWIRE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.FLOWER_POT, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CARROTS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.POTATOES, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.ANVIL, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.PRESSURE_PLATE_LIGHT, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.PRESSURE_PLATE_HEAVY, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.COMPARATOR_OFF, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.COMPARATOR_ON, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CARPET, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DOUBLE_PLANT, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.STANDING_BANNER, PlayerDirection.DOWN);
        addCardinals(BlockID.WALL_BANNER, 4, 2, 5, 3);
        nonDataAttachments.put(BlockID.SPRUCE_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.BIRCH_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.JUNGLE_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.ACACIA_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DARK_OAK_DOOR, PlayerDirection.DOWN);

        // Rails are hardcoded to be attached to the block below them.
        // In addition to that, let's attach ascending rails to the block they're ascending towards.
        for (int offset = 0; offset < 16; offset += 8) {
            addCardinals(BlockID.POWERED_RAIL, offset + 3, offset + 4, offset + 2, offset + 5);
            addCardinals(BlockID.DETECTOR_RAIL, offset + 3, offset + 4, offset + 2, offset + 5);
            addCardinals(BlockID.MINECART_TRACKS, offset + 3, offset + 4, offset + 2, offset + 5);
            addCardinals(BlockID.ACTIVATOR_RAIL, offset + 3, offset + 4, offset + 2, offset + 5);
        }
    }

    /**
     * Returns the direction to the block(B) this block(A) is attached to.
     * Attached means that if block B is destroyed, block A will pop off.
     *
     * @param type the block id of block A
     * @param data the data value of block A
     * @return direction to block B
     */
    public static PlayerDirection getAttachment(int type, int data) {
        PlayerDirection direction = nonDataAttachments.get(type);
        if (direction != null) return direction;

        return dataAttachments.get(typeDataKey(type, data));
    }

    private static int typeDataKey(int type, int data) {
        return (type << 4) | (data & 0xf);
    }

    private static void addCardinals(int type, int west, int north, int east, int south) {
        dataAttachments.put(typeDataKey(type, west), PlayerDirection.WEST);
        dataAttachments.put(typeDataKey(type, north), PlayerDirection.NORTH);
        dataAttachments.put(typeDataKey(type, east), PlayerDirection.EAST);
        dataAttachments.put(typeDataKey(type, south), PlayerDirection.SOUTH);
    }

}
