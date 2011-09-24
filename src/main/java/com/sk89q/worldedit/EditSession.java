// $Id$
/*
 * WorldEditLibrary
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.bags.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.*;

/**
 * This class can wrap all block editing operations into one "edit session" that
 * stores the state of the blocks before modification. This allows for easy undo
 * or redo. In addition to that, this class can use a "queue mode" that will
 * know how to handle some special types of items such as signs and torches. For
 * example, torches must be placed only after there is already a block below it,
 * otherwise the torch will be placed as an item.
 *
 * @author sk89q
 */
public class EditSession {

    /**
     * Random number generator.
     */
    private static Random prng = new Random();
    /**
     * World.
     */
    protected LocalWorld world;
    /**
     * Stores the original blocks before modification.
     */
    private DoubleArrayList<BlockVector, BaseBlock> original =
            new DoubleArrayList<BlockVector, BaseBlock>(
            true);
    /**
     * Stores the current blocks.
     */
    private DoubleArrayList<BlockVector, BaseBlock> current =
            new DoubleArrayList<BlockVector, BaseBlock>(
            false);
    /**
     * Blocks that should be placed before last.
     */
    private DoubleArrayList<BlockVector, BaseBlock> queueAfter =
            new DoubleArrayList<BlockVector, BaseBlock>(
            false);
    /**
     * Blocks that should be placed last.
     */
    private DoubleArrayList<BlockVector, BaseBlock> queueLast =
            new DoubleArrayList<BlockVector, BaseBlock>(
            false);

    /**
     * The maximum number of blocks to change at a time. If this number is
     * exceeded, a MaxChangedBlocksException exception will be raised. -1
     * indicates no limit.
     */
    private int maxBlocks = -1;

    /**
     * Indicates whether some types of blocks should be queued for best
     * reproduction.
     */
    private boolean queued = false;

    /**
     * Use the fast mode, which may leave chunks not flagged "dirty".
     */
    private boolean fastMode = false;

    /**
     * Block bag to use for getting blocks.
     */
    private BlockBag blockBag;

    /**
     * List of missing blocks;
     */
    private Set<Integer> missingBlocks = new HashSet<Integer>();

    /**
     * Mask to cover operations.
     */
    private Mask mask;

