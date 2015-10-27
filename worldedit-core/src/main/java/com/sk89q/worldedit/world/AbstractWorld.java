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

package com.sk89q.worldedit.world;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;

import javax.annotation.Nullable;
import java.util.PriorityQueue;

/**
 * An abstract implementation of {@link World}.
 */
public abstract class AbstractWorld implements World {

    private final PriorityQueue<QueuedEffect> effectQueue = new PriorityQueue<QueuedEffect>();
    private int taskId = -1;

    @Override
    public boolean useItem(Vector position, BaseItem item, Direction face) {
        return false;
    }

    @Override
    public final boolean setBlockType(Vector position, int type) {
        try {
            return setBlock(position, new BaseBlock(type));
        } catch (WorldEditException ignored) {
            return false;
        }
    }

    @Override
    public final void setBlockData(Vector position, int data) {
        try {
            setBlock(position, new BaseBlock(getLazyBlock(position).getType(), data));
        } catch (WorldEditException ignored) {
        }
    }

    @Override
    public final boolean setTypeIdAndData(Vector position, int type, int data) {
        try {
            return setBlock(position, new BaseBlock(type, data));
        } catch (WorldEditException ignored) {
            return false;
        }
    }

    @Override
    public final boolean setBlock(Vector pt, BaseBlock block) throws WorldEditException {
        return setBlock(pt, block, true);
    }

    @Override
    public int getMaxY() {
        return getMaximumPoint().getBlockY();
    }

    @Override
    public boolean isValidBlockType(int type) {
        return BlockType.fromID(type) != null;
    }

    @Override
    public boolean usesBlockData(int type) {
        // We future proof here by assuming all unknown blocks use data
        return BlockType.usesData(type) || BlockType.fromID(type) == null;
    }

    @Override
    public Mask createLiquidMask() {
        return new BlockMask(this,
                new BaseBlock(BlockID.STATIONARY_LAVA, -1),
                new BaseBlock(BlockID.LAVA, -1),
                new BaseBlock(BlockID.STATIONARY_WATER, -1),
                new BaseBlock(BlockID.WATER, -1));
    }

    @Override
    public int getBlockType(Vector pt) {
        return getLazyBlock(pt).getType();
    }

    @Override
    public int getBlockData(Vector pt) {
        return getLazyBlock(pt).getData();
    }

    @Override
    public void dropItem(Vector pt, BaseItemStack item, int times) {
        for (int i = 0; i < times; ++i) {
            dropItem(pt, item);
        }
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        BaseBlock block = getLazyBlock(pt);
        BaseItemStack stack = BlockType.getBlockDrop(block.getId(), (short) block.getData());

        if (stack != null) {
            final int amount = stack.getAmount();
            if (amount > 1) {
                dropItem(pt, new BaseItemStack(stack.getType(), 1, stack.getData()), amount);
            } else {
                dropItem(pt, stack, amount);
            }
        }

        try {
            setBlock(pt, new BaseBlock(BlockID.AIR));
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean generateTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return generateTree(TreeType.TREE, editSession, pt);
    }

    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return generateTree(TreeType.BIG_TREE, editSession, pt);
    }

    @Override
    public boolean generateBirchTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return generateTree(TreeType.BIRCH, editSession, pt);
    }

    @Override
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return generateTree(TreeType.REDWOOD, editSession, pt);
    }

    @Override
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return generateTree(TreeType.TALL_REDWOOD, editSession, pt);
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
    }

    @Override
    public void fixLighting(Iterable<BlockVector2D> chunks) {
    }

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean queueBlockBreakEffect(Platform server, Vector position, int blockId, double priority) {
        if (taskId == -1) {
            taskId = server.schedule(0, 1, new Runnable() {
                @Override
                public void run() {
                    int max = Math.max(1, Math.min(30, effectQueue.size() / 3));
                    for (int i = 0; i < max; ++i) {
                        if (effectQueue.isEmpty()) return;

                        effectQueue.poll().play();
                    }
                }
            });
        }

        if (taskId == -1) {
            return false;
        }

        effectQueue.offer(new QueuedEffect(position, blockId, priority));

        return true;
    }

    @Override
    public Vector getMinimumPoint() {
        return new Vector(-30000000, 0, -30000000);
    }

    @Override
    public Vector getMaximumPoint() {
        return new Vector(30000000, 255, 30000000);
    }

    @Override
    public @Nullable Operation commit() {
        return null;
    }

    private class QueuedEffect implements Comparable<QueuedEffect> {
        private final Vector position;
        private final int blockId;
        private final double priority;

        private QueuedEffect(Vector position, int blockId, double priority) {
            this.position = position;
            this.blockId = blockId;
            this.priority = priority;
        }

        public void play() {
            playEffect(position, 2001, blockId);
        }

        @Override
        public int compareTo(@Nullable QueuedEffect other) {
            return Double.compare(priority, other != null ? other.priority : 0);
        }
    }

}
