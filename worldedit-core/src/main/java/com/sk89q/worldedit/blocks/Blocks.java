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

import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Block-related utility methods.
 */
public final class Blocks {

    private Blocks() {
    }

    /**
     * HashSet for shouldPlaceLate.
     */
    private static final Set<BlockType> shouldPlaceLate = new HashSet<>();
    static {
        shouldPlaceLate.add(BlockTypes.WATER);
        shouldPlaceLate.add(BlockTypes.LAVA);
        shouldPlaceLate.add(BlockTypes.GRAVEL);
        shouldPlaceLate.add(BlockTypes.SAND);
    }
    /**
     * Checks to see whether a block should be placed in the final queue.
     *
     * This applies to blocks that can be attached to other blocks that have an attachment.
     *
     * @param type the type of the block
     * @return whether the block is in the late queue
     */
    public static boolean shouldPlaceLate(BlockType type) {
        return shouldPlaceLate.contains(type);
    }

    /**
     * HashSet for shouldPlaceLast.
     */
    private static final Set<BlockType> shouldPlaceLast = new HashSet<>();
    static {
        shouldPlaceLast.addAll(BlockCategories.SAPLINGS.getAll());
        shouldPlaceLast.addAll(BlockCategories.FLOWER_POTS.getAll());
        shouldPlaceLast.addAll(BlockCategories.BUTTONS.getAll());
        shouldPlaceLast.addAll(BlockCategories.ANVIL.getAll()); // becomes relevant with asynchronous placement
        shouldPlaceLast.addAll(BlockCategories.WOODEN_PRESSURE_PLATES.getAll());
        shouldPlaceLast.addAll(BlockCategories.CARPETS.getAll());
        shouldPlaceLast.addAll(BlockCategories.RAILS.getAll());
        shouldPlaceLast.add(BlockTypes.BLACK_BED);
        shouldPlaceLast.add(BlockTypes.BLUE_BED);
        shouldPlaceLast.add(BlockTypes.BROWN_BED);
        shouldPlaceLast.add(BlockTypes.CYAN_BED);
        shouldPlaceLast.add(BlockTypes.GRAY_BED);
        shouldPlaceLast.add(BlockTypes.GREEN_BED);
        shouldPlaceLast.add(BlockTypes.LIGHT_BLUE_BED);
        shouldPlaceLast.add(BlockTypes.LIGHT_GRAY_BED);
        shouldPlaceLast.add(BlockTypes.LIME_BED);
        shouldPlaceLast.add(BlockTypes.MAGENTA_BED);
        shouldPlaceLast.add(BlockTypes.ORANGE_BED);
        shouldPlaceLast.add(BlockTypes.PINK_BED);
        shouldPlaceLast.add(BlockTypes.PURPLE_BED);
        shouldPlaceLast.add(BlockTypes.RED_BED);
        shouldPlaceLast.add(BlockTypes.WHITE_BED);
        shouldPlaceLast.add(BlockTypes.YELLOW_BED);
        shouldPlaceLast.add(BlockTypes.GRASS);
        shouldPlaceLast.add(BlockTypes.TALL_GRASS);
        shouldPlaceLast.add(BlockTypes.ROSE_BUSH);
        shouldPlaceLast.add(BlockTypes.DANDELION);
        shouldPlaceLast.add(BlockTypes.BROWN_MUSHROOM);
        shouldPlaceLast.add(BlockTypes.RED_MUSHROOM);
        shouldPlaceLast.add(BlockTypes.FERN);
        shouldPlaceLast.add(BlockTypes.LARGE_FERN);
        shouldPlaceLast.add(BlockTypes.OXEYE_DAISY);
        shouldPlaceLast.add(BlockTypes.AZURE_BLUET);
        shouldPlaceLast.add(BlockTypes.TORCH);
        shouldPlaceLast.add(BlockTypes.WALL_TORCH);
        shouldPlaceLast.add(BlockTypes.FIRE);
        shouldPlaceLast.add(BlockTypes.REDSTONE_WIRE);
        shouldPlaceLast.add(BlockTypes.CARROTS);
        shouldPlaceLast.add(BlockTypes.POTATOES);
        shouldPlaceLast.add(BlockTypes.WHEAT);
        shouldPlaceLast.add(BlockTypes.BEETROOTS);
        shouldPlaceLast.add(BlockTypes.COCOA);
        shouldPlaceLast.add(BlockTypes.LADDER);
        shouldPlaceLast.add(BlockTypes.LEVER);
        shouldPlaceLast.add(BlockTypes.REDSTONE_TORCH);
        shouldPlaceLast.add(BlockTypes.REDSTONE_WALL_TORCH);
        shouldPlaceLast.add(BlockTypes.SNOW);
        shouldPlaceLast.add(BlockTypes.NETHER_PORTAL);
        shouldPlaceLast.add(BlockTypes.END_PORTAL);
        shouldPlaceLast.add(BlockTypes.REPEATER);
        shouldPlaceLast.add(BlockTypes.VINE);
        shouldPlaceLast.add(BlockTypes.LILY_PAD);
        shouldPlaceLast.add(BlockTypes.NETHER_WART);
        shouldPlaceLast.add(BlockTypes.PISTON);
        shouldPlaceLast.add(BlockTypes.STICKY_PISTON);
        shouldPlaceLast.add(BlockTypes.TRIPWIRE_HOOK);
        shouldPlaceLast.add(BlockTypes.TRIPWIRE);
        shouldPlaceLast.add(BlockTypes.STONE_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockTypes.COMPARATOR);
        shouldPlaceLast.add(BlockTypes.IRON_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.ACACIA_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.BIRCH_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.DARK_OAK_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.JUNGLE_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.OAK_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.SPRUCE_TRAPDOOR);
        shouldPlaceLast.add(BlockTypes.DAYLIGHT_DETECTOR);
    }

