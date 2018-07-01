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
import com.sk89q.worldedit.blocks.type.BlockStateHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Block types.
 *
 * {@deprecated Please use {@link com.sk89q.worldedit.blocks.type.BlockType}}
 */
@Deprecated
public enum BlockType {

    ;

    /**
     * HashSet for shouldPlaceLast.
     */
    private static final Set<Integer> shouldPlaceLast = new HashSet<>();
    static {
        shouldPlaceLast.add(BlockID.SAPLING);
        shouldPlaceLast.add(BlockID.BED);
        shouldPlaceLast.add(BlockID.POWERED_RAIL);
        shouldPlaceLast.add(BlockID.DETECTOR_RAIL);
        shouldPlaceLast.add(BlockID.LONG_GRASS);
        shouldPlaceLast.add(BlockID.DEAD_BUSH);
        shouldPlaceLast.add(BlockID.YELLOW_FLOWER);
        shouldPlaceLast.add(BlockID.RED_FLOWER);
        shouldPlaceLast.add(BlockID.BROWN_MUSHROOM);
        shouldPlaceLast.add(BlockID.RED_MUSHROOM);
        shouldPlaceLast.add(BlockID.TORCH);
        shouldPlaceLast.add(BlockID.FIRE);
        shouldPlaceLast.add(BlockID.REDSTONE_WIRE);
        shouldPlaceLast.add(BlockID.CROPS);
        shouldPlaceLast.add(BlockID.LADDER);
        shouldPlaceLast.add(BlockID.MINECART_TRACKS);
        shouldPlaceLast.add(BlockID.LEVER);
        shouldPlaceLast.add(BlockID.STONE_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockID.WOODEN_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockID.REDSTONE_TORCH_OFF);
        shouldPlaceLast.add(BlockID.REDSTONE_TORCH_ON);
        shouldPlaceLast.add(BlockID.STONE_BUTTON);
        shouldPlaceLast.add(BlockID.SNOW);
        shouldPlaceLast.add(BlockID.PORTAL);
        shouldPlaceLast.add(BlockID.REDSTONE_REPEATER_OFF);
        shouldPlaceLast.add(BlockID.REDSTONE_REPEATER_ON);
        shouldPlaceLast.add(BlockID.TRAP_DOOR);
        shouldPlaceLast.add(BlockID.VINE);
        shouldPlaceLast.add(BlockID.LILY_PAD);
        shouldPlaceLast.add(BlockID.NETHER_WART);
        shouldPlaceLast.add(BlockID.PISTON_BASE);
        shouldPlaceLast.add(BlockID.PISTON_STICKY_BASE);
        shouldPlaceLast.add(BlockID.PISTON_EXTENSION);
        shouldPlaceLast.add(BlockID.PISTON_MOVING_PIECE);
        shouldPlaceLast.add(BlockID.COCOA_PLANT);
        shouldPlaceLast.add(BlockID.TRIPWIRE_HOOK);
        shouldPlaceLast.add(BlockID.TRIPWIRE);
        shouldPlaceLast.add(BlockID.FLOWER_POT);
        shouldPlaceLast.add(BlockID.CARROTS);
        shouldPlaceLast.add(BlockID.POTATOES);
        shouldPlaceLast.add(BlockID.WOODEN_BUTTON);
        shouldPlaceLast.add(BlockID.ANVIL); // becomes relevant with asynchronous placement
        shouldPlaceLast.add(BlockID.PRESSURE_PLATE_LIGHT);
        shouldPlaceLast.add(BlockID.PRESSURE_PLATE_HEAVY);
        shouldPlaceLast.add(BlockID.COMPARATOR_OFF);
        shouldPlaceLast.add(BlockID.COMPARATOR_ON);
        shouldPlaceLast.add(BlockID.ACTIVATOR_RAIL);
        shouldPlaceLast.add(BlockID.IRON_TRAP_DOOR);
        shouldPlaceLast.add(BlockID.CARPET);
        shouldPlaceLast.add(BlockID.DOUBLE_PLANT);
        shouldPlaceLast.add(BlockID.DAYLIGHT_SENSOR_INVERTED);
    }

    /**
     * Checks to see whether a block should be placed last (when reordering
     * blocks that are placed).
     *
     * @param id the block ID
     * @return true if the block should be placed last
     */
    public static boolean shouldPlaceLast(int id) {
        return shouldPlaceLast.contains(id);
    }

    /**
     * HashSet for shouldPlaceLast.
     */
    private static final Set<Integer> shouldPlaceFinal = new HashSet<>();
    static {
        shouldPlaceFinal.add(BlockID.SIGN_POST);
        shouldPlaceFinal.add(BlockID.WOODEN_DOOR);
        shouldPlaceFinal.add(BlockID.ACACIA_DOOR);
        shouldPlaceFinal.add(BlockID.BIRCH_DOOR);
        shouldPlaceFinal.add(BlockID.JUNGLE_DOOR);
        shouldPlaceFinal.add(BlockID.DARK_OAK_DOOR);
        shouldPlaceFinal.add(BlockID.SPRUCE_DOOR);
        shouldPlaceFinal.add(BlockID.WALL_SIGN);
        shouldPlaceFinal.add(BlockID.IRON_DOOR);
        shouldPlaceFinal.add(BlockID.CACTUS);
        shouldPlaceFinal.add(BlockID.REED);
        shouldPlaceFinal.add(BlockID.CAKE_BLOCK);
        shouldPlaceFinal.add(BlockID.PISTON_EXTENSION);
        shouldPlaceFinal.add(BlockID.PISTON_MOVING_PIECE);
        shouldPlaceFinal.add(BlockID.STANDING_BANNER);
        shouldPlaceFinal.add(BlockID.WALL_BANNER);
    }

