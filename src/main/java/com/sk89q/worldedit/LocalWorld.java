// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit;

import java.util.PriorityQueue;
import java.util.Random;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.regions.Region;

/**
 * Represents a world.
 * 
 * @author sk89q
 */
public abstract class LocalWorld {
    /**
     * Named flags to use as parameters to {@link LocalWorld#killMobs(Vector, int, int)}
     */
    public class KillFlags {
        public static final int PETS = 1 << 0;
        public static final int NPCS = 1 << 1;
        public static final int ANIMALS = 1 << 2;
        public static final int WITH_LIGHTNING = 1 << 20;
    }

    /**
     * Random generator.
     */
    protected Random random = new Random();

    /**
     * Get the name of the world.
     * 
     * @return
     */
    public abstract String getName();

    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    public abstract boolean setBlockType(Vector pt, int type);

    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    public boolean setBlockTypeFast(Vector pt, int type) {
        return setBlockType(pt, type);
    }

    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockType(Vector pt);

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */

    public abstract void setBlockData(Vector pt, int data);

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */
    public abstract void setBlockDataFast(Vector pt, int data);

    /**
     * set block type & data
     * @param pt
     * @param type
     * @param data
     * @return
     */
    public boolean setTypeIdAndData(Vector pt, int type, int data) {
        boolean ret = setBlockType(pt, type);
        setBlockData(pt, data);
        return ret;
    }

    /**
     * set block type & data
     * @param pt
     * @param type
     * @param data
     * @return 
     */
    public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
        boolean ret = setBlockTypeFast(pt, type);
        setBlockDataFast(pt, data);
        return ret;
    }

    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockData(Vector pt);

    /**
     * Get block light level.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockLightLevel(Vector pt);

    /**
     * Regenerate an area.
     * 
     * @param region
     * @param editSession
     * @return
     */
    public abstract boolean regenerate(Region region, EditSession editSession);

    /**
     * Attempts to accurately copy a BaseBlock's extra data to the world.
     * 
     * @param pt
     * @param block
     * @return
     */
    public abstract boolean copyToWorld(Vector pt, BaseBlock block);

    /**
     * Attempts to read a BaseBlock's extra data from the world.
     * 
     * @param pt
     * @param block
     * @return
     */
    public abstract boolean copyFromWorld(Vector pt, BaseBlock block);

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     * @return
     */
    public abstract boolean clearContainerBlockContents(Vector pt);

    /**
     * Generate a tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a big tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateBigTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a birch tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateBirchTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a redwood tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateRedwoodTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a tall redwood tree at a location.
     * 
     * @param editSession 
     * @param pt
     * @return
     * @throws MaxChangedBlocksException 
     */
    public abstract boolean generateTallRedwoodTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Drop an item.
     * 
     * @param pt
     * @param item 
     * @param times
     */
    public void dropItem(Vector pt, BaseItemStack item, int times) {
        for (int i = 0; i < times; ++i) {
            dropItem(pt, item);
        }
    }

    /**
     * Drop an item.
     * 
     * @param pt
     * @param item
     */
    public abstract void dropItem(Vector pt, BaseItemStack item);

    /**
     * Simulate a block being mined.
     * 
     * @param pt
     */
    public void simulateBlockMine(Vector pt) {
        BaseItemStack stack = BlockType.getBlockDrop(getBlockType(pt), (short) getBlockData(pt));
        if (stack == null) {
            return;
        }

        final int amount = stack.getAmount();
        if (amount > 1) {
            dropItem(pt, new BaseItemStack(stack.getType(), 1, stack.getDamage()), amount);
        } else {
            dropItem(pt, stack, amount);
        }
    }

    /**
     * Kill mobs in an area, excluding pet wolves.
     * 
     * @param origin
     * @param radius
     * @return
     */
    @Deprecated
    public int killMobs(Vector origin, int radius) {
        return killMobs(origin, radius, false);
    }

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius -1 for all mobs
     * @param flags various flags that determine what to kill
     * @return
     */
    @Deprecated
    public int killMobs(Vector origin, int radius, boolean killPets) {
        return killMobs(origin, radius, killPets ? KillFlags.PETS : 0);
    }

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius
     * @param killflags
     * @return
     */
    public int killMobs(Vector origin, double radius, int flags) {
        return killMobs(origin, (int) radius, (flags & KillFlags.PETS) != 0);
    }

    /**
     * Remove entities in an area.
     * 
     * @param type 
     * @param origin
     * @param radius
     * @return
     */
    public abstract int removeEntities(EntityType type, Vector origin, int radius);

    /**
     * Returns whether a block has a valid ID.
     * 
     * @param type
     * @return
     */
    public boolean isValidBlockType(int type) {
        return BlockType.fromID(type) != null;
    }

    /**
     * Checks if the chunk pt is in is loaded. if not, loads the chunk
     *
     * @param pt Position to check
     */
    public void checkLoadedChunk(Vector pt) {
    }

    /**
     * Compare if the other world is equal.
     * 
     * @param other
     * @return
     */
    @Override
    public abstract boolean equals(Object other);

    /**
     * Hash code.
     * 
     * @return
     */
    @Override
    public abstract int hashCode();

    /**
     * Get the world's height
     * 
     * @return
     */
    public int getMaxY() {
        return 127;
    }

    /**
     * Does some post-processing. Should be called after using fast mode
     * 
     * @param chunks the chunks to fix
     */
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
    }

    public void fixLighting(Iterable<BlockVector2D> chunks) {
    }

    /**
     * Plays the minecraft effect with the given type and data at the given position.
     *
     * @param position
     * @param type
     * @param data
     */
    public boolean playEffect(Vector position, int type, int data) {
        return false;
    }

    private class QueuedEffect implements Comparable<QueuedEffect> {
        private final Vector position;
        private final int blockId;
        private final double priority;
        public QueuedEffect(Vector position, int blockId, double priority) {
            this.position = position;
            this.blockId = blockId;
            this.priority = priority;
        }

        public void play() {
            playEffect(position, 2001, blockId);
        }

        @Override
        public int compareTo(QueuedEffect other) {
            return Double.compare(priority, other.priority);
        }
    }

    private final PriorityQueue<QueuedEffect> effectQueue = new PriorityQueue<QueuedEffect>();
    private int taskId = -1;
    public boolean queueBlockBreakEffect(ServerInterface server, Vector position, int blockId, double priority) {
        if (taskId == -1) {
            taskId = server.schedule(0, 1, new Runnable() { 
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
}