    /**
     * Construct the object with a maximum number of blocks.
     *
     * @param world
     * @param maxBlocks
     */
    public EditSession(LocalWorld world, int maxBlocks) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }

        this.maxBlocks = maxBlocks;
        this.world = world;
    }

    /**
     * Construct the object with a maximum number of blocks and a block bag.
     *
     * @param world
     * @param maxBlocks
     * @param blockBag
     * @blockBag
     */
    public EditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }

        this.maxBlocks = maxBlocks;
        this.blockBag = blockBag;
        this.world = world;
    }

    /**
     * Sets a block without changing history.
     *
     * @param pt
     * @param block
     * @return Whether the block changed
     */
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        final int y = pt.getBlockY();
        final int type = block.getType();
        if (y < 0 || y > 127) {
            return false;
        }

        world.checkLoadedChuck(pt);

        // No invalid blocks
        if (!world.isValidBlockType(type)) {
            return false;
        }

        if (mask != null) {
            if (!mask.matches(this, pt)) {
                return false;
            }
        }

        final int existing = world.getBlockType(pt);

        // Clear the container block so that it doesn't drop items
        if (BlockType.isContainerBlock(existing) && blockBag == null) {
            world.clearContainerBlockContents(pt);
            // Ice turns until water so this has to be done first
        } else if (existing == BlockID.ICE) {
            world.setBlockType(pt, BlockID.AIR);
        }

        if (blockBag != null) {
            if (type > 0) {
                try {
                    blockBag.fetchPlacedBlock(type);
                } catch (UnplaceableBlockException e) {
                    return false;
                } catch (BlockBagException e) {
                    missingBlocks.add(type);
                    return false;
                }
            }

            if (existing > 0) {
                try {
                    blockBag.storeDroppedBlock(existing);
                } catch (BlockBagException e) {
                }
            }
        }

        final boolean result;

        if (BlockType.usesData(type)) {
            if (fastMode) {
                result = world.setTypeIdAndDataFast(pt, type, block.getData() > -1 ? block.getData() : 0);
            } else {
                result = world.setTypeIdAndData(pt, type, block.getData() > -1 ? block.getData() : 0);
            }
        } else {
            if (fastMode) {
                result = world.setBlockTypeFast(pt, type);
            } else {
                result = world.setBlockType(pt, type);
            }
        }
        //System.out.println(pt + "" +result);

        if (type != 0) {
            if (block instanceof ContainerBlock) {
                if (blockBag == null) {
                    world.copyToWorld(pt, block);
                }
            }
            else if (block instanceof TileEntityBlock) {
                world.copyToWorld(pt, block);
            }
        }
        return result;
    }

    /**
     * Sets the block at position x, y, z with a block type. If queue mode is
     * enabled, blocks may not be actually set in world until flushQueue() is
     * called.
     *
     * @param pt
     * @param block
     * @return Whether the block changed -- not entirely dependable
     * @throws MaxChangedBlocksException
     */
    public boolean setBlock(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        BlockVector blockPt = pt.toBlockVector();

        // if (!original.containsKey(blockPt)) {
        original.put(blockPt, getBlock(pt));

        if (maxBlocks != -1 && original.size() > maxBlocks) {
            throw new MaxChangedBlocksException(maxBlocks);
        }
        // }

        current.put(pt.toBlockVector(), block);

        return smartSetBlock(pt, block);
    }

    /**
     * Insert a contrived block change into the history.
     *
     * @param pt
     * @param existing
     * @param block
     */
    public void rememberChange(Vector pt, BaseBlock existing, BaseBlock block) {
        BlockVector blockPt = pt.toBlockVector();

        original.put(blockPt, existing);
        current.put(pt.toBlockVector(), block);
    }

    /**
     * Set a block with a pattern.
     *
     * @param pt
     * @param pat
     * @return Whether the block changed -- not entirely dependable
     * @throws MaxChangedBlocksException
     */
    public boolean setBlock(Vector pt, Pattern pat)
            throws MaxChangedBlocksException {
        return setBlock(pt, pat.next(pt));
    }

    /**
     * Set a block only if there's no block already there.
     *
     * @param pt
     * @param block
     * @return if block was changed
     * @throws MaxChangedBlocksException
     */
    public boolean setBlockIfAir(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        if (!getBlock(pt).isAir()) {
            return false;
        } else {
            return setBlock(pt, block);
        }
    }

    /**
     * Actually set the block. Will use queue.
     *
     * @param pt
     * @param block
     * @return
     */
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        if (queued) {
            // Place torches, etc. last
            if (BlockType.shouldPlaceLast(block.getType())) {
                queueLast.put(pt.toBlockVector(), block);
                return !(getBlockType(pt) == block.getType()
                        && getBlockData(pt) == block.getData());
                // Destroy torches, etc. first
            } else if (BlockType.shouldPlaceLast(getBlockType(pt))) {
                rawSetBlock(pt, new BaseBlock(BlockID.AIR));
            } else {
                queueAfter.put(pt.toBlockVector(), block);
                return !(getBlockType(pt) == block.getType()
                        && getBlockData(pt) == block.getData());
            }
        }

        return rawSetBlock(pt, block);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param pt
     * @return Block type
     */
    public BaseBlock getBlock(Vector pt) {
        // In the case of the queue, the block may have not actually been
        // changed yet
        if (queued) {
            /*
             * BlockVector blockPt = pt.toBlockVector();
             *
             * if (current.containsKey(blockPt)) { return current.get(blockPt);
             * }
             */
        }

        return rawGetBlock(pt);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param pt
     * @return Block type
     */
    public int getBlockType(Vector pt) {
        // In the case of the queue, the block may have not actually been
        // changed yet
        if (queued) {
            /*
             * BlockVector blockPt = pt.toBlockVector();
             *
             * if (current.containsKey(blockPt)) { return current.get(blockPt);
             * }
             */
        }

        return world.getBlockType(pt);
    }

    public int getBlockData(Vector pt) {
        // In the case of the queue, the block may have not actually been
        // changed yet
        if (queued) {
            /*
             * BlockVector blockPt = pt.toBlockVector();
             *
             * if (current.containsKey(blockPt)) { return current.get(blockPt);
             * }
             */
        }

        return world.getBlockData(pt);
    }
    /**
     * Gets the block type at a position x, y, z.
     *
     * @param pt
     * @return BaseBlock
     */
    public BaseBlock rawGetBlock(Vector pt) {
        world.checkLoadedChuck(pt);

        int type = world.getBlockType(pt);
        int data = world.getBlockData(pt);

        switch (type) {
        case BlockID.WALL_SIGN:
        case BlockID.SIGN_POST: {
            SignBlock block = new SignBlock(type, data);
            world.copyFromWorld(pt, block);
            return block;
        }

        case BlockID.CHEST: {
            ChestBlock block = new ChestBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        }

        case BlockID.FURNACE:
        case BlockID.BURNING_FURNACE: {
            FurnaceBlock block = new FurnaceBlock(type, data);
            world.copyFromWorld(pt, block);
            return block;
        }

        case BlockID.DISPENSER: {
            DispenserBlock block = new DispenserBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        }

        case BlockID.MOB_SPAWNER: {
            MobSpawnerBlock block = new MobSpawnerBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        }

        case BlockID.NOTE_BLOCK: {
            NoteBlock block = new NoteBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        }

        default:
            return new BaseBlock(type, data);
        }
    }

    /**
     * Restores all blocks to their initial state.
     *
     * @param sess
     */
    public void undo(EditSession sess) {
        for (Map.Entry<BlockVector, BaseBlock> entry : original) {
            BlockVector pt = (BlockVector) entry.getKey();
            sess.smartSetBlock(pt, (BaseBlock) entry.getValue());
        }
        sess.flushQueue();
    }

    /**
     * Sets to new state.
     *
     * @param sess
     */
    public void redo(EditSession sess) {
        for (Map.Entry<BlockVector, BaseBlock> entry : current) {
            BlockVector pt = (BlockVector) entry.getKey();
            sess.smartSetBlock(pt, (BaseBlock) entry.getValue());
        }
        sess.flushQueue();
    }

    /**
     * Get the number of changed blocks.
     *
     * @return
     */
    public int size() {
        return original.size();
    }

    /**
     * Get the maximum number of blocks that can be changed. -1 will be returned
     * if disabled.
     *
     * @return block change limit
     */
    public int getBlockChangeLimit() {
        return maxBlocks;
    }

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param maxBlocks -1 to disable
     */
    public void setBlockChangeLimit(int maxBlocks) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }
        this.maxBlocks = maxBlocks;
    }

    /**
     * Returns queue status.
     *
     * @return whether the queue is enabled
     */
    public boolean isQueueEnabled() {
        return queued;
    }

    /**
     * Queue certain types of block for better reproduction of those blocks.
     */
    public void enableQueue() {
        queued = true;
    }

    /**
     * Disable the queue. This will flush the queue.
     */
    public void disableQueue() {
        if (queued != false) {
            flushQueue();
        }
        queued = false;
    }

    /**
     * Set fast mode.
     *
     * @param fastMode
     */
    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }

    /**
     * Return fast mode status.
     *
     * @return
     */
    public boolean hasFastMode() {
        return fastMode;
    }

    /**
     * Finish off the queue.
     */
    public void flushQueue() {
        if (!queued) {
            return;
        }

        for (Map.Entry<BlockVector, BaseBlock> entry : queueAfter) {
            BlockVector pt = (BlockVector) entry.getKey();
            rawSetBlock(pt, (BaseBlock) entry.getValue());
        }

        // We don't want to place these blocks if other blocks were missing
        // because it might cause the items to drop
        if (blockBag == null || missingBlocks.size() == 0) {
            for (Map.Entry<BlockVector, BaseBlock> entry : queueLast) {
                BlockVector pt = (BlockVector) entry.getKey();
                rawSetBlock(pt, (BaseBlock) entry.getValue());
            }
        }

        queueAfter.clear();
        queueLast.clear();
    }

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param origin
     * @param block
     * @param radius
     * @param depth
     * @param recursive
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int fillXZ(Vector origin, BaseBlock block, double radius, int depth,
            boolean recursive) throws MaxChangedBlocksException {

        int affected = 0;
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        queue.push(new BlockVector(originX, originY, originZ));

        while (!queue.empty()) {
            BlockVector pt = queue.pop();
            int cx = pt.getBlockX();
            int cy = pt.getBlockY();
            int cz = pt.getBlockZ();

            if (cy < 0 || cy > originY || visited.contains(pt)) {
                continue;
            }

            visited.add(pt);

            if (recursive) {
                if (origin.distance(pt) > radius) {
                    continue;
                }

                if (getBlock(pt).isAir()) {
                    if (setBlock(pt, block)) {
                        ++affected;
                    }
                } else {
                    continue;
                }

                queue.push(new BlockVector(cx, cy - 1, cz));
                queue.push(new BlockVector(cx, cy + 1, cz));
            } else {
                double dist = Math.sqrt(Math.pow(originX - cx, 2)
                        + Math.pow(originZ - cz, 2));
                int minY = originY - depth + 1;

                if (dist > radius) {
                    continue;
                }

                if (getBlock(pt).isAir()) {
                    affected += fillY(cx, originY, cz, block, minY);
                } else {
                    continue;
                }
            }

            queue.push(new BlockVector(cx + 1, cy, cz));
            queue.push(new BlockVector(cx - 1, cy, cz));
            queue.push(new BlockVector(cx, cy, cz + 1));
            queue.push(new BlockVector(cx, cy, cz - 1));
        }

        return affected;
    }

    /**
     * Recursively fills a block and below until it hits another block.
     *
     * @param x
     * @param cy
     * @param z
     * @param block
     * @param minY
     * @throws MaxChangedBlocksException
     * @return
     */
    private int fillY(int x, int cy, int z, BaseBlock block, int minY)
            throws MaxChangedBlocksException {
        int affected = 0;

        for (int y = cy; y >= minY; --y) {
            Vector pt = new Vector(x, y, z);

            if (getBlock(pt).isAir()) {
                setBlock(pt, block);
                ++affected;
            } else {
                break;
            }
        }

        return affected;
    }

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param origin
     * @param pattern
     * @param radius
     * @param depth
     * @param recursive
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int fillXZ(Vector origin, Pattern pattern, double radius, int depth,
            boolean recursive) throws MaxChangedBlocksException {

        int affected = 0;
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        queue.push(new BlockVector(originX, originY, originZ));

        while (!queue.empty()) {
            BlockVector pt = queue.pop();
            int cx = pt.getBlockX();
            int cy = pt.getBlockY();
            int cz = pt.getBlockZ();

            if (cy < 0 || cy > originY || visited.contains(pt)) {
                continue;
            }

            visited.add(pt);

            if (recursive) {
                if (origin.distance(pt) > radius) {
                    continue;
                }

                if (getBlock(pt).isAir()) {
                    if (setBlock(pt, pattern.next(pt))) {
                        ++affected;
                    }
                } else {
                    continue;
                }

                queue.push(new BlockVector(cx, cy - 1, cz));
                queue.push(new BlockVector(cx, cy + 1, cz));
            } else {
                double dist = Math.sqrt(Math.pow(originX - cx, 2)
                        + Math.pow(originZ - cz, 2));
                int minY = originY - depth + 1;

                if (dist > radius) {
                    continue;
                }

                if (getBlock(pt).isAir()) {
                    affected += fillY(cx, originY, cz, pattern, minY);
                } else {
                    continue;
                }
            }

            queue.push(new BlockVector(cx + 1, cy, cz));
            queue.push(new BlockVector(cx - 1, cy, cz));
            queue.push(new BlockVector(cx, cy, cz + 1));
            queue.push(new BlockVector(cx, cy, cz - 1));
        }

        return affected;
    }

    /**
     * Recursively fills a block and below until it hits another block.
     *
     * @param x
     * @param cy
     * @param z
     * @param pattern
     * @param minY
     * @throws MaxChangedBlocksException
     * @return
     */
    private int fillY(int x, int cy, int z, Pattern pattern, int minY)
            throws MaxChangedBlocksException {
        int affected = 0;

        for (int y = cy; y >= minY; --y) {
            Vector pt = new Vector(x, y, z);

            if (getBlock(pt).isAir()) {
                setBlock(pt, pattern.next(pt));
                ++affected;
            } else {
                break;
            }
        }

        return affected;
    }

    /**
     * Remove blocks above.
     *
     * @param pos
     * @param size
     * @param height
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int removeAbove(Vector pos, int size, int height)
            throws MaxChangedBlocksException {
        int maxY = Math.min(127, pos.getBlockY() + height - 1);
        --size;
        int affected = 0;

        int oX = pos.getBlockX();
        int oY = pos.getBlockY();
        int oZ = pos.getBlockZ();

        for (int x = oX - size; x <= oX + size; ++x) {
            for (int z = oZ - size; z <= oZ + size; ++z) {
                for (int y = oY; y <= maxY; ++y) {
                    Vector pt = new Vector(x, y, z);

                    if (getBlockType(pt) != BlockID.AIR) {
                        setBlock(pt, new BaseBlock(BlockID.AIR));
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Remove blocks below.
     *
     * @param pos
     * @param size
     * @param height
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int removeBelow(Vector pos, int size, int height)
            throws MaxChangedBlocksException {
        int minY = Math.max(0, pos.getBlockY() - height);
        --size;
        int affected = 0;

        int oX = pos.getBlockX();
        int oY = pos.getBlockY();
        int oZ = pos.getBlockZ();

        for (int x = oX - size; x <= oX + size; ++x) {
            for (int z = oZ - size; z <= oZ + size; ++z) {
                for (int y = oY; y >= minY; --y) {
                    Vector pt = new Vector(x, y, z);

                    if (getBlockType(pt) != BlockID.AIR) {
                        setBlock(pt, new BaseBlock(BlockID.AIR));
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Remove nearby blocks of a type.
     *
     * @param pos
     * @param blockType
     * @param size
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int removeNear(Vector pos, int blockType, int size)
            throws MaxChangedBlocksException {
        int affected = 0;
        BaseBlock air = new BaseBlock(BlockID.AIR);

        int minX = pos.getBlockX() - size;
        int maxX = pos.getBlockX() + size;
        int minY = Math.max(0, pos.getBlockY() - size);
        int maxY = Math.min(127, pos.getBlockY() + size);
        int minZ = pos.getBlockZ() - size;
        int maxZ = pos.getBlockZ() + size;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Vector p = new Vector(x, y, z);

                    if (getBlockType(p) == blockType) {
                        if (setBlock(p, air)) {
                            ++affected;
                        }
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Sets all the blocks inside a region to a certain block type.
     *
     * @param region
     * @param block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int setBlocks(Region region, BaseBlock block)
            throws MaxChangedBlocksException {
        int affected = 0;

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        if (setBlock(pt, block)) {
                            ++affected;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (setBlock(pt, block)) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    /**
     * Sets all the blocks inside a region to a certain block type.
     *
     * @param region
     * @param pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int setBlocks(Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        int affected = 0;

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        if (setBlock(pt, pattern.next(pt))) {
                            ++affected;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (setBlock(pt, pattern.next(pt))) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    /**
     * Replaces all the blocks of a type inside a region to another block type.
     *
     * @param region
     * @param fromBlockTypes -1 for non-air
     * @param toBlock
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int replaceBlocks(Region region, Set<BaseBlock> fromBlockTypes,
            BaseBlock toBlock) throws MaxChangedBlocksException {
        int affected = 0;

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);
                        BaseBlock curBlockType = getBlock(pt);

                        if ((fromBlockTypes == null && !curBlockType.isAir())
                                || (fromBlockTypes != null && curBlockType.inIterable(fromBlockTypes))) { // Probably faster if someone adds a proper hashCode to BaseBlock
                            if (setBlock(pt, toBlock)) {
                                ++affected;
                            }
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BaseBlock curBlockType = getBlock(pt);

                if (fromBlockTypes == null && !curBlockType.isAir()
                        || fromBlockTypes != null && curBlockType.inIterable(fromBlockTypes)) {
                    if (setBlock(pt, toBlock)) {
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Replaces all the blocks of a type inside a region to another block type.
     *
     * @param region
     * @param fromBlockTypes -1 for non-air
     * @param pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int replaceBlocks(Region region, Set<BaseBlock> fromBlockTypes,
            Pattern pattern) throws MaxChangedBlocksException {
        int affected = 0;

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);
                        BaseBlock curBlockType = getBlock(pt);

                        if ((fromBlockTypes == null && !curBlockType.isAir())
                                || (fromBlockTypes != null && curBlockType.inIterable(fromBlockTypes))) { // Probably faster if someone adds a proper hashCode to BaseBlock
                            if (setBlock(pt, pattern.next(pt))) {
                                ++affected;
                            }
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BaseBlock curBlockType = getBlock(pt);

                if (fromBlockTypes == null && !curBlockType.isAir()
                        || curBlockType.inIterable(fromBlockTypes)) {
                    if (setBlock(pt, pattern.next(pt))) {
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Make faces of the region (as if it was a cuboid if it's not).
     *
     * @param region
     * @param block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int makeCuboidFaces(Region region, BaseBlock block)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                if (setBlock(new Vector(x, y, minZ), block)) {
                    ++affected;
                }
                if (setBlock(new Vector(x, y, maxZ), block)) {
                    ++affected;
                }
                ++affected;
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                if (setBlock(new Vector(minX, y, z), block)) {
                    ++affected;
                }
                if (setBlock(new Vector(maxX, y, z), block)) {
                    ++affected;
                }
            }
        }

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                if (setBlock(new Vector(x, minY, z), block)) {
                    ++affected;
                }
                if (setBlock(new Vector(x, maxY, z), block)) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    /**
     * Make faces of the region (as if it was a cuboid if it's not).
     *
     * @param region
     * @param pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int makeCuboidFaces(Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                Vector minV = new Vector(x, y, minZ);
                if (setBlock(min, pattern.next(minV))) {
                    ++affected;
                }
                Vector maxV = new Vector(x, y, maxZ);
                if (setBlock(maxV, pattern.next(maxV))) {
                    ++affected;
                }
                ++affected;
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                Vector minV = new Vector(minX, y, z);
                if (setBlock(minV, pattern.next(minV))) {
                    ++affected;
                }
                Vector maxV = new Vector(maxX, y, z);
                if (setBlock(maxV, pattern.next(maxV))) {
                    ++affected;
                }
            }
        }

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                Vector minV = new Vector(x, minY, z);
                if (setBlock(minV, pattern.next(minV))) {
                    ++affected;
                }
                Vector maxV = new Vector(x, maxY, z);
                if (setBlock(maxV, pattern.next(maxV))) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    /**
     * Make walls of the region (as if it was a cuboid if it's not).
     *
     * @param region
     * @param block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int makeCuboidWalls(Region region, BaseBlock block)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                if (setBlock(new Vector(x, y, minZ), block)) {
                    ++affected;
                }
                if (setBlock(new Vector(x, y, maxZ), block)) {
                    ++affected;
                }
                ++affected;
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                if (setBlock(new Vector(minX, y, z), block)) {
                    ++affected;
                }
                if (setBlock(new Vector(maxX, y, z), block)) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    /**
     * Make walls of the region (as if it was a cuboid if it's not).
     *
     * @param region
     * @param block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int makeCuboidWalls(Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                Vector minV = new Vector(x, y, minZ);
                if (setBlock(minV, pattern.next(minV))) {
                    ++affected;
                }
                Vector maxV = new Vector(x, y, maxZ);
                if (setBlock(maxV, pattern.next(maxV))) {
                    ++affected;
                }
                ++affected;
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                Vector minV = new Vector(minX, y, z);
                if (setBlock(minV, pattern.next(minV))) {
                    ++affected;
                }
                Vector maxV = new Vector(maxX, y, z);
                if (setBlock(maxV, pattern.next(maxV))) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    /**
     * Overlays a layer of blocks over a cuboid area.
     *
     * @param region
     * @param block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int overlayCuboidBlocks(Region region, BaseBlock block)
            throws MaxChangedBlocksException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int upperY = Math.min(127, max.getBlockY() + 1);
        int lowerY = Math.max(0, min.getBlockY() - 1);

        int affected = 0;

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = upperY; y >= lowerY; --y) {
                    Vector above = new Vector(x, y + 1, z);

                    if (y + 1 <= 127 && !getBlock(new Vector(x, y, z)).isAir()
                            && getBlock(above).isAir()) {
                        if (setBlock(above, block)) {
                            ++affected;
                        }
                        break;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Overlays a layer of blocks over a cuboid area.
     *
     * @param region
     * @param pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int overlayCuboidBlocks(Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int upperY = Math.min(127, max.getBlockY() + 1);
        int lowerY = Math.max(0, min.getBlockY() - 1);

        int affected = 0;

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = upperY; y >= lowerY; --y) {
                    Vector above = new Vector(x, y + 1, z);

                    if (y + 1 <= 127 && !getBlock(new Vector(x, y, z)).isAir()
                            && getBlock(above).isAir()) {
                        if (setBlock(above, pattern.next(above))) {
                            ++affected;
                        }
                        break;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Turns the first 3 layers into dirt/grass and the bottom layers
     * into rock, like a natural Minecraft mountain.
     *
     * @param region
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int naturalizeCuboidBlocks(Region region)
            throws MaxChangedBlocksException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int upperY = Math.min(127, max.getBlockY() + 1);
        int lowerY = Math.max(0, min.getBlockY() - 1);

        int affected = 0;

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();

        BaseBlock grass = new BaseBlock(BlockID.GRASS);
        BaseBlock dirt = new BaseBlock(BlockID.DIRT);
        BaseBlock stone = new BaseBlock(BlockID.STONE);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                int level = -1;

                for (int y = upperY; y >= lowerY; --y) {
                    Vector pt = new Vector(x, y, z);
                    //Vector above = new Vector(x, y + 1, z);
                    int blockType = getBlockType(pt);

                    boolean isTransformable =
                            blockType == BlockID.GRASS
                            || blockType == BlockID.DIRT
                            || blockType == BlockID.STONE;

                    // Still searching for the top block
                    if (level == -1) {
                        if (!isTransformable) {
                            continue; // Not transforming this column yet
                        }

                        level = 0;
                    }

                    if (level >= 0) {
                        if (isTransformable) {
                            if (level == 0) {
                                setBlock(pt, grass);
                                affected++;
                            } else if (level <= 2) {
                                setBlock(pt, dirt);
                                affected++;
                            } else {
                                setBlock(pt, stone);
                                affected++;
                            }
                        }

                        level++;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Stack a cuboid region.
     *
     * @param region
     * @param dir
     * @param count
     * @param copyAir
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int stackCuboidRegion(Region region, Vector dir, int count,
            boolean copyAir) throws MaxChangedBlocksException {
        int affected = 0;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        int xs = region.getWidth();
        int ys = region.getHeight();
        int zs = region.getLength();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = minY; y <= maxY; ++y) {
                    BaseBlock block = getBlock(new Vector(x, y, z));

                    if (!block.isAir() || copyAir) {
                        for (int i = 1; i <= count; ++i) {
                            Vector pos = new Vector(x + xs * dir.getBlockX()
                                    * i, y + ys * dir.getBlockY() * i, z + zs
                                    * dir.getBlockZ() * i);

                            if (setBlock(pos, block)) {
                                ++affected;
                            }
                        }
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Move a cuboid region.
     *
     * @param region
     * @param dir
     * @param distance
     * @param copyAir
     * @param replace
     * @return number of blocks moved
     * @throws MaxChangedBlocksException
     */
    public int moveCuboidRegion(Region region, Vector dir, int distance,
            boolean copyAir, BaseBlock replace)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector shift = dir.multiply(distance);
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        Vector newMin = min.add(shift);
        Vector newMax = min.add(shift);

        Map<Vector, BaseBlock> delayed = new LinkedHashMap<Vector, BaseBlock>();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = minY; y <= maxY; ++y) {
                    Vector pos = new Vector(x, y, z);
                    BaseBlock block = getBlock(pos);

                    if (!block.isAir() || copyAir) {
                        Vector newPos = pos.add(shift);

                        delayed.put(newPos, getBlock(pos));

                        // Don't want to replace the old block if it's in
                        // the new area
                        if (x >= newMin.getBlockX() && x <= newMax.getBlockX()
                                && y >= newMin.getBlockY()
                                && y <= newMax.getBlockY()
                                && z >= newMin.getBlockZ()
                                && z <= newMax.getBlockZ()) {
                        } else {
                            setBlock(pos, replace);
                        }
                    }
                }
            }
        }

        for (Map.Entry<Vector, BaseBlock> entry : delayed.entrySet()) {
            setBlock(entry.getKey(), entry.getValue());
            ++affected;
        }

        return affected;
    }

    /**
     * Drain nearby pools of water or lava.
     *
     * @param pos
     * @param radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int drainArea(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        for (int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; ++x) {
            for (int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; ++z) {
                for (int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; ++y) {
                    queue.push(new BlockVector(x, y, z));
                }
            }
        }

        while (!queue.empty()) {
            BlockVector cur = queue.pop();

            int type = getBlockType(cur);

            // Check block type
            if (type != BlockID.WATER && type != BlockID.STATIONARY_WATER
                    && type != BlockID.LAVA && type != BlockID.STATIONARY_LAVA) {
                continue;
            }

            // Don't want to revisit
            if (visited.contains(cur)) {
                continue;
            }

            visited.add(cur);

            // Check radius
            if (pos.distance(cur) > radius) {
                continue;
            }

            for (int x = cur.getBlockX() - 1; x <= cur.getBlockX() + 1; ++x) {
                for (int z = cur.getBlockZ() - 1; z <= cur.getBlockZ() + 1; ++z) {
                    for (int y = cur.getBlockY() - 1; y <= cur.getBlockY() + 1; ++y) {
                        BlockVector newPos = new BlockVector(x, y, z);

                        if (!cur.equals(newPos)) {
                            queue.push(newPos);
                        }
                    }
                }
            }

            if (setBlock(cur, new BaseBlock(BlockID.AIR))) {
                ++affected;
            }
        }

        return affected;
    }

    /**
     * Level water.
     *
     * @param pos
     * @param radius
     * @param moving
     * @param stationary
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int fixLiquid(Vector pos, double radius, int moving, int stationary)
            throws MaxChangedBlocksException {
        int affected = 0;

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        for (int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; ++x) {
            for (int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; ++z) {
                for (int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; ++y) {
                    int type = getBlock(new Vector(x, y, z)).getType();

                    // Check block type
                    if (type == moving || type == stationary) {
                        queue.push(new BlockVector(x, y, z));
                    }
                }
            }
        }

        BaseBlock stationaryBlock = new BaseBlock(stationary);

        while (!queue.empty()) {
            BlockVector cur = queue.pop();

            int type = getBlockType(cur);

            // Check block type
            if (type != moving && type != stationary && type != BlockID.AIR) {
                continue;
            }

            // Don't want to revisit
            if (visited.contains(cur)) {
                continue;
            }

            visited.add(cur);

            if (setBlock(cur, stationaryBlock)){
                ++affected;
            }

            // Check radius
            if (pos.distance(cur) > radius) {
                continue;
            }

            queue.push(cur.add(1, 0, 0).toBlockVector());
            queue.push(cur.add(-1, 0, 0).toBlockVector());
            queue.push(cur.add(0, 0, 1).toBlockVector());
            queue.push(cur.add(0, 0, -1).toBlockVector());
        }

        return affected;
    }

    /**
     * Helper method to draw the cylinder.
     *
     * @param center
     * @param x
     * @param z
     * @param height
     * @param block
     * @throws MaxChangedBlocksException
     */
    private int makeHCylinderPoints(Vector center, int x, double z, int height,
            Pattern block) throws MaxChangedBlocksException {
        int ceilZ = (int) Math.ceil(z);
        int affected = 0;

        if (x == 0) {
            for (int y = 0; y < height; ++y) {
                setBlock(center.add(0, y, ceilZ), block);
                setBlock(center.add(0, y, -ceilZ), block);
                setBlock(center.add(ceilZ, y, 0), block);
                setBlock(center.add(-ceilZ, y, 0), block);
                affected += 4;
            }
        } else if (x == z) {
            for (int y = 0; y < height; ++y) {
                setBlock(center.add(x, y, ceilZ), block);
                setBlock(center.add(-x, y, ceilZ), block);
                setBlock(center.add(x, y, -ceilZ), block);
                setBlock(center.add(-x, y, -ceilZ), block);
                affected += 4;
            }
        } else if (x < z) {
            for (int y = 0; y < height; ++y) {
                setBlock(center.add(x, y, ceilZ), block);
                setBlock(center.add(-x, y, ceilZ), block);
                setBlock(center.add(x, y, -ceilZ), block);
                setBlock(center.add(-x, y, -ceilZ), block);
                setBlock(center.add(ceilZ, y, x), block);
                setBlock(center.add(-ceilZ, y, x), block);
                setBlock(center.add(ceilZ, y, -x), block);
                setBlock(center.add(-ceilZ, y, -x), block);
                affected += 8;
            }
        }

        return affected;
    }

    /**
     * Draw a hollow cylinder.
     *
     * @param pos
     * @param block
     * @param radius
     * @param height
     * @return number of blocks set
     * @throws MaxChangedBlocksException
     */
    public int makeHollowCylinder(Vector pos, Pattern block, double radius,
            int height) throws MaxChangedBlocksException {
        int x = 0;
        double z = radius;
        double d = (5 - radius * 4) / 4;
        int affected = 0;

        if (height == 0) {
            return 0;
        } else if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
        }

        // Only do this check if height is negative --Elizabeth
        if (height < 0 && pos.getBlockY() - height - 1 < 0) {
            height = pos.getBlockY() + 1;
        } else if (pos.getBlockY() + height - 1 > 127) {
            height = 127 - pos.getBlockY() + 1;
        }

        affected += makeHCylinderPoints(pos, x, z, height, block);

        while (x < z) {
            ++x;

            if (d >= 0) {
                d += 2 * (x - --z) + 1;
            } else {
                d += 2 * x + 1;
            }

            affected += makeHCylinderPoints(pos, x, z, height, block);
        }

        return affected;
    }

    /**
     * Helper method to draw the cylinder.
     *
     * @param center
     * @param x
     * @param z
     * @param height
     * @param block
     * @throws MaxChangedBlocksException
     */
    private int makeCylinderPoints(Vector center, int x, double z, int height,
            Pattern block) throws MaxChangedBlocksException {
        int ceilZ = (int) Math.ceil(z);
    	int affected = 0;

        if (x == z) {
            for (int y = 0; y < height; ++y) {
                for (int z2 = -ceilZ; z2 <= ceilZ; ++z2) {
                    setBlock(center.add(x, y, z2), block);
                    setBlock(center.add(-x, y, z2), block);
                    affected += 2;
                }
            }
        } else if (x < z) {
            for (int y = 0; y < height; ++y) {
                for (int x2 = -x; x2 <= x; ++x2) {
                    for (int z2 = -ceilZ; z2 <= ceilZ; ++z2) {
                        setBlock(center.add(x2, y, z2), block);
                        ++affected;
                    }
                    setBlock(center.add(ceilZ, y, x2), block);
                    setBlock(center.add(-ceilZ, y, x2), block);
                    affected += 2;
                }
            }
        }

        return affected;
    }

    /**
     * Draw a filled cylinder.
     *
     * @param pos
     * @param block
     * @param radius
     * @param height
     * @return number of blocks set
     * @throws MaxChangedBlocksException
     */
    public int makeCylinder(Vector pos, Pattern block, double radius, int height)
            throws MaxChangedBlocksException {
        int x = 0;
        double z = radius;
        double d = (5 - radius * 4) / 4;
        int affected = 0;

        if (height == 0) {
            return 0;
        } else if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
        }

        if (pos.getBlockY() - height - 1 < 0) {
            height = pos.getBlockY() + 1;
        } else if (pos.getBlockY() + height - 1 > 127) {
            height = 127 - pos.getBlockY() + 1;
        }

        affected += makeCylinderPoints(pos, x, z, height, block);

        while (x < z) {
            ++x;

            if (d >= 0) {
                d += 2 * (x - --z) + 1;
            } else {
                d += 2 * x + 1;
            }

            affected += makeCylinderPoints(pos, x, z, height, block);
        }

        return affected;
    }

    /**
     * Makes a sphere or ellipsoid.
     *
     * @param pos Center of the sphere or ellipsoid
     * @param block The block pattern to use
     * @param radiusX The sphere/ellipsoid's largest north/south extent
     * @param radiusY The sphere/ellipsoid's largest up/down extent
     * @param radiusZ The sphere/ellipsoid's largest east/west extent
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException
     */
    public int makeSphere(Vector pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radius += 0.5;
        final double radiusSq = radius*radius;
        final double radius1Sq = (radius - 1)*(radius - 1);

        final int ceilRadius = (int) Math.ceil(radius);
        for (int x = 0; x <= ceilRadius; ++x) {
            for (int y = 0; y <= ceilRadius; ++y) {
                for (int z = 0; z <= ceilRadius; ++z) {
                    double dSq = lengthSq(x, y, z);

                    if (dSq > radiusSq) {
                        continue;
                    }
                    if (!filled) {
                        if (dSq < radius1Sq
                                || (lengthSq(x + 1, y, z) <= radiusSq
                                && lengthSq(x, y + 1, z) <= radiusSq
                                && lengthSq(x, y, z + 1) <= radiusSq)) {
                            continue;
                        }
                    }

                    if (setBlock(pos.add(x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, -y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, -y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, -y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, -y, -z), block)) {
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Makes a sphere or ellipsoid.
     *
     * @param pos Center of the sphere or ellipsoid
     * @param block The block pattern to use
     * @param radiusX The sphere/ellipsoid's largest north/south extent
     * @param radiusY The sphere/ellipsoid's largest up/down extent
     * @param radiusZ The sphere/ellipsoid's largest east/west extent
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException
     */
    public int makeSphere(Vector pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radiusX += 0.5;
        radiusY += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (!filled) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    if (setBlock(pos.add(x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, -y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, -y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, -y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, -y, -z), block)) {
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    private static final double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    /**
     * Makes a pyramid.
     *
     * @param pos
     * @param block
     * @param size
     * @param filled
     * @return number of blocks changed
     * @throws MaxChangedBlocksException
     */
    public int makePyramid(Vector pos, Pattern block, int size,
            boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        int height = size;

        for (int y = 0; y <= height; ++y) {
            size--;
            for (int x = 0; x <= size; ++x) {
                for (int z = 0; z <= size; ++z) {

                    if ((filled && z <= size && x <= size) || z == size || x == size) {

                        if (setBlock(pos.add(x, y, z), block)) {
                            ++affected;
                        }
                        if (setBlock(pos.add(-x, y, z), block)) {
                            ++affected;
                        }
                        if (setBlock(pos.add(x, y, -z), block)) {
                            ++affected;
                        }
                        if (setBlock(pos.add(-x, y, -z), block)) {
                            ++affected;
                        }
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Thaw.
     *
     * @param pos
     * @param radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int thaw(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius*radius;

        int ox = pos.getBlockX();
        int oy = pos.getBlockY();
        int oz = pos.getBlockZ();

        BaseBlock air = new BaseBlock(0);
        BaseBlock water = new BaseBlock(BlockID.STATIONARY_WATER);

        int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(pos) > radiusSq) {
                    continue;
                }

                for (int y = 127; y >= 1; --y) {
                    Vector pt = new Vector(x, y, z);
                    int id = getBlockType(pt);

                    switch (id) {
                    case BlockID.ICE:
                        if (setBlock(pt, water)) {
                            ++affected;
                        }
                        break;

                    case BlockID.SNOW:
                        if (setBlock(pt, air)) {
                            ++affected;
                        }
                        break;

                    case BlockID.AIR:
                        continue;

                    default:
                        break;
                    }

                    break;
                }
            }
        }

        return affected;
    }

    /**
     * Make snow.
     *
     * @param pos
     * @param radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int simulateSnow(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius*radius;

        int ox = pos.getBlockX();
        int oy = pos.getBlockY();
        int oz = pos.getBlockZ();

        BaseBlock ice = new BaseBlock(BlockID.ICE);
        BaseBlock snow = new BaseBlock(BlockID.SNOW);

        int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(pos) > radiusSq) {
                    continue;
                }

                for (int y = 127; y >= 1; --y) {
                    Vector pt = new Vector(x, y, z);
                    int id = getBlockType(pt);

                    if (id == BlockID.AIR) {
                        continue;
                    }

                    // Ice!
                    if (id == BlockID.WATER || id == BlockID.STATIONARY_WATER) {
                        if (setBlock(pt, ice)) {
                            ++affected;
                        }
                        break;
                    }

                    // Snow should not cover these blocks
                    if (BlockType.canPassThrough(id)) {
                        break;
                    }

                    // Too high?
                    if (y == 127) {
                        break;
                    }

                    // add snow cover
                    if (setBlock(pt.add(0, 1, 0), snow)) {
                        ++affected;
                    }
                    break;
                }
            }
        }

        return affected;
    }

    /**
     * Green.
     *
     * @param pos
     * @param radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int green(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius*radius;

        int ox = pos.getBlockX();
        int oy = pos.getBlockY();
        int oz = pos.getBlockZ();

        BaseBlock grass = new BaseBlock(BlockID.GRASS);

        int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(pos) > radiusSq) {
                    continue;
                }

                for (int y = 127; y >= 1; --y) {
                    Vector pt = new Vector(x, y, z);
                    int id = getBlockType(pt);

                    if (BlockType.canPassThrough(id)) {
                        continue;
                    }

                    if (id == BlockID.DIRT) {
                        if (setBlock(pt, grass)) {
                            ++affected;
                        }
                    }
                    break;
                }
            }
        }

        return affected;
    }

    /**
     * Set a block by chance.
     *
     * @param pos
     * @param block
     * @param c 0-1 chance
     * @return whether a block was changed
     * @throws MaxChangedBlocksException
     */
    public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c)
            throws MaxChangedBlocksException {
        if (Math.random() <= c) {
            return setBlockIfAir(pos, block);
        }
        return false;
    }

    /**
     * Makes a pumpkin patch.
     *
     * @param basePos
     */
    private void makePumpkinPatch(Vector basePos)
            throws MaxChangedBlocksException {
        // BaseBlock logBlock = new BaseBlock(BlockID.LOG);
        BaseBlock leavesBlock = new BaseBlock(BlockID.LEAVES);

        // setBlock(basePos.subtract(0, 1, 0), logBlock);
        setBlockIfAir(basePos, leavesBlock);

        makePumpkinPatchVine(basePos, basePos.add(0, 0, 1));
        makePumpkinPatchVine(basePos, basePos.add(0, 0, -1));
        makePumpkinPatchVine(basePos, basePos.add(1, 0, 0));
        makePumpkinPatchVine(basePos, basePos.add(-1, 0, 0));
    }

    /**
     * Make a pumpkin patch fine.
     *
     * @param basePos
     * @param pos
     */
    private void makePumpkinPatchVine(Vector basePos, Vector pos)
            throws MaxChangedBlocksException {
        if (pos.distance(basePos) > 4) return;
        if (getBlockType(pos) != 0) return;

        for (int i = -1; i > -3; --i) {
            Vector testPos = pos.add(0, i, 0);
            if (getBlockType(testPos) == BlockID.AIR) {
                pos = testPos;
            } else {
                break;
            }
        }

        setBlockIfAir(pos, new BaseBlock(BlockID.LEAVES));

        int t = prng.nextInt(4);
        int h = prng.nextInt(3) - 1;

        BaseBlock log = new BaseBlock(BlockID.LOG);
        BaseBlock pumpkin = new BaseBlock(BlockID.PUMPKIN);

        switch (t) {
        case 0:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(1, 0, 0));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(1, h, -1), log);
            }
            setBlockIfAir(pos.add(0, 0, -1), pumpkin);
            break;

        case 1:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(0, 0, 1));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(1, h, 0), log);
            }
            setBlockIfAir(pos.add(1, 0, 1), pumpkin);
            break;

        case 2:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(0, 0, -1));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(-1, h, 0), log);
            }
            setBlockIfAir(pos.add(-1, 0, 1), pumpkin);
            break;

        case 3:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(-1, 0, 0));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(-1, h, -1), log);
            }
            setBlockIfAir(pos.add(-1, 0, -1), pumpkin);
            break;
        }
    }

    /**
     * Makes pumpkin patches.
     *
     * @param basePos
     * @param size
     * @return number of trees created
     * @throws MaxChangedBlocksException
     */
    public int makePumpkinPatches(Vector basePos, int size)
            throws MaxChangedBlocksException {
        int affected = 0;

        for (int x = basePos.getBlockX() - size; x <= basePos.getBlockX()
                + size; ++x) {
            for (int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ()
                    + size; ++z) {
                // Don't want to be in the ground
                if (!getBlock(new Vector(x, basePos.getBlockY(), z)).isAir()) {
                    continue;
                }
                // The gods don't want a pumpkin patch here
                if (Math.random() < 0.98) {
                    continue;
                }

                for (int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; --y) {
                    // Check if we hit the ground
                    int t = getBlock(new Vector(x, y, z)).getType();
                    if (t == BlockID.GRASS || t == BlockID.DIRT) {
                        makePumpkinPatch(new Vector(x, y + 1, z));
                        ++affected;
                        break;
                    } else if (t != BlockID.AIR) { // Trees won't grow on this!
                        break;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Makes a forest.
     *
     * @param basePos
     * @param size
     * @param density
     * @param treeGenerator
     * @return number of trees created
     * @throws MaxChangedBlocksException
     */
    public int makeForest(Vector basePos, int size, double density,
            TreeGenerator treeGenerator) throws MaxChangedBlocksException {
        int affected = 0;

        for (int x = basePos.getBlockX() - size; x <= basePos.getBlockX()
                + size; ++x) {
            for (int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ()
                    + size; ++z) {
                // Don't want to be in the ground
                if (!getBlock(new Vector(x, basePos.getBlockY(), z)).isAir()) {
                    continue;
                }
                // The gods don't want a tree here
                if (Math.random() >= density) {
                    continue;
                } // def 0.05

                for (int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; --y) {
                    // Check if we hit the ground
                    int t = getBlock(new Vector(x, y, z)).getType();
                    if (t == BlockID.GRASS || t == BlockID.DIRT) {
                        treeGenerator.generate(this, new Vector(x, y + 1, z));
                        ++affected;
                        break;
                    } else if (t != BlockID.AIR) { // Trees won't grow on this!
                        break;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Count the number of blocks of a list of types in a region.
     *
     * @param region
     * @param searchIDs
     * @return
     */
    public int countBlocks(Region region, Set<Integer> searchIDs) {
        int count = 0;

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        if (searchIDs.contains(getBlockType(pt))) {
                            ++count;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (searchIDs.contains(getBlockType(pt))) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * Get the block distribution inside a region.
     *
     * @param region
     * @return
     */
    public List<Countable<Integer>> getBlockDistribution(Region region) {
        List<Countable<Integer>> distribution = new ArrayList<Countable<Integer>>();
        Map<Integer, Countable<Integer>> map = new HashMap<Integer, Countable<Integer>>();

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        int id = getBlockType(pt);

                        if (map.containsKey(id)) {
                            map.get(id).increment();
                        } else {
                            Countable<Integer> c = new Countable<Integer>(id, 1);
                            map.put(id, c);
                            distribution.add(c);
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                int id = getBlockType(pt);

                if (map.containsKey(id)) {
                    map.get(id).increment();
                } else {
                    Countable<Integer> c = new Countable<Integer>(id, 1);
                    map.put(id, c);
                }
            }
        }

        Collections.sort(distribution);
        // Collections.reverse(distribution);

        return distribution;
    }

    /**
     * Returns the highest solid 'terrain' block which can occur naturally.
     *
     * @param x
     * @param z
     * @param minY minimal height
     * @param maxY maximal height
     * @param naturalOnly look at natural blocks or all blocks
     * @return height of highest block found or 'minY'
     */
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        return getHighestTerrainBlock(x, z, minY, maxY, false);
    }

    /**
     * Returns the highest solid 'terrain' block which can occur naturally.
     *
     * @param x
     * @param z
     * @param minY minimal height
     * @param maxY maximal height
     * @param naturalOnly look at natural blocks or all blocks
     * @return height of highest block found or 'minY'
     */
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY, boolean naturalOnly) {
        for (int y = maxY; y >= minY; --y) {
            Vector pt = new Vector(x, y, z);
            int id = getBlockType(pt);
            if (naturalOnly ? BlockType.isNaturalTerrainBlock(id) : !BlockType.canPassThrough(id)) {
                return y;
            }
        }
        return minY;
    }

    /**
     * Gets the list of missing blocks and clears the list for the next
     * operation.
     *
     * @return
     */
    public Set<Integer> popMissingBlocks() {
        Set<Integer> missingBlocks = this.missingBlocks;
        this.missingBlocks = new HashSet<Integer>();
        return missingBlocks;
    }

    /**
     * @return the blockBag
     */
    public BlockBag getBlockBag() {
        return blockBag;
    }

    /**
     * @param blockBag
     *            the blockBag to set
     */
    public void setBlockBag(BlockBag blockBag) {
        this.blockBag = blockBag;
    }

    /**
     * Get the world.
     *
     * @return
     */
    public LocalWorld getWorld() {
        return world;
    }

    /**
     * Get the number of blocks changed, including repeated block changes.
     *
     * @return
     */
    public int getBlockChangeCount() {
        return original.size();
    }

    /**
     * Get the mask.
     *
     * @return mask, may be null
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Set a mask.
     *
     * @param mask mask or null
     */
    public void setMask(Mask mask) {
        this.mask = mask;
    }
}