    /**
     * Checks to see whether a block should be placed in the final queue.
     *
     * This applies to blocks that can be attached to other blocks that have an attachment.
     *
     * @param id the type ID of the block
     * @return whether the block is in the final queue
     */
    public static boolean shouldPlaceFinal(int id) {
        return shouldPlaceFinal.contains(id);
    }

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

    /**
     * HashSet for isContainerBlock.
     */
    private static final Set<Integer> isContainerBlock = new HashSet<>();
    static {
        isContainerBlock.add(BlockID.DISPENSER);
        isContainerBlock.add(BlockID.FURNACE);
        isContainerBlock.add(BlockID.BURNING_FURNACE);
        isContainerBlock.add(BlockID.CHEST);
        isContainerBlock.add(BlockID.BREWING_STAND);
        isContainerBlock.add(BlockID.TRAPPED_CHEST);
        isContainerBlock.add(BlockID.HOPPER);
        isContainerBlock.add(BlockID.DROPPER);
        //isContainerBlock.add(BlockID.ENDER_CHEST); // ender chest has no own inventory, don't add this here
    }

    /**
     * Returns true if the block is a container block.
     *
     * @param id the block ID
     * @return true if the block is a container
     */
    public static boolean isContainerBlock(int id) {
        return isContainerBlock.contains(id);
    }

    /**
     * HashSet for isNaturalBlock.
     */
    private static final Set<Integer> isNaturalTerrainBlock = new HashSet<>();
    static {
        isNaturalTerrainBlock.add(BlockID.STONE);
        isNaturalTerrainBlock.add(BlockID.GRASS);
        isNaturalTerrainBlock.add(BlockID.DIRT);
        // isNaturalBlock.add(BlockID.COBBLESTONE); // technically can occur next to water and lava
        isNaturalTerrainBlock.add(BlockID.BEDROCK);
        isNaturalTerrainBlock.add(BlockID.SAND);
        isNaturalTerrainBlock.add(BlockID.GRAVEL);
        isNaturalTerrainBlock.add(BlockID.CLAY);
        isNaturalTerrainBlock.add(BlockID.MYCELIUM);
        isNaturalTerrainBlock.add(BlockID.PACKED_ICE);
        isNaturalTerrainBlock.add(BlockID.STAINED_CLAY);

        // hell
        isNaturalTerrainBlock.add(BlockID.NETHERRACK);
        isNaturalTerrainBlock.add(BlockID.SLOW_SAND);
        isNaturalTerrainBlock.add(BlockID.LIGHTSTONE);
        isNaturalTerrainBlock.add(BlockID.QUARTZ_ORE);

        // ores
        isNaturalTerrainBlock.add(BlockID.COAL_ORE);
        isNaturalTerrainBlock.add(BlockID.IRON_ORE);
        isNaturalTerrainBlock.add(BlockID.GOLD_ORE);
        isNaturalTerrainBlock.add(BlockID.LAPIS_LAZULI_ORE);
        isNaturalTerrainBlock.add(BlockID.DIAMOND_ORE);
        isNaturalTerrainBlock.add(BlockID.REDSTONE_ORE);
        isNaturalTerrainBlock.add(BlockID.GLOWING_REDSTONE_ORE);
        isNaturalTerrainBlock.add(BlockID.EMERALD_ORE);
    }

    /**
     * Checks if the block type is naturally occurring.
     *
     * @param id the type ID of the block
     * @return true if the block type is naturally occurring
     * @deprecated Use {@link #isNaturalTerrainBlock(int, int)}
     */
    @Deprecated
    public static boolean isNaturalTerrainBlock(int id) {
        return isNaturalTerrainBlock.contains(id);
    }

    /**
     * Checks if the block type is naturally occurring
     *
     * @param id the type ID of the block
     * @param data data value of the block
     * @return true if the block type is naturally occurring
     */
    public static boolean isNaturalTerrainBlock(int id, int data) {
        return isNaturalTerrainBlock.contains(-16*id-data) || isNaturalTerrainBlock.contains(id);
    }

