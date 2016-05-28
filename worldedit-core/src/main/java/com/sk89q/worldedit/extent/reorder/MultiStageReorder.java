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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.BlockMapEntryPlacer;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.util.collection.LowMemoryTupleArrayList;

import java.util.*;

/**
 * Re-orders blocks into several stages.
 */
public class MultiStageReorder extends AbstractDelegateExtent implements ReorderingExtent {

    private static final int STAGE_COUNT = 4;

    private final List<LowMemoryTupleArrayList<BlockVector, BaseBlock>> stages = new ArrayList<LowMemoryTupleArrayList<BlockVector, BaseBlock>>();
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
            stages.add(new LowMemoryTupleArrayList<BlockVector, BaseBlock>());
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

    public int getPlacementPriority(BaseBlock block) {
        if (BlockType.shouldPlaceLate(block.getType())) {
            return 1;
        } else if (BlockType.shouldPlaceLast(block.getType())) {
            // Place torches, etc. last
            return 2;
        } else if (BlockType.shouldPlaceFinal(block.getType())) {
            // Place signs, reed, etc even later
            return 3;
        } else {
            return 0;
        }
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        BaseBlock lazyBlock = getLazyBlock(location);

        if (!enabled) {
            return super.setBlock(location, block);
        }

        int priority = getPlacementPriority(block);
        int srcPriority = getPlacementPriority(lazyBlock);

        // Destroy torches, water, stand, etc. first
        if (srcPriority == 1 || srcPriority == 2) {
            // Destroy torches, etc. first
            super.setBlock(location, WorldEdit.getInstance().getBaseBlockFactory().getBaseBlock(BlockID.AIR));
            return super.setBlock(location, block);
        }

        stages.get(priority).put(location.toBlockPoint(), block);
        return !(lazyBlock.getType() == block.getType() && lazyBlock.getData() == block.getData());
    }

    @Override
    public Operation commitBefore() {
        List<Operation> operations = new ArrayList<Operation>();
        for (int i = 0; i < stages.size() - 1; ++i) {
            operations.add(new BlockMapEntryPlacer(getExtent(), stages.get(i).iterator()));
        }
        operations.add(new FinalStageCommitter());

        return new OperationQueue(operations);
    }

    private class FinalStageCommitter implements Operation {
        private Extent extent = getExtent();

        private final Set<BlockVector> blocks = new HashSet<BlockVector>();
        private final Map<BlockVector, BaseBlock> blockTypes = new HashMap<BlockVector, BaseBlock>();

        public FinalStageCommitter() {
            for (Map.Entry<BlockVector, BaseBlock> entry : stages.get(stages.size() - 1)) {
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

                final Deque<BlockVector> walked = new LinkedList<BlockVector>();

                while (true) {
                    walked.addFirst(current);

                    assert (blockTypes.containsKey(current));

                    final BaseBlock baseBlock = blockTypes.get(current);

                    final int type = baseBlock.getType();
                    final int data = baseBlock.getData();

                    switch (type) {
                        case BlockID.WOODEN_DOOR:
                        case BlockID.ACACIA_DOOR:
                        case BlockID.BIRCH_DOOR:
                        case BlockID.JUNGLE_DOOR:
                        case BlockID.DARK_OAK_DOOR:
                        case BlockID.SPRUCE_DOOR:
                        case BlockID.IRON_DOOR:
                            if ((data & 0x8) == 0) {
                                // Deal with lower door halves being attached to the floor AND the upper half
                                BlockVector upperBlock = current.add(0, 1, 0).toBlockVector();
                                if (blocks.contains(upperBlock) && !walked.contains(upperBlock)) {
                                    walked.addFirst(upperBlock);
                                }
                            }
                            break;

                        case BlockID.MINECART_TRACKS:
                        case BlockID.POWERED_RAIL:
                        case BlockID.DETECTOR_RAIL:
                        case BlockID.ACTIVATOR_RAIL:
                            // Here, rails are hardcoded to be attached to the block below them.
                            // They're also attached to the block they're ascending towards via BlockType.getAttachment.
                            BlockVector lowerBlock = current.add(0, -1, 0).toBlockVector();
                            if (blocks.contains(lowerBlock) && !walked.contains(lowerBlock)) {
                                walked.addFirst(lowerBlock);
                            }
                            break;
                    }

                    final PlayerDirection attachment = BlockType.getAttachment(type, data);
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

            if (blocks.isEmpty()) {
                for (LowMemoryTupleArrayList<BlockVector, BaseBlock> stage : stages) {
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
