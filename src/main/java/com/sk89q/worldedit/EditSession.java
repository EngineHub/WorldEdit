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
     * Block bag to use for getting blocks.
     */
    private BlockBag blockBag;
    /**
     * List of missing blocks;
     */
    private Set<Integer> missingBlocks = new HashSet<Integer>();

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
        int y = pt.getBlockY();
        int type = block.getType();
        
        if (y < 0 || y > 127) {
            return false;
        }
        
        // No invalid blocks
        if ((type > 32 && type < 35) || type == 36 || type == 29 || type > 96) {
            return false;
        }
        
        int existing = world.getBlockType(pt);

        // Clear the container block so that it doesn't drop items
        if (BlockType.isContainerBlock(existing) && blockBag == null) {
            world.clearContainerBlockContents(pt);
        // Ice turns until water so this has to be done first
        } else if (existing == BlockID.ICE) {
            world.setBlockType(pt, 0);
        }

        int id = block.getType();

        if (blockBag != null) {
            if (id > 0) {
                try {
                    blockBag.fetchPlacedBlock(id);
                } catch (UnplaceableBlockException e) {
                    return false;
                } catch (BlockBagException e) {
                    missingBlocks.add(id);
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

        boolean result = world.setBlockType(pt, id);
        if (id != 0) {
            if (BlockType.usesData(id)) {
                world.setBlockData(pt, block.getData());
            }

            // Signs
            if (block instanceof SignBlock) {
                SignBlock signBlock = (SignBlock) block;
                world.copyToWorld(pt, signBlock);
            // Chests
            } else if (block instanceof ChestBlock && blockBag == null) {
                ChestBlock chestBlock = (ChestBlock) block;
                world.copyToWorld(pt, chestBlock);
            // Furnaces
            } else if (block instanceof FurnaceBlock && blockBag == null) {
                FurnaceBlock furnaceBlock = (FurnaceBlock) block;
                world.copyToWorld(pt, furnaceBlock);
            // Dispenser
            } else if (block instanceof DispenserBlock && blockBag == null) {
                DispenserBlock dispenserBlock = (DispenserBlock) block;
                world.copyToWorld(pt, dispenserBlock);
            // Mob spawners
            } else if (block instanceof MobSpawnerBlock) {
                MobSpawnerBlock mobSpawnerblock = (MobSpawnerBlock) block;
                world.copyToWorld(pt, mobSpawnerblock);
            // Note blocks
            } else if (block instanceof NoteBlock) {
                NoteBlock noteBlock = (NoteBlock) block;
                world.copyToWorld(pt, noteBlock);
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
                return getBlockType(pt) != block.getType();
                // Destroy torches, etc. first
            } else if (BlockType.shouldPlaceLast(getBlockType(pt))) {
                rawSetBlock(pt, new BaseBlock(0));
            } else {
                queueAfter.put(pt.toBlockVector(), block);
                return getBlockType(pt) != block.getType();
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

    /**
     * Gets the block type at a position x, y, z.
     * 
     * @param pt
     * @return BaseBlock
     */
    public BaseBlock rawGetBlock(Vector pt) {
        int type = world.getBlockType(pt);
        int data = world.getBlockData(pt);

        // Sign
        if (type == BlockID.WALL_SIGN || type == BlockID.SIGN_POST) {
            SignBlock block = new SignBlock(type, data);
            world.copyFromWorld(pt, block);
            return block;
        // Chest
        } else if (type == BlockID.CHEST) {
            ChestBlock block = new ChestBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        // Furnace
        } else if (type == BlockID.FURNACE || type == BlockID.BURNING_FURNACE) {
            FurnaceBlock block = new FurnaceBlock(type, data);
            world.copyFromWorld(pt, block);
            return block;
        // Dispenser
        } else if (type == BlockID.DISPENSER) {
            DispenserBlock block = new DispenserBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        // Mob spawner
        } else if (type == BlockID.MOB_SPAWNER) {
            MobSpawnerBlock block = new MobSpawnerBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        // Note block
        } else if (type == BlockID.NOTE_BLOCK) {
            NoteBlock block = new NoteBlock(data);
            world.copyFromWorld(pt, block);
            return block;
        } else {
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
     * @param maxBlocks
     *            -1 to disable
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
    public int fillXZ(Vector origin, BaseBlock block, int radius, int depth,
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
                        affected++;
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

        for (int y = cy; y >= minY; y--) {
            Vector pt = new Vector(x, y, z);

            if (getBlock(pt).isAir()) {
                setBlock(pt, block);
                affected++;
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
    public int fillXZ(Vector origin, Pattern pattern, int radius, int depth,
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
                        affected++;
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

        for (int y = cy; y >= minY; y--) {
            Vector pt = new Vector(x, y, z);

            if (getBlock(pt).isAir()) {
                setBlock(pt, pattern.next(pt));
                affected++;
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
        size--;
        int affected = 0;

        int oX = pos.getBlockX();
        int oY = pos.getBlockY();
        int oZ = pos.getBlockZ();

        for (int x = oX - size; x <= oX + size; x++) {
            for (int z = oZ - size; z <= oZ + size; z++) {
                for (int y = oY; y <= maxY; y++) {
                    Vector pt = new Vector(x, y, z);

                    if (getBlockType(pt) != 0) {
                        setBlock(pt, new BaseBlock(0));
                        affected++;
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
        size--;
        int affected = 0;

        int oX = pos.getBlockX();
        int oY = pos.getBlockY();
        int oZ = pos.getBlockZ();

        for (int x = oX - size; x <= oX + size; x++) {
            for (int z = oZ - size; z <= oZ + size; z++) {
                for (int y = oY; y >= minY; y--) {
                    Vector pt = new Vector(x, y, z);

                    if (getBlockType(pt) != 0) {
                        setBlock(pt, new BaseBlock(0));
                        affected++;
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
        BaseBlock air = new BaseBlock(0);

        int minX = pos.getBlockX() - size;
        int maxX = pos.getBlockX() + size;
        int minY = Math.max(0, pos.getBlockY() - size);
        int maxY = Math.min(127, pos.getBlockY() + size);
        int minZ = pos.getBlockZ() - size;
        int maxZ = pos.getBlockZ() + size;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Vector p = new Vector(x, y, z);

                    if (getBlockType(p) == blockType) {
                        if (setBlock(p, air)) {
                            affected++;
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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector pt = new Vector(x, y, z);

                        if (setBlock(pt, block)) {
                            affected++;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (setBlock(pt, block)) {
                    affected++;
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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector pt = new Vector(x, y, z);

                        if (setBlock(pt, pattern.next(pt))) {
                            affected++;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (setBlock(pt, pattern.next(pt))) {
                    affected++;
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
    public int replaceBlocks(Region region, Set<Integer> fromBlockTypes,
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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector pt = new Vector(x, y, z);
                        int curBlockType = getBlockType(pt);

                        if ((fromBlockTypes == null && curBlockType != 0)
                                || (fromBlockTypes != null && fromBlockTypes
                                        .contains(curBlockType))) {
                            if (setBlock(pt, toBlock)) {
                                affected++;
                            }
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                int curBlockType = getBlockType(pt);

                if (fromBlockTypes == null && curBlockType != 0
                        || fromBlockTypes.contains(curBlockType)) {
                    if (setBlock(pt, toBlock)) {
                        affected++;
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
    public int replaceBlocks(Region region, Set<Integer> fromBlockTypes,
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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector pt = new Vector(x, y, z);
                        int curBlockType = getBlockType(pt);

                        if ((fromBlockTypes == null && curBlockType != 0)
                                || (fromBlockTypes != null && fromBlockTypes
                                        .contains(curBlockType))) {
                            if (setBlock(pt, pattern.next(pt))) {
                                affected++;
                            }
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                int curBlockType = getBlockType(pt);

                if (fromBlockTypes == null && curBlockType != 0
                        || fromBlockTypes.contains(curBlockType)) {
                    if (setBlock(pt, pattern.next(pt))) {
                        affected++;
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

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (setBlock(new Vector(x, y, minZ), block)) {
                    affected++;
                }
                if (setBlock(new Vector(x, y, maxZ), block)) {
                    affected++;
                }
                affected++;
            }
        }

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (setBlock(new Vector(minX, y, z), block)) {
                    affected++;
                }
                if (setBlock(new Vector(maxX, y, z), block)) {
                    affected++;
                }
            }
        }

        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (setBlock(new Vector(x, minY, z), block)) {
                    affected++;
                }
                if (setBlock(new Vector(x, maxY, z), block)) {
                    affected++;
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

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (setBlock(new Vector(x, y, minZ), block)) {
                    affected++;
                }
                if (setBlock(new Vector(x, y, maxZ), block)) {
                    affected++;
                }
                affected++;
            }
        }

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (setBlock(new Vector(minX, y, z), block)) {
                    affected++;
                }
                if (setBlock(new Vector(maxX, y, z), block)) {
                    affected++;
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

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = upperY; y >= lowerY; y--) {
                    Vector above = new Vector(x, y + 1, z);

                    if (y + 1 <= 127 && !getBlock(new Vector(x, y, z)).isAir()
                            && getBlock(above).isAir()) {
                        if (setBlock(above, block)) {
                            affected++;
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

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = upperY; y >= lowerY; y--) {
                    Vector above = new Vector(x, y + 1, z);

                    if (y + 1 <= 127 && !getBlock(new Vector(x, y, z)).isAir()
                            && getBlock(above).isAir()) {
                        if (setBlock(above, pattern.next(above))) {
                            affected++;
                        }
                        break;
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

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BaseBlock block = getBlock(new Vector(x, y, z));

                    if (!block.isAir() || copyAir) {
                        for (int i = 1; i <= count; i++) {
                            Vector pos = new Vector(x + xs * dir.getBlockX()
                                    * i, y + ys * dir.getBlockY() * i, z + zs
                                    * dir.getBlockZ() * i);

                            if (setBlock(pos, block)) {
                                affected++;
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

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
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
            affected++;
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
    public int drainArea(Vector pos, int radius)
            throws MaxChangedBlocksException {
        int affected = 0;

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        for (int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; x++) {
            for (int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; z++) {
                for (int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; y++) {
                    queue.push(new BlockVector(x, y, z));
                }
            }
        }

        while (!queue.empty()) {
            BlockVector cur = queue.pop();

            int type = getBlockType(cur);

            // Check block type
            if (type != 8 && type != 9 && type != 10 && type != 11) {
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

            for (int x = cur.getBlockX() - 1; x <= cur.getBlockX() + 1; x++) {
                for (int z = cur.getBlockZ() - 1; z <= cur.getBlockZ() + 1; z++) {
                    for (int y = cur.getBlockY() - 1; y <= cur.getBlockY() + 1; y++) {
                        BlockVector newPos = new BlockVector(x, y, z);

                        if (!cur.equals(newPos)) {
                            queue.push(newPos);
                        }
                    }
                }
            }

            if (setBlock(cur, new BaseBlock(0))) {
                affected++;
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
    public int fixLiquid(Vector pos, int radius, int moving, int stationary)
            throws MaxChangedBlocksException {
        int affected = 0;

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        for (int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; x++) {
            for (int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; z++) {
                for (int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; y++) {
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
            if (type != moving && type != stationary && type != 0) {
                continue;
            }

            // Don't want to revisit
            if (visited.contains(cur)) {
                continue;
            }

            visited.add(cur);

            if (setBlock(cur, stationaryBlock)) {
                affected++;
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
    private int makeHCylinderPoints(Vector center, int x, int z, int height,
            Pattern block) throws MaxChangedBlocksException {
        int affected = 0;

        if (x == 0) {
            for (int y = 0; y < height; y++) {
                setBlock(center.add(0, y, z), block);
                setBlock(center.add(0, y, -z), block);
                setBlock(center.add(z, y, 0), block);
                setBlock(center.add(-z, y, 0), block);
                affected += 4;
            }
        } else if (x == z) {
            for (int y = 0; y < height; y++) {
                setBlock(center.add(x, y, z), block);
                setBlock(center.add(-x, y, z), block);
                setBlock(center.add(x, y, -z), block);
                setBlock(center.add(-x, y, -z), block);
                affected += 4;
            }
        } else if (x < z) {
            for (int y = 0; y < height; y++) {
                setBlock(center.add(x, y, z), block);
                setBlock(center.add(-x, y, z), block);
                setBlock(center.add(x, y, -z), block);
                setBlock(center.add(-x, y, -z), block);
                setBlock(center.add(z, y, x), block);
                setBlock(center.add(-z, y, x), block);
                setBlock(center.add(z, y, -x), block);
                setBlock(center.add(-z, y, -x), block);
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
    public int makeHollowCylinder(Vector pos, Pattern block, int radius,
            int height) throws MaxChangedBlocksException {
        int x = 0;
        int z = radius;
        int d = (5 - radius * 4) / 4;
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

        affected += makeHCylinderPoints(pos, x, z, height, block);

        while (x < z) {
            x++;

            if (d >= 0) {
                z--;
                d += 2 * (x - z) + 1;
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
    private int makeCylinderPoints(Vector center, int x, int z, int height,
            Pattern block) throws MaxChangedBlocksException {
        int affected = 0;

        if (x == z) {
            for (int y = 0; y < height; y++) {
                for (int z2 = -z; z2 <= z; z2++) {
                    setBlock(center.add(x, y, z2), block);
                    setBlock(center.add(-x, y, z2), block);
                    affected += 2;
                }
            }
        } else if (x < z) {
            for (int y = 0; y < height; y++) {
                for (int x2 = -x; x2 <= x; x2++) {
                    for (int z2 = -z; z2 <= z; z2++) {
                        setBlock(center.add(x2, y, z2), block);
                        affected++;
                    }
                    setBlock(center.add(z, y, x2), block);
                    setBlock(center.add(-z, y, x2), block);
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
    public int makeCylinder(Vector pos, Pattern block, int radius, int height)
            throws MaxChangedBlocksException {
        int x = 0;
        int z = radius;
        int d = (5 - radius * 4) / 4;
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
            x++;

            if (d >= 0) {
                z--;
                d += 2 * (x - z) + 1;
            } else {
                d += 2 * x + 1;
            }

            affected += makeCylinderPoints(pos, x, z, height, block);
        }

        return affected;
    }

    /**
     * Makes a sphere.
     * 
     * @param pos
     * @param block
     * @param radius
     * @param filled
     * @return number of blocks changed
     * @throws MaxChangedBlocksException
     */
    public int makeSphere(Vector pos, Pattern block, int radius,
            boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        for (int x = 0; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = 0; z <= radius; z++) {
                    Vector vec = pos.add(x, y, z);
                    double d = vec.distance(pos);

                    if (d <= radius + 0.5 && (filled || d >= radius - 0.5)) {
                        if (setBlock(vec, block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(-x, y, z), block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(x, -y, z), block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(x, y, -z), block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(-x, -y, z), block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(x, -y, -z), block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(-x, y, -z), block)) {
                            affected++;
                        }
                        if (setBlock(pos.add(-x, -y, -z), block)) {
                            affected++;
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
    public int thaw(Vector pos, int radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        int radiusSq = (int)Math.pow(radius, 2);

        int ox = pos.getBlockX();
        int oy = pos.getBlockY();
        int oz = pos.getBlockZ();

        BaseBlock air = new BaseBlock(0);
        BaseBlock water = new BaseBlock(BlockID.STATIONARY_WATER);

        for (int x = ox - radius; x <= ox + radius; x++) {
            for (int z = oz - radius; z <= oz + radius; z++) {
                if ((new Vector(x, oy, z)).distanceSq(pos) > radiusSq) {
                    continue;
                }

                for (int y = 127; y >= 1; y--) {
                    Vector pt = new Vector(x, y, z);
                    int id = getBlockType(pt);

                    if (id == BlockID.ICE) { // Ice
                        if (setBlock(pt, water)) {
                            affected++;
                        }
                    } else if (id == BlockID.SNOW) {
                        if (setBlock(pt, air)) {
                            affected++;
                        }
                    } else if (id != 0) {
                        break;
                    }
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
    public int simulateSnow(Vector pos, int radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        int radiusSq = (int)Math.pow(radius, 2);

        int ox = pos.getBlockX();
        int oy = pos.getBlockY();
        int oz = pos.getBlockZ();

        BaseBlock ice = new BaseBlock(79);
        BaseBlock snow = new BaseBlock(78);

        for (int x = ox - radius; x <= ox + radius; x++) {
            for (int z = oz - radius; z <= oz + radius; z++) {
                if ((new Vector(x, oy, z)).distanceSq(pos) > radiusSq) {
                    continue;
                }

                for (int y = 127; y >= 1; y--) {
                    Vector pt = new Vector(x, y, z);
                    int id = getBlockType(pt);

                    // Snow should not cover these blocks
                    if (id == 6 // Saplings
                            || id == 10 // Lava
                            || id == 11 // Lava
                            || id == 37 // Yellow flower
                            || id == 38 // Red rose
                            || id == 39 // Brown mushroom
                            || id == 40 // Red mushroom
                            || id == 44 // Step
                            || id == 50 // Torch
                            || id == 51 // Fire
                            || id == 53 // Wood steps
                            || id == 55 // Redstone wire
                            || id == 59 // Crops
                            || (id >= 63 && id <= 72) || id == 75 // Redstone
                                                                  // torch
                            || id == 76 // Redstone torch
                            || id == 77 // Stone button
                            || id == 78 // Snow
                            || id == 79 // Ice
                            || id == 81 // Cactus
                            || id == 83 // Reed
                            || id == 85 // Fence
                            || id == 90) { // Portal
                        break;
                    }

                    // Ice!
                    if (id == 8 || id == 9) {
                        if (setBlock(pt, ice)) {
                            affected++;
                        }
                        break;
                    }

                    // Cover
                    if (id != 0) {
                        if (y == 127) { // Too high!
                            break;
                        }

                        if (setBlock(pt.add(0, 1, 0), snow)) {
                            affected++;
                        }
                        break;
                    }
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
        // BaseBlock logBlock = new BaseBlock(17);
        BaseBlock leavesBlock = new BaseBlock(18);

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
        if (pos.distance(basePos) > 4)
            return;
        if (getBlockType(pos) != 0)
            return;

        for (int i = -1; i > -3; i--) {
            Vector testPos = pos.add(0, i, 0);
            if (getBlockType(testPos) == 0) {
                pos = testPos;
            } else {
                break;
            }
        }

        setBlockIfAir(pos, new BaseBlock(18));

        int t = prng.nextInt(4);
        int h = prng.nextInt(3) - 1;

        if (t == 0) {
            if (prng.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(1, 0, 0));
            if (prng.nextBoolean())
                setBlockIfAir(pos.add(1, h, -1), new BaseBlock(18));
            setBlockIfAir(pos.add(0, 0, -1), new BaseBlock(86));
        } else if (t == 1) {
            if (prng.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(0, 0, 1));
            if (prng.nextBoolean())
                setBlockIfAir(pos.add(1, h, 0), new BaseBlock(18));
            setBlockIfAir(pos.add(1, 0, 1), new BaseBlock(86));
        } else if (t == 2) {
            if (prng.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(0, 0, -1));
            if (prng.nextBoolean())
                setBlockIfAir(pos.add(-1, h, 0), new BaseBlock(18));
            setBlockIfAir(pos.add(-1, 0, 1), new BaseBlock(86));
        } else if (t == 3) {
            if (prng.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(-1, 0, 0));
            if (prng.nextBoolean())
                setBlockIfAir(pos.add(-1, h, -1), new BaseBlock(18));
            setBlockIfAir(pos.add(-1, 0, -1), new BaseBlock(86));
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
                + size; x++) {
            for (int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ()
                    + size; z++) {
                // Don't want to be in the ground
                if (!getBlock(new Vector(x, basePos.getBlockY(), z)).isAir())
                    continue;
                // The gods don't want a pumpkin patch here
                if (Math.random() < 0.98) {
                    continue;
                }

                for (int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; y--) {
                    // Check if we hit the ground
                    int t = getBlock(new Vector(x, y, z)).getType();
                    if (t == 2 || t == 3) {
                        makePumpkinPatch(new Vector(x, y + 1, z));
                        affected++;
                        break;
                    } else if (t != 0) { // Trees won't grow on this!
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
                + size; x++) {
            for (int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ()
                    + size; z++) {
                // Don't want to be in the ground
                if (!getBlock(new Vector(x, basePos.getBlockY(), z)).isAir())
                    continue;
                // The gods don't want a tree here
                if (Math.random() >= density) {
                    continue;
                } // def 0.05

                for (int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; y--) {
                    // Check if we hit the ground
                    int t = getBlock(new Vector(x, y, z)).getType();
                    if (t == 2 || t == 3) {
                        treeGenerator.generate(this, new Vector(x, y + 1, z));
                        affected++;
                        break;
                    } else if (t != 0) { // Trees won't grow on this!
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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector pt = new Vector(x, y, z);

                        if (searchIDs.contains(getBlockType(pt))) {
                            count++;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (searchIDs.contains(getBlockType(pt))) {
                    count++;
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

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
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
     * Looks at: 1, 2, 3, 7, 12, 13, 14, 15, 16, 56, 73, 74, 87, 88, 89, 82
     * 
     * @param x
     * @param z
     * @param minY
     *            minimal height
     * @param maxY
     *            maximal height
     * @return height of highest block found or 'minY'
     */

    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            Vector pt = new Vector(x, y, z);
            int id = getBlockType(pt);

            if (id == 1 // stone
                    || id == 2 // grass
                    || id == 3 // dirt
                    || id == 7 // bedrock
                    || id == 12 // sand
                    || id == 13 // gravel
                    || id == 82 // clay
                    // hell
                    || id == 87 // netherstone
                    || id == 88 // slowsand
                    || id == 89 // lightstone
                    // ores
                    || id == 14 // coal ore
                    || id == 15 // iron ore
                    || id == 16 // gold ore
                    || id == 56 // diamond ore
                    || id == 73 // redstone ore
                    || id == 74 // redstone ore (active)
            ) {
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
}