    /**
     * Checks to see whether a block should be placed last (when reordering
     * blocks that are placed).
     *
     * @param type the block type
     * @return true if the block should be placed last
     */
    public static boolean shouldPlaceLast(BlockType type) {
        return shouldPlaceLast.contains(type);
    }

    /**
     * HashSet for shouldPlaceLast.
     */
    private static final Set<BlockType> shouldPlaceFinal = new HashSet<>();
    static {
        shouldPlaceFinal.addAll(BlockCategories.DOORS.getAll());
        shouldPlaceFinal.addAll(BlockCategories.BANNERS.getAll());
        shouldPlaceFinal.add(BlockTypes.SIGN);
        shouldPlaceFinal.add(BlockTypes.WALL_SIGN);
        shouldPlaceFinal.add(BlockTypes.CACTUS);
        shouldPlaceFinal.add(BlockTypes.SUGAR_CANE);
        shouldPlaceFinal.add(BlockTypes.CAKE);
        shouldPlaceFinal.add(BlockTypes.PISTON_HEAD);
        shouldPlaceFinal.add(BlockTypes.MOVING_PISTON);
    }

    /**
     * Checks to see whether a block should be placed in the final queue.
     *
     * This applies to blocks that can be attached to other blocks that have an attachment.
     *
     * @param type the type of the block
     * @return whether the block is in the final queue
     */
    public static boolean shouldPlaceFinal(BlockType type) {
        return shouldPlaceFinal.contains(type);
    }

    /**
     * Checks whether a given block is in a list of base blocks.
     *
     * @param collection the collection
     * @param o the block
     * @return true if the collection contains the given block
     */
    public static boolean containsFuzzy(Collection<? extends BlockStateHolder> collection, BlockStateHolder o) {
        // Allow masked data in the searchBlocks to match various types
        for (BlockStateHolder b : collection) {
            if (b.equalsFuzzy(o)) {
                return true;
            }
        }
        return false;
    }

}
