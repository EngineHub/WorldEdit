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

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BlockType;
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

    private TupleArrayList<BlockVector, BlockStateHolder> stage1 = new TupleArrayList<>();
    private TupleArrayList<BlockVector, BlockStateHolder> stage2 = new TupleArrayList<>();
    private TupleArrayList<BlockVector, BlockStateHolder> stage3 = new TupleArrayList<>();
    private boolean enabled;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param enabled true to enable
     */
    public MultiStageReorder(Extent extent, boolean enabled) {
        super(extent);
        this.enabled = enabled;
    }

    /**
     * Create a new instance when the re-ordering is enabled.
     *
     * @param extent the extent
     */
    public MultiStageReorder(Extent extent) {
        this(extent, true);
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

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        BlockState existing = getBlock(location);

        if (!enabled) {
            return super.setBlock(location, block);
        }

        if (Blocks.shouldPlaceLast(block.getBlockType())) {
            // Place torches, etc. last
            stage2.put(location.toBlockVector(), block);
            return !existing.equalsFuzzy(block);
        } else if (Blocks.shouldPlaceFinal(block.getBlockType())) {
            // Place signs, reed, etc even later
            stage3.put(location.toBlockVector(), block);
            return !existing.equalsFuzzy(block);
        } else if (Blocks.shouldPlaceLast(existing.getBlockType())) {
            // Destroy torches, etc. first
            super.setBlock(location, BlockTypes.AIR.getDefaultState());
            return super.setBlock(location, block);
        } else {
            stage1.put(location.toBlockVector(), block);
            return !existing.equalsFuzzy(block);
        }
    }

    @Override
    public Operation commitBefore() {
        return new OperationQueue(
                new BlockMapEntryPlacer(
                        getExtent(),
                        Iterators.concat(stage1.iterator(), stage2.iterator())),
                new Stage3Committer());
    }

    private class Stage3Committer implements Operation {

        @Override
        public Operation resume(RunContext run) throws WorldEditException {
            Extent extent = getExtent();

            final Set<BlockVector> blocks = new HashSet<>();
            final Map<BlockVector, BlockStateHolder> blockTypes = new HashMap<>();
            for (Map.Entry<BlockVector, BlockStateHolder> entry : stage3) {
                final BlockVector pt = entry.getKey();
                blocks.add(pt);
                blockTypes.put(pt, entry.getValue());
            }

            while (!blocks.isEmpty()) {
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

                    final PlayerDirection attachment = BlockType.getAttachment(blockStateHolder.getBlockType().getLegacyId(), 0); // TODO
                    if (attachment == null) {
                        // Block is not attached to anything => we can place it
                        break;
                    }

                    current = current.add(attachment.vector()).toBlockVector();

                    if (!blocks.contains(current)) {
                        // We ran outside the remaining set => assume we can place blocks on this
                        break;
                    }

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

            stage1.clear();
            stage2.clear();
            stage3.clear();

            return null;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void addStatusMessages(List<String> messages) {
        }

    }

}
