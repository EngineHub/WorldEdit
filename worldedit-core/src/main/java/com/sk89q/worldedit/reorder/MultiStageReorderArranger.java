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

package com.sk89q.worldedit.reorder;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.action.BlockPlacement;
import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;
import com.sk89q.worldedit.reorder.arrange.SimpleAttributeKey;
import com.sk89q.worldedit.reorder.buffer.MutableArrayWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.MutableWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Re-orders blocks into several stages.
 */
public final class MultiStageReorderArranger implements Arranger {

    private static final Map<BlockType, PlacementPriority> priorityMap = new HashMap<>();

    static {
        // Late
        priorityMap.put(BlockTypes.WATER, PlacementPriority.PHYSICS);
        priorityMap.put(BlockTypes.LAVA, PlacementPriority.PHYSICS);
        priorityMap.put(BlockTypes.SAND, PlacementPriority.PHYSICS);
        priorityMap.put(BlockTypes.GRAVEL, PlacementPriority.PHYSICS);

        // Late
        BlockCategories.SAPLINGS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.FLOWER_POTS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.BUTTONS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.ANVIL.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.WOODEN_PRESSURE_PLATES.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.CARPETS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.RAILS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.BEDS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        BlockCategories.SMALL_FLOWERS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.BLOCK_DEPENDENT));
        priorityMap.put(BlockTypes.BLACK_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.BLUE_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.BROWN_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.CYAN_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.GRAY_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.GREEN_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LIGHT_BLUE_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LIGHT_GRAY_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LIME_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.MAGENTA_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.ORANGE_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.PINK_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.PURPLE_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.RED_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.WHITE_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.YELLOW_BED, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.GRASS, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.TALL_GRASS, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.ROSE_BUSH, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.DANDELION, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.BROWN_MUSHROOM, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.RED_MUSHROOM, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.FERN, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LARGE_FERN, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.OXEYE_DAISY, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.AZURE_BLUET, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.TORCH, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.WALL_TORCH, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.FIRE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.REDSTONE_WIRE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.CARROTS, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.POTATOES, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.WHEAT, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.BEETROOTS, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.COCOA, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LADDER, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LEVER, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.REDSTONE_TORCH, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.REDSTONE_WALL_TORCH, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.SNOW, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.NETHER_PORTAL, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.END_PORTAL, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.REPEATER, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.VINE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LILY_PAD, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.NETHER_WART, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.PISTON, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.STICKY_PISTON, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.TRIPWIRE_HOOK, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.TRIPWIRE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.STONE_PRESSURE_PLATE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.COMPARATOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.IRON_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.ACACIA_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.BIRCH_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.DARK_OAK_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.JUNGLE_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.OAK_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.SPRUCE_TRAPDOOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.DAYLIGHT_DETECTOR, PlacementPriority.BLOCK_DEPENDENT);
        priorityMap.put(BlockTypes.CAKE, PlacementPriority.BLOCK_DEPENDENT);

        // Final
        BlockCategories.DOORS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.FINAL));
        BlockCategories.BANNERS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.FINAL));
        BlockCategories.SIGNS.getAll().forEach(type -> priorityMap.put(type, PlacementPriority.FINAL));
        priorityMap.put(BlockTypes.SIGN, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.WALL_SIGN, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.CACTUS, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.SUGAR_CANE, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.PISTON_HEAD, PlacementPriority.FINAL);
        priorityMap.put(BlockTypes.MOVING_PISTON, PlacementPriority.FINAL);
    }

    public enum PlacementPriority {
        CLEAR_FINAL,
        CLEAR_BLOCK_DEPENDENT,
        CLEAR_PHYSICS,
        STATIC,
        PHYSICS,
        BLOCK_DEPENDENT,
        FINAL
    }

    /**
     * Gets the stage priority of the block.
     *
     * @param block The block
     * @return The priority
     */
    private static <B extends BlockStateHolder<B>> PlacementPriority getPlacementPriority(B block) {
        return priorityMap.getOrDefault(block.getBlockType(), PlacementPriority.STATIC);
    }

    private static final SimpleAttributeKey<Map<PlacementPriority, List<BlockPlacement>>> STAGES =
        SimpleAttributeKey.create("stages", () -> {
            Map<PlacementPriority, List<BlockPlacement>> stages = new HashMap<>();
            for (PlacementPriority priority : PlacementPriority.values()) {
                stages.put(priority, new ArrayList<>());
            }
            return stages;
        });
    private static final SimpleAttributeKey<Boolean> IMMEDIATE_PENDING =
        SimpleAttributeKey.create("immediatePending", () -> false);

    @Override
    public void onWrite(ArrangerContext context, WorldActionBuffer buffer) {
        Map<PlacementPriority, List<BlockPlacement>> stages = STAGES.get(context);
        MutableWorldActionBuffer copy = MutableArrayWorldActionBuffer.allocate(buffer.remaining());
        while (buffer.hasRemaining()) {
            WorldAction placement = buffer.get();
            if (placement instanceof BlockPlacement) {
                BlockPlacement bp = (BlockPlacement) placement;
                BlockPlacement immediate = reorder(stages, bp);
                if (immediate != null) {
                    copy.put(immediate);
                }
            } else {
                copy.put(placement);
            }
        }
        copy.flip();
        if (copy.hasRemaining()) {
            context.write(copy);
            IMMEDIATE_PENDING.set(context, true);
        }
    }

    private BlockPlacement reorder(Map<PlacementPriority, List<BlockPlacement>> stages, BlockPlacement placement) {
        BlockVector3 location = placement.getPosition();
        BaseBlock block = placement.getBlock();
        BlockState existing = placement.getOldBlock().toImmutableState();
        PlacementPriority priority = getPlacementPriority(block);
        PlacementPriority srcPriority = getPlacementPriority(existing);
        BlockPlacement immediate = null;

        if (srcPriority != PlacementPriority.STATIC) {
            BaseBlock replacement = (block.getBlockType().getMaterial().isAir() ? block : BlockTypes.AIR.getDefaultState()).toBaseBlock();
            BlockPlacement replBp = BlockPlacement.create(location, block, replacement, ImmutableList.of());

            switch (srcPriority) {
                case FINAL:
                    // we can push this out earlier
                    immediate = replBp;
                    break;
                case PHYSICS:
                    stages.get(PlacementPriority.CLEAR_PHYSICS).add(replBp);
                    break;
                case BLOCK_DEPENDENT:
                    stages.get(PlacementPriority.CLEAR_BLOCK_DEPENDENT).add(replBp);
                    break;
            }
        }

        stages.get(priority).add(placement);
        return immediate;
    }

    @Override
    public void onFlush(ArrangerContext context) {
        if (IMMEDIATE_PENDING.get(context)) {
            context.flush();
            IMMEDIATE_PENDING.set(context, false);
        }
        Map<PlacementPriority, List<BlockPlacement>> stages = STAGES.get(context);
        for (PlacementPriority priority : PlacementPriority.values()) {
            List<BlockPlacement> blocks = stages.get(priority);
            context.write(MutableArrayWorldActionBuffer.wrap(
                blocks.toArray(new WorldAction[0])
            ));
            context.flush();
            blocks.clear();
        }
    }

}