    /**
     * HashSet for isTranslucent.
     */
    private static final Set<Integer> isTranslucent = new HashSet<>();
    static {
        isTranslucent.add(BlockID.AIR);
        isTranslucent.add(BlockID.SAPLING);
        isTranslucent.add(BlockID.WATER);
        isTranslucent.add(BlockID.STATIONARY_WATER);
        isTranslucent.add(BlockID.LEAVES);
        isTranslucent.add(BlockID.GLASS);
        isTranslucent.add(BlockID.BED);
        isTranslucent.add(BlockID.POWERED_RAIL);
        isTranslucent.add(BlockID.DETECTOR_RAIL);
        //isTranslucent.add(BlockID.PISTON_STICKY_BASE);
        isTranslucent.add(BlockID.WEB);
        isTranslucent.add(BlockID.LONG_GRASS);
        isTranslucent.add(BlockID.DEAD_BUSH);
        //isTranslucent.add(BlockID.PISTON_BASE);
        isTranslucent.add(BlockID.PISTON_EXTENSION);
        //isTranslucent.add(BlockID.PISTON_MOVING_PIECE);
        isTranslucent.add(BlockID.YELLOW_FLOWER);
        isTranslucent.add(BlockID.RED_FLOWER);
        isTranslucent.add(BlockID.BROWN_MUSHROOM);
        isTranslucent.add(BlockID.RED_MUSHROOM);
        isTranslucent.add(BlockID.STEP);
        isTranslucent.add(BlockID.TORCH);
        isTranslucent.add(BlockID.FIRE);
        isTranslucent.add(BlockID.MOB_SPAWNER);
        isTranslucent.add(BlockID.OAK_WOOD_STAIRS);
        isTranslucent.add(BlockID.CHEST);
        isTranslucent.add(BlockID.REDSTONE_WIRE);
        isTranslucent.add(BlockID.CROPS);
        isTranslucent.add(BlockID.SIGN_POST);
        isTranslucent.add(BlockID.WOODEN_DOOR);
        isTranslucent.add(BlockID.LADDER);
        isTranslucent.add(BlockID.MINECART_TRACKS);
        isTranslucent.add(BlockID.COBBLESTONE_STAIRS);
        isTranslucent.add(BlockID.WALL_SIGN);
        isTranslucent.add(BlockID.LEVER);
        isTranslucent.add(BlockID.STONE_PRESSURE_PLATE);
        isTranslucent.add(BlockID.IRON_DOOR);
        isTranslucent.add(BlockID.WOODEN_PRESSURE_PLATE);
        isTranslucent.add(BlockID.REDSTONE_TORCH_OFF);
        isTranslucent.add(BlockID.REDSTONE_TORCH_ON);
        isTranslucent.add(BlockID.STONE_BUTTON);
        isTranslucent.add(BlockID.SNOW);
        isTranslucent.add(BlockID.ICE);
        isTranslucent.add(BlockID.CACTUS);
        isTranslucent.add(BlockID.REED);
        isTranslucent.add(BlockID.FENCE);
        isTranslucent.add(BlockID.PORTAL);
        isTranslucent.add(BlockID.CAKE_BLOCK);
        isTranslucent.add(BlockID.REDSTONE_REPEATER_OFF);
        isTranslucent.add(BlockID.REDSTONE_REPEATER_ON);
        isTranslucent.add(BlockID.TRAP_DOOR);
        isTranslucent.add(BlockID.IRON_BARS);
        isTranslucent.add(BlockID.GLASS_PANE);
        isTranslucent.add(BlockID.PUMPKIN_STEM);
        isTranslucent.add(BlockID.MELON_STEM);
        isTranslucent.add(BlockID.VINE);
        isTranslucent.add(BlockID.FENCE_GATE);
        isTranslucent.add(BlockID.BRICK_STAIRS);
        isTranslucent.add(BlockID.STONE_BRICK_STAIRS);
        isTranslucent.add(BlockID.LILY_PAD);
        isTranslucent.add(BlockID.NETHER_BRICK_FENCE);
        isTranslucent.add(BlockID.NETHER_BRICK_STAIRS);
        isTranslucent.add(BlockID.NETHER_WART);
        isTranslucent.add(BlockID.ENCHANTMENT_TABLE);
        isTranslucent.add(BlockID.BREWING_STAND);
        isTranslucent.add(BlockID.CAULDRON);
        isTranslucent.add(BlockID.WOODEN_STEP);
        isTranslucent.add(BlockID.COCOA_PLANT);
        isTranslucent.add(BlockID.SANDSTONE_STAIRS);
        isTranslucent.add(BlockID.ENDER_CHEST);
        isTranslucent.add(BlockID.TRIPWIRE_HOOK);
        isTranslucent.add(BlockID.TRIPWIRE);
        isTranslucent.add(BlockID.SPRUCE_WOOD_STAIRS);
        isTranslucent.add(BlockID.BIRCH_WOOD_STAIRS);
        isTranslucent.add(BlockID.JUNGLE_WOOD_STAIRS);
        isTranslucent.add(BlockID.COBBLESTONE_WALL);
        isTranslucent.add(BlockID.FLOWER_POT);
        isTranslucent.add(BlockID.CARROTS);
        isTranslucent.add(BlockID.POTATOES);
        isTranslucent.add(BlockID.WOODEN_BUTTON);
        isTranslucent.add(BlockID.HEAD);
        isTranslucent.add(BlockID.ANVIL);
        isTranslucent.add(BlockID.TRAPPED_CHEST);
        isTranslucent.add(BlockID.PRESSURE_PLATE_LIGHT);
        isTranslucent.add(BlockID.PRESSURE_PLATE_HEAVY);
        isTranslucent.add(BlockID.COMPARATOR_OFF);
        isTranslucent.add(BlockID.COMPARATOR_ON);
        isTranslucent.add(BlockID.DAYLIGHT_SENSOR);
        isTranslucent.add(BlockID.HOPPER);
        isTranslucent.add(BlockID.QUARTZ_STAIRS);
        isTranslucent.add(BlockID.ACTIVATOR_RAIL);
        isTranslucent.add(BlockID.ACACIA_STAIRS);
        isTranslucent.add(BlockID.DARK_OAK_STAIRS);
        isTranslucent.add(BlockID.BARRIER);
        isTranslucent.add(BlockID.IRON_TRAP_DOOR);
        isTranslucent.add(BlockID.CARPET);
        isTranslucent.add(BlockID.LEAVES2);
        isTranslucent.add(BlockID.STAINED_GLASS_PANE);
        isTranslucent.add(BlockID.DOUBLE_PLANT);
        isTranslucent.add(BlockID.STANDING_BANNER);
        isTranslucent.add(BlockID.WALL_BANNER);
        isTranslucent.add(BlockID.DAYLIGHT_SENSOR_INVERTED);
        isTranslucent.add(BlockID.RED_SANDSTONE_STAIRS);
        isTranslucent.add(BlockID.STEP2);
        isTranslucent.add(BlockID.SPRUCE_FENCE_GATE);
        isTranslucent.add(BlockID.BIRCH_FENCE_GATE);
        isTranslucent.add(BlockID.JUNGLE_FENCE_GATE);
        isTranslucent.add(BlockID.DARK_OAK_FENCE_GATE);
        isTranslucent.add(BlockID.ACACIA_FENCE_GATE);
        isTranslucent.add(BlockID.SPRUCE_FENCE);
        isTranslucent.add(BlockID.BIRCH_FENCE);
        isTranslucent.add(BlockID.JUNGLE_FENCE);
        isTranslucent.add(BlockID.DARK_OAK_FENCE);
        isTranslucent.add(BlockID.ACACIA_FENCE);
        isTranslucent.add(BlockID.SPRUCE_DOOR);
        isTranslucent.add(BlockID.BIRCH_DOOR);
        isTranslucent.add(BlockID.JUNGLE_DOOR);
        isTranslucent.add(BlockID.ACACIA_DOOR);
        isTranslucent.add(BlockID.DARK_OAK_DOOR);
        isTranslucent.add(BlockID.END_ROD);
        isTranslucent.add(BlockID.CHORUS_PLANT);
        isTranslucent.add(BlockID.CHORUS_FLOWER);
        isTranslucent.add(BlockID.PURPUR_STAIRS);
        isTranslucent.add(BlockID.PURPUR_SLAB);
        isTranslucent.add(BlockID.BEETROOTS);
    }

