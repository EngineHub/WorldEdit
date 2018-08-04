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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.world.block.BlockStateHolder;

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

    /**
     * HashSet for centralTopLimit.
     */
    private static final Map<Integer, Double> centralTopLimit = new HashMap<>();
    static {
        centralTopLimit.put(BlockID.BED, 0.5625);
        centralTopLimit.put(BlockID.BREWING_STAND, 0.875);
        centralTopLimit.put(BlockID.CAKE_BLOCK, 0.4375);
        for (int data = 6; data < 16; ++data) {
            centralTopLimit.put(-16*BlockID.CAKE_BLOCK-data, 0.0);
        }
        centralTopLimit.put(BlockID.CAULDRON, 0.3125);
        centralTopLimit.put(BlockID.COCOA_PLANT, 0.750);
        centralTopLimit.put(BlockID.ENCHANTMENT_TABLE, 0.75);
        for (int data = 0; data < 16; ++data) {
            if ((data & 4) != 0) {
                centralTopLimit.put(-16*BlockID.END_PORTAL_FRAME-data, 1.0);
            } else {
                centralTopLimit.put(-16*BlockID.END_PORTAL_FRAME-data, 0.8125);
            }
            centralTopLimit.put(-16*BlockID.HEAD-data, 0.75);
        }
        // Heads on the floor are lower
        centralTopLimit.put(-16*BlockID.HEAD-1, 0.5);
        centralTopLimit.put(-16*BlockID.HEAD-9, 0.5);
        centralTopLimit.put(BlockID.FENCE, 1.5);
        for (int data = 0; data < 8; ++data) {
            centralTopLimit.put(-16*BlockID.STEP-data, 0.5);
            centralTopLimit.put(-16*BlockID.WOODEN_STEP-data, 0.5);
            centralTopLimit.put(-16*BlockID.STEP2-data, 0.5);
            centralTopLimit.put(-16*BlockID.SNOW-data, 0.125*data);
            centralTopLimit.put(-16*BlockID.SNOW-(data+8), 0.125*data);
        }
        centralTopLimit.put(BlockID.LILY_PAD, 0.015625);
        centralTopLimit.put(BlockID.REDSTONE_REPEATER_ON, .125);
        centralTopLimit.put(BlockID.REDSTONE_REPEATER_OFF, .125);
        for (int data = 0; data < 4; ++data) {
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+ 0), 0.1875); // closed lower trap doors
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+ 4), 0.0); // opened lower trap doors
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+ 8), 1.0); // closed upper trap doors
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+12), 0.0); // opened upper trap doors

            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+ 0), 1.5);
            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+ 4), 0.0);
            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+ 8), 1.5);
            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+12), 0.0);
        }
        centralTopLimit.put(BlockID.SLOW_SAND, 0.875);
        centralTopLimit.put(BlockID.COBBLESTONE_WALL, 1.5);
        centralTopLimit.put(BlockID.FLOWER_POT, 0.375);
        centralTopLimit.put(BlockID.COMPARATOR_OFF, .125);
        centralTopLimit.put(BlockID.COMPARATOR_ON, .125);
        centralTopLimit.put(BlockID.DAYLIGHT_SENSOR, 0.375);
        centralTopLimit.put(BlockID.HOPPER, 0.625);

        // Some default values to be used if no data value is given
        centralTopLimit.put(BlockID.HEAD, 0.75);
        centralTopLimit.put(BlockID.TRAP_DOOR, 1.0);
        centralTopLimit.put(BlockID.FENCE_GATE, 1.5);
    }

    /**
     * Returns the y offset a player falls to when falling onto the top of a block at xp+0.5/zp+0.5.
     *
     * @param id the block ID
     * @param data the block data value
     * @return the y offset
     */
    public static double centralTopLimit(int id, int data) {
        if (centralTopLimit.containsKey(-16*id-data))
            return centralTopLimit.get(-16*id-data);

        if (centralTopLimit.containsKey(id))
            return centralTopLimit.get(id);

        return 1;
    }

    /**
     * Returns the y offset a player falls to when falling onto the top of a block at xp+0.5/zp+0.5.
     *
     * @param block the block
     * @return the y offset
     */
    public static double centralTopLimit(BlockStateHolder block) {
        checkNotNull(block);
        return centralTopLimit(block.getBlockType().getLegacyId(), 0);
    }

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
