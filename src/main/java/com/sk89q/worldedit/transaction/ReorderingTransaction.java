// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.transaction;

import java.lang.Thread.State;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.foundation.Extent;
import com.sk89q.worldedit.foundation.MutableExtent;
import com.sk89q.worldedit.operation.ExecutionHint;
import com.sk89q.worldedit.operation.ExecutionWatch;
import com.sk89q.worldedit.operation.Operation;
import com.sk89q.worldedit.util.ChangeList;

/**
 * A transaction that re-orders the placement of blocks so that blocks that are
 * attached to another block are placed at the end.
 */
public class ReorderingTransaction extends AbstractTransaction {

    private final Extent extent;
    private final ChangeList pass2 = new ChangeList(false);
    private final ChangeList pass3 = new ChangeList(false);

    /**
     * Create a new re-ordering transaction for the given extent.
     * 
     * @param extent the extent
     */
    public ReorderingTransaction(Extent extent) {
        this.extent = extent;
    }
    
    /**
     * Set the given block at the given location on the extent.
     * 
     * <p>Implementations can override this method to better implement
     * {@link #getApplyPhysics()} or {@link #getCalculateLighting()}. The default
     * implementation does</p>
     * 
     * @param location the location
     * @param block the block
     * @return {@link MutableExtent#setBlock(Vector, BaseBlock)}
     */
    protected boolean setBlockOnExtent(Vector location, BaseBlock block) {
        return extent.setBlock(location, block);
    }
    
    /**
     * Flush the given chunk.
     * 
     * @param chunk the chunk
     */
    protected void flushChunk(BlockVector2D chunk) {
    }
    
    @Override
    public final boolean setBlock(Vector location, BaseBlock block) {
        // Place torches, etc. last
        if (BlockType.shouldPlaceLast(block.getType())) {
            pass2.put(location.toBlockVector(), block);
            return !isExtentBlockEqual(location, block);

        // Place signs, reed, etc even later
        } else if (BlockType.shouldPlaceFinal(block.getType())) {
            pass3.put(location.toBlockVector(), block);
            return !isExtentBlockEqual(location, block);

        // Destroy torches, etc. first
        } else if (BlockType.shouldPlaceLast(extent.getBlockType(location))) {
            setBlockOnExtent(location, new BaseBlock(BlockID.AIR));
            return setBlockOnExtent(location, block);

        } else {
            return setBlockOnExtent(location, block);
        }
    }

    @Override
    public Operation getFlushOperation() {
        return new FlushOperation();
    }
    
    /**
     * Return whether the given block at the given location is the same (ID and data
     * value) as the block at the same location in the extent.
     * 
     * @param location the block location
     * @param block the block to test against
     * @return true if the block is the same
     */
    private boolean isExtentBlockEqual(Vector location, BaseBlock block) {
        return extent.getBlockType(location) == block.getType() && extent
                .getBlockData(location) == block.getData();
    }
    
    /**
     * Flushes changes to the underlying {@link Extent}.
     */
    private class FlushOperation implements Operation, Runnable {
        
        private Thread thread;
        private final Iterator<Entry<BlockVector, BaseBlock>> pass2Iterator = pass2.iterator();
        private final Set<BlockVector2D> dirtyChunks = new HashSet<BlockVector2D>();
        private final Set<BlockVector> blocks = new HashSet<BlockVector>();
        private final Map<BlockVector, BaseBlock> blockTypes = new HashMap<BlockVector, BaseBlock>();
        private final Deque<BlockVector> walked = new LinkedList<BlockVector>();
        private boolean asyncTaskCompleted = false;
        private Iterator<BlockVector2D> dirtyChunksIterator;
        private int pass = 1;
        
        /**
         * Processes {@link ReorderingTransaction#pass1}.
         * 
         * @param opt the execution hint
         * @return true if this pass has completed
         */
        private boolean runPass1(ExecutionHint opt) {
            return true; // Pass 1 removed
        }

        /**
         * Processes {@link ReorderingTransaction#pass2}.
         * 
         * @param opt the execution hint
         * @return true if this pass has completed
         * @throws InterruptedException on interruption
         */
        private boolean runPass2(ExecutionHint opt) throws InterruptedException {
            ExecutionWatch watch = opt.createWatch();
            
            while (pass2Iterator.hasNext() && watch.shouldContinue()) {
                Entry<BlockVector, BaseBlock> entry = pass2Iterator.next();
                BlockVector pt = entry.getKey();
                setBlockOnExtent(pt, entry.getValue());

                // TODO: use ChunkStore.toChunk(pt) after optimizing it.
                if (!getApplyPhysics()) {
                    dirtyChunks.add(new BlockVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4));
                }
            }
            