    /**
     * Checks if the block type lets light through.
     *
     * @param id the type ID of the block
     * @return true if the block type lets light through
     */
    public static boolean isTranslucent(int id) {
        return isTranslucent.contains(id);
    }

    /**
     * HashMap for getBlockBagItem.
     */
    private static final Map<Integer, BaseItem> dataBlockBagItems = new HashMap<>();
    private static final Map<Integer, BaseItem> nonDataBlockBagItems = new HashMap<>();
    private static final BaseItem doNotDestroy = new BaseItemStack(BlockID.AIR, 0);
    static {
        /*
         * rules:
         *
         * 1. block yields itself => addIdentity
         * 2. block is part of a 2-block object => drop an appropriate item for one of the 2 blocks
         * 3. block can be placed by right-clicking an obtainable item on the ground => use that item
         * 4. block yields more than one item => addIdentities
         * 5. block yields exactly one item => use that item
         * 6. block is a liquid => drop nothing
         * 7. block is created from thin air by the game other than by the map generator => drop nothing
         */

        nonDataBlockBagItems.put(BlockID.STONE, new BaseItem(BlockID.COBBLESTONE)); // rule 5
        nonDataBlockBagItems.put(BlockID.GRASS, new BaseItem(BlockID.DIRT)); // rule 5
        addIdentities(BlockID.DIRT, 3); // rule 1
        addIdentity(BlockID.COBBLESTONE); // rule 1
        addIdentities(BlockID.WOOD, 6); // rule 1
        addIdentities(BlockID.SAPLING, 6); // rule 1
        nonDataBlockBagItems.put(BlockID.BEDROCK, doNotDestroy); // exception
        // WATER, rule 6
        // STATIONARY_WATER, rule 6
        // LAVA, rule 6
        // STATIONARY_LAVA, rule 6
        addIdentity(BlockID.SAND); // rule 1
        addIdentity(BlockID.GRAVEL); // rule 1
        addIdentity(BlockID.GOLD_ORE); // rule 1
        addIdentity(BlockID.IRON_ORE); // rule 1
        nonDataBlockBagItems.put(BlockID.COAL_ORE, new BaseItem(ItemID.COAL)); // rule 5
        addIdentities(BlockID.LOG, 4); // rule 1
        addIdentities(BlockID.LEAVES, 4); // rule 1 with shears, otherwise rule 3
        addIdentity(BlockID.SPONGE); // rule 1
        addIdentity(BlockID.GLASS); // rule 3
        addIdentity(BlockID.LAPIS_LAZULI_ORE); // rule 4
        addIdentity(BlockID.LAPIS_LAZULI_BLOCK); // rule 1
        addIdentity(BlockID.DISPENSER); // rule 1
        addIdentity(BlockID.SANDSTONE); // rule 1
        addIdentity(BlockID.NOTE_BLOCK); // rule 1
        addIdentities(BlockID.BED, 8); // rule 2
        addIdentity(BlockID.POWERED_RAIL); // rule 1
        addIdentity(BlockID.DETECTOR_RAIL); // rule 1
        addIdentity(BlockID.PISTON_STICKY_BASE);
        nonDataBlockBagItems.put(BlockID.WEB, new BaseItem(ItemID.STRING)); // rule 5
        // LONG_GRASS
        // DEAD_BUSH
        addIdentity(BlockID.PISTON_BASE);
        // PISTON_EXTENSION, rule 7
        addIdentities(BlockID.CLOTH, 16); // rule 1
        // PISTON_MOVING_PIECE, rule 7
        addIdentity(BlockID.YELLOW_FLOWER); // rule 1
        addIdentity(BlockID.RED_FLOWER); // rule 1
        addIdentity(BlockID.BROWN_MUSHROOM); // rule 1
        addIdentity(BlockID.RED_MUSHROOM); // rule 1
        addIdentity(BlockID.GOLD_BLOCK); // rule 1
        addIdentity(BlockID.IRON_BLOCK); // rule 1
        addIdentities(BlockID.DOUBLE_STEP, 7); // rule 3
        addIdentities(BlockID.STEP, 7); // rule 1
        addIdentity(BlockID.BRICK); // rule 1
        addIdentity(BlockID.TNT);
        addIdentity(BlockID.BOOKCASE); // rule 3
        addIdentity(BlockID.MOSSY_COBBLESTONE); // rule 1
        addIdentity(BlockID.OBSIDIAN); // rule 1
        addIdentity(BlockID.TORCH); // rule 1
        // FIRE
        // MOB_SPAWNER
        addIdentity(BlockID.OAK_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.CHEST); // rule 1
        nonDataBlockBagItems.put(BlockID.REDSTONE_WIRE, new BaseItem(ItemID.REDSTONE_DUST)); // rule 3
        nonDataBlockBagItems.put(BlockID.DIAMOND_ORE, new BaseItem(ItemID.DIAMOND)); // rule 5
        addIdentity(BlockID.DIAMOND_BLOCK); // rule 1
        addIdentity(BlockID.WORKBENCH); // rule 1
        nonDataBlockBagItems.put(BlockID.CROPS, new BaseItem(ItemID.SEEDS)); // rule 3
        nonDataBlockBagItems.put(BlockID.SOIL, new BaseItem(BlockID.DIRT)); // rule 5
        addIdentity(BlockID.FURNACE); // rule 1
        nonDataBlockBagItems.put(BlockID.BURNING_FURNACE, new BaseItem(BlockID.FURNACE));
        nonDataBlockBagItems.put(BlockID.SIGN_POST, new BaseItem(ItemID.SIGN)); // rule 3
        addIdentities(BlockID.WOODEN_DOOR, 8); // rule 2
        addIdentity(BlockID.LADDER); // rule 1
        addIdentity(BlockID.MINECART_TRACKS); // rule 1
        addIdentity(BlockID.COBBLESTONE_STAIRS); // rule 3
        nonDataBlockBagItems.put(BlockID.WALL_SIGN, new BaseItem(ItemID.SIGN)); // rule 3
        addIdentity(BlockID.LEVER); // rule 1
        addIdentity(BlockID.STONE_PRESSURE_PLATE); // rule 1
        addIdentities(BlockID.IRON_DOOR, 8); // rule 2
        addIdentity(BlockID.WOODEN_PRESSURE_PLATE); // rule 1
        addIdentity(BlockID.REDSTONE_ORE); // rule 4
        nonDataBlockBagItems.put(BlockID.GLOWING_REDSTONE_ORE, new BaseItem(BlockID.REDSTONE_ORE)); // rule 4
        nonDataBlockBagItems.put(BlockID.REDSTONE_TORCH_OFF, new BaseItem(BlockID.REDSTONE_TORCH_ON)); // rule 3
        addIdentity(BlockID.REDSTONE_TORCH_ON); // rule 1
        addIdentity(BlockID.STONE_BUTTON); // rule 1
        addIdentity(BlockID.SNOW); // rule 1
        addIdentity(BlockID.ICE); // exception
        addIdentity(BlockID.SNOW_BLOCK); // rule 3
        addIdentity(BlockID.CACTUS);
        addIdentity(BlockID.CLAY); // rule 3
        nonDataBlockBagItems.put(BlockID.REED, new BaseItem(ItemID.SUGAR_CANE_ITEM)); // rule 3
        addIdentity(BlockID.JUKEBOX); // rule 1
        addIdentity(BlockID.FENCE); // rule 1
        addIdentity(BlockID.PUMPKIN); // rule 1
        addIdentity(BlockID.NETHERRACK); // rule 1
        addIdentity(BlockID.SLOW_SAND); // rule 1
        addIdentity(BlockID.LIGHTSTONE); // rule 4
        // PORTAL
        addIdentity(BlockID.JACKOLANTERN); // rule 1
        nonDataBlockBagItems.put(BlockID.CAKE_BLOCK, new BaseItem(ItemID.CAKE_ITEM)); // rule 3
        nonDataBlockBagItems.put(BlockID.REDSTONE_REPEATER_OFF, new BaseItem(ItemID.REDSTONE_REPEATER)); // rule 3
        nonDataBlockBagItems.put(BlockID.REDSTONE_REPEATER_ON, new BaseItem(ItemID.REDSTONE_REPEATER)); // rule 3
        addIdentities(BlockID.STAINED_GLASS_PANE, 16); // ???
        addIdentity(BlockID.TRAP_DOOR); // rule 1
        nonDataBlockBagItems.put(BlockID.SILVERFISH_BLOCK, doNotDestroy); // exception
        addIdentity(BlockID.STONE_BRICK); // rule 1
        addIdentity(BlockID.BROWN_MUSHROOM_CAP);
        addIdentity(BlockID.RED_MUSHROOM_CAP);
        addIdentity(BlockID.IRON_BARS); // rule 1
        addIdentity(BlockID.GLASS_PANE); // rule 1
        addIdentity(BlockID.MELON_BLOCK); // rule 3
        nonDataBlockBagItems.put(BlockID.PUMPKIN_STEM, new BaseItem(ItemID.PUMPKIN_SEEDS)); // rule 3
        nonDataBlockBagItems.put(BlockID.MELON_STEM, new BaseItem(ItemID.MELON_SEEDS)); // rule 3
        nonDataBlockBagItems.put(BlockID.VINE, doNotDestroy); // exception
        addIdentity(BlockID.FENCE_GATE); // rule 1
        addIdentity(BlockID.BRICK_STAIRS); // rule 3
        addIdentity(BlockID.STONE_BRICK_STAIRS); // rule 3

        // 1.9 blocks
        nonDataBlockBagItems.put(BlockID.MYCELIUM, new BaseItem(BlockID.DIRT));
        addIdentity(BlockID.LILY_PAD);
        addIdentity(BlockID.NETHER_BRICK);
        addIdentity(BlockID.NETHER_BRICK_FENCE);
        addIdentity(BlockID.NETHER_BRICK_STAIRS);
        nonDataBlockBagItems.put(BlockID.NETHER_WART, new BaseItem(ItemID.NETHER_WART_SEED));
        addIdentity(BlockID.ENCHANTMENT_TABLE);
        nonDataBlockBagItems.put(BlockID.BREWING_STAND, new BaseItem(ItemID.BREWING_STAND));
        nonDataBlockBagItems.put(BlockID.CAULDRON, new BaseItem(ItemID.CAULDRON));
        nonDataBlockBagItems.put(BlockID.END_PORTAL, doNotDestroy);
        nonDataBlockBagItems.put(BlockID.END_PORTAL_FRAME, doNotDestroy);
        addIdentity(BlockID.END_STONE);

        addIdentity(BlockID.REDSTONE_LAMP_OFF);
        nonDataBlockBagItems.put(BlockID.REDSTONE_LAMP_ON, new BaseItem(BlockID.REDSTONE_LAMP_OFF));

        addIdentities(BlockID.DOUBLE_WOODEN_STEP, 7); // rule 3
        addIdentities(BlockID.WOODEN_STEP, 7); // rule 1
        nonDataBlockBagItems.put(BlockID.COCOA_PLANT, new BaseItem(ItemID.INK_SACK)); // rule 3 TODO data removed
        addIdentity(BlockID.SANDSTONE_STAIRS); // rule 1
        nonDataBlockBagItems.put(BlockID.EMERALD_ORE, new BaseItem(ItemID.EMERALD)); // rule 5
        addIdentity(BlockID.ENDER_CHEST); // rule 3
        addIdentity(BlockID.TRIPWIRE_HOOK); // rule 1
        nonDataBlockBagItems.put(BlockID.TRIPWIRE, new BaseItem(ItemID.STRING)); // rule 3
        addIdentity(BlockID.EMERALD_BLOCK); // rule 1
        addIdentity(BlockID.SPRUCE_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.BIRCH_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.JUNGLE_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.COMMAND_BLOCK); // rule 1
        addIdentities(BlockID.COBBLESTONE_WALL, 1); // rule 4
        nonDataBlockBagItems.put(BlockID.FLOWER_POT, new BaseItemStack(ItemID.FLOWER_POT)); // rule 3
        nonDataBlockBagItems.put(BlockID.CARROTS, new BaseItemStack(ItemID.CARROT)); // rule 3
        nonDataBlockBagItems.put(BlockID.POTATOES, new BaseItemStack(ItemID.POTATO)); // rule 3
        addIdentity(BlockID.WOODEN_BUTTON); // rule 1
        nonDataBlockBagItems.put(BlockID.HEAD, doNotDestroy); // exception, can't handle TE data
        addIdentities(BlockID.ANVIL, 2); // rule 4
        addIdentity(BlockID.TRAPPED_CHEST); // rule 1
        addIdentity(BlockID.PRESSURE_PLATE_LIGHT); // rule 1
        addIdentity(BlockID.PRESSURE_PLATE_HEAVY); // rule 1
        nonDataBlockBagItems.put(BlockID.COMPARATOR_OFF, new BaseItemStack(ItemID.COMPARATOR)); // rule 3
        nonDataBlockBagItems.put(BlockID.COMPARATOR_ON, new BaseItemStack(ItemID.COMPARATOR)); // rule 3
        addIdentity(BlockID.DAYLIGHT_SENSOR); // rule 1
        addIdentity(BlockID.REDSTONE_BLOCK); // rule 1
        nonDataBlockBagItems.put(BlockID.QUARTZ_ORE, new BaseItemStack(ItemID.NETHER_QUARTZ)); // rule 3
        addIdentity(BlockID.HOPPER); // rule 1
        addIdentities(BlockID.QUARTZ_BLOCK, 1); // rule 4
        for (int i = 2; i <= 4; i++) {
            dataBlockBagItems.put(typeDataKey(BlockID.QUARTZ_BLOCK, i), new BaseItem(BlockID.QUARTZ_BLOCK)); // rule 4, quartz pillars TODO data
            // removed
        }
        addIdentity(BlockID.QUARTZ_STAIRS); // rule 1
        addIdentity(BlockID.ACTIVATOR_RAIL); // rule 1
        addIdentity(BlockID.DROPPER); // rule 1

        addIdentities(BlockID.STAINED_CLAY, 16); // rule 1
        addIdentity(BlockID.HAY_BLOCK); // rule 1
        addIdentities(BlockID.CARPET, 16); // rule 1
        addIdentity(BlockID.HARDENED_CLAY); // rule 1
        addIdentity(BlockID.COAL_BLOCK); // rule 1

        addIdentities(BlockID.LOG2, 1);
        addIdentities(BlockID.LEAVES2, 1);
        addIdentity(BlockID.ACACIA_STAIRS);
        addIdentity(BlockID.DARK_OAK_STAIRS);
        addIdentity(BlockID.PACKED_ICE);
        addIdentities(BlockID.STAINED_GLASS_PANE, 16);
        addIdentities(BlockID.DOUBLE_PLANT, 6);

        addIdentities(BlockID.ACACIA_DOOR, 8); // rule 2
        addIdentities(BlockID.BIRCH_DOOR, 8); // rule 2
        addIdentities(BlockID.JUNGLE_DOOR, 8); // rule 2
        addIdentities(BlockID.DARK_OAK_DOOR, 8); // rule 2
        addIdentities(BlockID.SPRUCE_DOOR, 8); // rule 2
    }

