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

package com.sk89q.worldedit.extent.reorder;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.BlockMapEntryPlacer;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.collection.TupleArrayList;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Re-orders blocks into several stages.
 */
public class MultiStageReorder extends AbstractDelegateExtent implements ReorderingExtent {

    private static final int STAGE_COUNT = 4;

    private final List<TupleArrayList<BlockVector, BlockStateHolder>> stages = new ArrayList<>();

    private boolean enabled;

    /**
     * Create a new instance when the re-ordering is enabled.
     *
     * @param extent the extent
     */
    public MultiStageReorder(Extent extent) {
        this(extent, true);
    }

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param enabled true to enable
     */
    public MultiStageReorder(Extent extent, boolean enabled) {
        super(extent);
        this.enabled = enabled;

        for (int i = 0; i < STAGE_COUNT; ++i) {
            stages.add(new TupleArrayList<>());
        }
    }

    /**
     * Return whether re-ordering is enabled.
     *
     * @return true if re-ordering is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether re-ordering is enabled.
     *
     * @param enabled true if re-ordering is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPlacementPriority(BlockStateHolder block) {
        if (Blocks.shouldPlaceLate(block.getBlockType())) {
            return 1;
        } else if (Blocks.shouldPlaceLast(block.getBlockType())) {
            // Place torches, etc. last
            return 2;
        } else if (Blocks.shouldPlaceFinal(block.getBlockType())) {
            // Place signs, reed, etc even later
            return 3;
        } else {
            return 0;
        }
    }

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        if (!enabled) {
            return super.setBlock(location, block);
        }

        BlockState existing = getBlock(location);

        int priority = getPlacementPriority(block);
        int srcPriority = getPlacementPriority(block);
        // Destroy torches, water, stand, etc. first
        if (srcPriority == 1 || srcPriority == 2) {
            // Destroy torches, etc. first
            super.setBlock(location, BlockTypes.AIR.getDefaultState());
            return super.setBlock(location, block);
        }

        stages.get(priority).put(location.toBlockPoint(), block);
        return !existing.equalsFuzzy(block);
    }

    @Override
    public Operation commitBefore() {
        List<Operation> operations = new ArrayList<>();
        for (int i = 0; i < stages.size() - 1; ++i) {
            operations.add(new BlockMapEntryPlacer(getExtent(), stages.get(i).iterator()));
        }
        operations.add(new FinalStageCommitter());
        return new OperationQueue(operations);
    }

    private class FinalStageCommitter implements Operation {
        private Extent extent = getExtent();

        private final Set<BlockVector> blocks = new HashSet<>();
        private final Map<BlockVector, BlockStateHolder> blockTypes = new HashMap<>();

        public FinalStageCommitter() {
            for (Map.Entry<BlockVector, BlockStateHolder> entry : stages.get(stages.size() - 1)) {
                final BlockVector pt = entry.getKey();
                blocks.add(pt);
                blockTypes.put(pt, entry.getValue());
            }
        }

        @Override
        public Operation resume(RunContext run) throws WorldEditException {
            while (run.shouldContinue() && !blocks.isEmpty()) {
                BlockVector current = blocks.iterator().next();
                if (!blocks.contains(current)) {
                    continue;
                }

                final Deque<BlockVector> walked = new LinkedList<>();

                while (true) {
                    walked.addFirst(current);

                    assert (blockTypes.containsKey(current));

                    final BlockStateHolder blockStateHolder = blockTypes.get(current);

                    if (BlockCategories.DOORS.contains(blockStateHolder.getBlockType())) {
                        Property<Object> halfProperty = blockStateHolder.getBlockType().getProperty("half");
                        if (blockStateHolder.getState(halfProperty).equals("lower")) {
                            // Deal with lower door halves being attached to the floor AND the upper half
                            BlockVector upperBlock = current.add(0, 1, 0).toBlockVector();
                            if (blocks.contains(upperBlock) && !walked.contains(upperBlock)) {
                                walked.addFirst(upperBlock);
                            }
                        }
                    } else if (BlockCategories.RAILS.contains(blockStateHolder.getBlockType())) {
                        BlockVector lowerBlock = current.add(0, -1, 0).toBlockVector();
                        if (blocks.contains(lowerBlock) && !walked.contains(lowerBlock)) {
                            walked.addFirst(lowerBlock);
                        }
                    }

                    if (!blockStateHolder.getBlockType().getMaterial().isFragileWhenPushed()) {
                        // Block is not attached to anything => we can place it
                        break;
                    }

//                    current = current.add(attachment.vector()).toBlockVector();
//
//                    if (!blocks.contains(current)) {
//                        // We ran outside the remaining set => assume we can place blocks on this
//                        break;
//                    }
//
                    if (walked.contains(current)) {
                        // Cycle detected => This will most likely go wrong, but there's nothing we can do about it.
                        break;
                    }
                }

                for (BlockVector pt : walked) {
                    extent.setBlock(pt, blockTypes.get(pt));
                    blocks.remove(pt);
                }
            }

            if (blocks.isEmpty()) {
                for (TupleArrayList<BlockVector, BlockStateHolder> stage : stages) {
                    stage.clear();
                }
                return null;
            }

            return this;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void addStatusMessages(List<String> messages) {
        }

    }

}