            return !pass2Iterator.hasNext();
        }

        /**
         * Processes {@link ReorderingTransaction#pass3}.
         * 
         * <p>This can only run after the asynchronous task is done.</p>
         * 
         * @param opt the execution hint
         * @return true if this pass has completed
         * @throws InterruptedException on interruption
         */
        private boolean runPass3Sync(ExecutionHint opt) throws InterruptedException {
            ExecutionWatch watch = opt.createWatch();
            
            while (!blocks.isEmpty() && watch.shouldContinue()) {
                BlockVector current = blocks.iterator().next();
                
                if (!blocks.contains(current)) {
                    continue;
                }

                while (true) {
                    walked.addFirst(current);

                    assert(blockTypes.containsKey(current));

                    final BaseBlock baseBlock = blockTypes.get(current);

                    final int type = baseBlock.getType();
                    final int data = baseBlock.getData();

                    switch (type) {
                    case BlockID.WOODEN_DOOR:
                    case BlockID.IRON_DOOR:
                        if ((data & 0x8) == 0) {
                            // Deal with lower door halves being attached to the floor AND the upper half
                            BlockVector upperBlock = current.add(0, 1, 0).toBlockVector();
                            if (blocks.contains(upperBlock) && !walked.contains(upperBlock)) {
                                walked.addFirst(upperBlock);
                            }
                        }
                    }

                    final PlayerDirection attachment = BlockType.getAttachment(type, data);
                    if (attachment == null) {
                        // Block is not attached to anything => we can place it
                        break;
                    }

                    current = current.add(attachment.vector()).toBlockVector();

                    if (!blocks.contains(current)) {
                        // We ran outside the remaing set => assume we can place blocks on this
                        break;
                    }

                    if (walked.contains(current)) {
                        // Cycle detected => This will most likely go wrong, but there's nothing we can do about it.
                        break;
                    }
                }

                for (BlockVector pt : walked) {
                    setBlockOnExtent(pt, blockTypes.get(pt));
                    blocks.remove(pt);

                    // TODO: use ChunkStore.toChunk(pt) after optimizing it.
                    if (!getApplyPhysics()) {
                        dirtyChunks.add(new BlockVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4));
                    }
                }
            }
            
            return blocks.isEmpty();
        }
        
        /**
         * Run at the end to flush chunks.
         * 
         * @param opt the execution hint
         * @return true if this pass has completed
         * @throws InterruptedException on interruption
         */
        private boolean runChunkFlush(ExecutionHint opt) throws InterruptedException {
            if (dirtyChunksIterator == null) {
                dirtyChunksIterator = dirtyChunks.iterator();
            }

            ExecutionWatch watch = opt.createWatch();
            
            while (dirtyChunksIterator.hasNext() && watch.shouldContinue()) {
                flushChunk(dirtyChunksIterator.next());
            }
            
            return !dirtyChunksIterator.hasNext();
        }

        @Override
        public void run() {
            for (Map.Entry<BlockVector, BaseBlock> entry : pass3) {
                final BlockVector pt = entry.getKey();
                blocks.add(pt);
                blockTypes.put(pt, entry.getValue());

                if (Thread.interrupted()) {
                    return;
                }
            }
            
            asyncTaskCompleted = true;
        }
        
        @Override
        public Operation resume(ExecutionHint opt) throws InterruptedException {
            if (thread == null) {
                thread = new Thread(this, FlushOperation.class.getCanonicalName());
                thread.start();
            }
            
            switch (pass) {
            case 1:
                if (runPass1(opt)) {
                    pass++;
                }
                return this;
            case 2:
                if (runPass2(opt)) {
                    pass++;
                }
                return this;
            case 3:
                if (!asyncTaskCompleted) {
                    if (thread.getState() == State.TERMINATED) {
                        throw new RuntimeException("Async pass 3 thread died");
                    }
                    
                    return this;
                }
                
                if (runPass3Sync(opt)) {
                    pass++;
                }
                return this;
            case 4:
                if (runChunkFlush(opt)) {
                    pass2.clear();
                    pass3.clear();
                    
                    return null;
                }
                return this;
            default:
                return null;
            }
        }
        
        @Override
        public void cancel() {
            if (thread != null) {
                thread.interrupt();
            }
        }
        
    }
    
}