    /**
     * Get the block or item that this block can be constructed from. If nothing is
     * dropped, a block with a BaseItemStack of type AIR and size 0 will be returned.
     * If the block should not be destroyed (i.e. bedrock), null will be returned.
     *
     * @param type the type of of the block
     * @param data the data value of the block
     * @return the item or null
     */
    @Nullable
    public static BaseItem getBlockBagItem(int type, int data) {
        BaseItem dropped = nonDataBlockBagItems.get(type);
        if (dropped != null) return dropped;

        dropped = dataBlockBagItems.get(typeDataKey(type, data));

        if (dropped == null) {
            return new BaseItemStack(BlockID.AIR, 0);
        }

        if (dropped == doNotDestroy) {
            return null;
        }

        return dropped;
    }

    private static void addIdentity(int type) {
        nonDataBlockBagItems.put(type, new BaseItem(type));
    }

    private static void addIdentities(int type, int maxData) {
        for (int data = 0; data < maxData; ++data) {
            dataBlockBagItems.put(typeDataKey(type, data), new BaseItem(type)); // TODO data removed
        }
    }

    private static final Random random = new Random();

    /**
     * Get the block drop for a block.
     *
     * @param id the type ID of the block
     * @param data the data value
     * @return an item or null
     */
    @Nullable
    public static BaseItemStack getBlockDrop(int id, short data) {
        int store;
        switch (id) {
        case BlockID.STONE:
            return new BaseItemStack(BlockID.COBBLESTONE);

        case BlockID.GRASS:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.GRAVEL:
            if (random.nextInt(10) == 0) {
                return new BaseItemStack(ItemID.FLINT);
            } else {
                return new BaseItemStack(BlockID.GRAVEL);
            }

        case BlockID.COAL_ORE:
            return new BaseItemStack(ItemID.COAL);

        case BlockID.LEAVES:
            if (random.nextDouble() > 0.95) {
                return new BaseItemStack(BlockID.SAPLING, 1, data);
            } else {
                return null;
            }

        case BlockID.LAPIS_LAZULI_ORE:
            return new BaseItemStack(ItemID.INK_SACK, random.nextInt(5) + 4, (short) 4);

        case BlockID.BED:
            return new BaseItemStack(ItemID.BED_ITEM);

        case BlockID.LONG_GRASS:
            if (random.nextInt(8) == 0) {
                return new BaseItemStack(ItemID.SEEDS);
            } else {
                return null;
            }

        case BlockID.DOUBLE_STEP:
            return new BaseItemStack(BlockID.STEP, 2, data);

        case BlockID.REDSTONE_WIRE:
            return new BaseItemStack(ItemID.REDSTONE_DUST);

        case BlockID.DIAMOND_ORE:
            return new BaseItemStack(ItemID.DIAMOND);

        case BlockID.CROPS:
            if (data == 7) return new BaseItemStack(ItemID.WHEAT);
            return new BaseItemStack(ItemID.SEEDS);

        case BlockID.SOIL:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.BURNING_FURNACE:
            return new BaseItemStack(BlockID.FURNACE);

        case BlockID.SIGN_POST:
            return new BaseItemStack(ItemID.SIGN);

        case BlockID.WOODEN_DOOR:
            return new BaseItemStack(ItemID.WOODEN_DOOR_ITEM);

        case BlockID.WALL_SIGN:
            return new BaseItemStack(ItemID.SIGN);

        case BlockID.IRON_DOOR:
            return new BaseItemStack(ItemID.IRON_DOOR_ITEM);

        case BlockID.REDSTONE_ORE:
        case BlockID.GLOWING_REDSTONE_ORE:
            return new BaseItemStack(ItemID.REDSTONE_DUST, (random.nextInt(2) + 4));

        case BlockID.REDSTONE_TORCH_OFF:
            return new BaseItemStack(BlockID.REDSTONE_TORCH_ON);

        case BlockID.CLAY:
            return new BaseItemStack(ItemID.CLAY_BALL, 4);

        case BlockID.REED:
            return new BaseItemStack(ItemID.SUGAR_CANE_ITEM);

        case BlockID.LIGHTSTONE:
            return new BaseItemStack(ItemID.LIGHTSTONE_DUST, (random.nextInt(3) + 2));

        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
            return new BaseItemStack(ItemID.REDSTONE_REPEATER);

        case BlockID.BROWN_MUSHROOM_CAP:
            store = random.nextInt(10);
            if (store == 0) {
                return new BaseItemStack(BlockID.BROWN_MUSHROOM, 2);
            } else if (store == 1) {
                return new BaseItemStack(BlockID.BROWN_MUSHROOM);
            } else {
                return null;
            }

        case BlockID.RED_MUSHROOM_CAP:
            store = random.nextInt(10);
            if (store == 0) {
                return new BaseItemStack(BlockID.RED_MUSHROOM, 2);
            } else if (store == 1) {
                return new BaseItemStack(BlockID.RED_MUSHROOM);
            } else {
                return null;
            }

        case BlockID.MELON_BLOCK:
            return new BaseItemStack(ItemID.MELON, (random.nextInt(5) + 3));

        case BlockID.PUMPKIN_STEM:
            return new BaseItemStack(ItemID.PUMPKIN_SEEDS);

        case BlockID.MELON_STEM:
            return new BaseItemStack(ItemID.MELON_SEEDS);

        case BlockID.MYCELIUM:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.LILY_PAD:
            return new BaseItemStack(BlockID.LILY_PAD);

        case BlockID.NETHER_WART:
            return new BaseItemStack(ItemID.NETHER_WART_SEED, random.nextInt(3) + 1);

        case BlockID.BREWING_STAND:
            return new BaseItemStack(ItemID.BREWING_STAND);

        case BlockID.CAULDRON:
            return new BaseItemStack(ItemID.CAULDRON);

        case BlockID.REDSTONE_LAMP_ON:
            return new BaseItemStack(BlockID.REDSTONE_LAMP_OFF);

        case BlockID.DOUBLE_WOODEN_STEP:
            return new BaseItemStack(BlockID.WOODEN_STEP, 2, data);

        case BlockID.COCOA_PLANT:
            return new BaseItemStack(ItemID.INK_SACK, (data >= 2 ? 3 : 1), (short) 3);

        case BlockID.EMERALD_ORE:
            return new BaseItemStack(ItemID.EMERALD);

        case BlockID.TRIPWIRE:
            return new BaseItemStack(ItemID.STRING);

        case BlockID.FLOWER_POT:
            return new BaseItemStack(ItemID.FLOWER_POT);

        case BlockID.CARROTS:
            return new BaseItemStack(ItemID.CARROT, random.nextInt(3) + 1);

        case BlockID.POTATOES:
            return new BaseItemStack(ItemID.POTATO, random.nextInt(3) + 1);

        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
            return new BaseItemStack(ItemID.COMPARATOR);

        case BlockID.QUARTZ_ORE:
            return new BaseItemStack(ItemID.NETHER_QUARTZ);

        case BlockID.QUARTZ_BLOCK:
            return new BaseItemStack(BlockID.QUARTZ_BLOCK, 1, (data >= 2 ? 2 : data));

        case BlockID.LOG:
            return new BaseItemStack(BlockID.LOG, 1, (short) (data & 0x3)); // strip orientation data

        case BlockID.HAY_BLOCK:
            return new BaseItemStack(BlockID.HAY_BLOCK); // strip orientation data

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
            return new BaseItemStack(id); // strip data from stairs

        case BlockID.BEDROCK:
        case BlockID.WATER:
        case BlockID.STATIONARY_WATER:
        case BlockID.LAVA:
        case BlockID.STATIONARY_LAVA:
        case BlockID.GLASS:
        case BlockID.STAINED_GLASS_PANE:
        case BlockID.PISTON_EXTENSION:
        case BlockID.BOOKCASE:
        case BlockID.FIRE:
        case BlockID.MOB_SPAWNER:
        case BlockID.SNOW:
        case BlockID.ICE:
        case BlockID.PORTAL:
        case BlockID.AIR:
        case BlockID.SILVERFISH_BLOCK:
        case BlockID.VINE:
        case BlockID.END_PORTAL:
        case BlockID.END_PORTAL_FRAME:
        case BlockID.HEAD:
            return null;
        }

        return new BaseItemStack(id);
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
