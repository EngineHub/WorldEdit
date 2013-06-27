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

package com.sk89q.worldedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.changelog.ApplyChangeLog;
import com.sk89q.worldedit.changelog.BlockChange;
import com.sk89q.worldedit.changelog.ChangeLog;
import com.sk89q.worldedit.changelog.LinkedListLog;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.expression.runtime.RValue;
import com.sk89q.worldedit.foundation.Extent;
import com.sk89q.worldedit.masks.AnyMask;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.operation.ChangeCountable;
import com.sk89q.worldedit.operation.Operation;
import com.sk89q.worldedit.operation.OperationHelper;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.transaction.DummyTransaction;
import com.sk89q.worldedit.transaction.Transaction;
import com.sk89q.worldedit.util.ArbitraryShape;
import com.sk89q.worldedit.util.ChangeList;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.TreeGenerator;

/**
 * Edit sessions maintain a log of changes for undo and enforce other WorldEdit
 * constructs transparently.
 */
public class EditSession implements Extent, ChangeCountable {

    private static final Random prng = new Random();
    
    private final LocalWorld world;

    private Transaction transaction;
    private ChangeLog history;
    private Mask mask;
    private int blockCount = 0;
    private int maxBlocks = -1;
    
    private BlockChange blockChange = new BlockChange();

    /**
     * Construct an instance.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks
     */
    public EditSession(LocalWorld world, int maxBlocks) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }

        this.history = new LinkedListLog();
        this.maxBlocks = maxBlocks;
        this.world = world;
        this.transaction = world.createTransaction();
    }

    /**
     * Construct an instance.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks
     * @param blockBag unused parameter
     */
    @Deprecated
    public EditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        this(world, maxBlocks);
    }

    /**
     * Get the world that this {@link EditSession} makes modifications on.
     *
     * @return the world
     */
    public LocalWorld getWorld() {
        return world;
    }

    /**
     * Get the history.
     * 
     * @return the history
     */
    public ChangeLog getHistory() {
        return history;
    }

    /**
     * Set the history.
     * 
     * @param history the history
     */
    public void setHistory(ChangeLog history) {
        this.history = history;
    }

    /**
     * Get the transaction.
     * 
     * @return the transaction
     */
    public Transaction getTransaction() {
        return transaction;
    }
    
    /**
     * Get the operation that must be run at the end to flush remaining changes.
     * 
     * @return an operation, or null if there is no operation to run
     */
    public Operation getFlushOperation() {
        return transaction.getFlushOperation();
    }

    /**
     * Get the mask.
     *
     * @return mask, or null if there is no mask set
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Set a mask.
     *
     * @param mask mask, or null if there is no mask set
     */
    public void setMask(Mask mask) {
        this.mask = mask;
    }

    /**
     * Fast mode is a legacy construct that skips physics.
     *
     * @param fastMode true to enable fast mode
     */
    @Deprecated
    public void setFastMode(boolean fastMode) {
        transaction.setApplyPhysics(fastMode);
    }

    /**
     * Fast mode is a legacy construct that skips physics.
     *
     * @return true if fast mode is enabled
     */
    @Deprecated
    public boolean hasFastMode() {
        return transaction.getApplyPhysics();
    }

    /**
     * Returns queue status.
     *
     * @return whether the queue is enabled
     */
    @Deprecated
    public boolean isQueueEnabled() {
        return transaction instanceof DummyTransaction;
    }

    /**
     * Queue certain types of block for better reproduction of those blocks.
     */
    @Deprecated
    public void enableQueue() {
        if (!isQueueEnabled()) {
            transaction = world.createTransaction();
        }
    }

    /**
     * Disable the queue. This will flush the queue.
     */
    @Deprecated
    public void disableQueue() {
        if (isQueueEnabled()) {
            Operation op = transaction.getFlushOperation();
            if (op != null) {
                OperationHelper.completeLegacy(op);
            }
            transaction = new DummyTransaction(world);
        }
    }

    /**
     * Get the maximum number of blocks that can be changed. 
     *
     * @return block change limit, -1 if disabled
     */
    public int getBlockChangeLimit() {
        return maxBlocks;
    }

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param limit -1 to disable
     */
    public void setBlockChangeLimit(int limit) {
        if (limit < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }
        this.maxBlocks = limit;
    }

    @Override
    public BaseBlock getBlock(Vector pt) {
        return world.getBlock(pt);
    }

    @Override
    public int getBlockType(Vector pt) {
        return world.getBlockType(pt);
    }

    @Override
    public int getBlockData(Vector pt) {
        return world.getBlockData(pt);
    }

    @Deprecated
    public BaseBlock rawGetBlock(Vector pt) {
        return world.getBlock(pt);
    }

    @Deprecated
    public boolean smartSetBlock(Vector location, BaseBlock block) {
        return setBlock(location, block, false, true);
    }

    @Deprecated
    public boolean rawSetBlock(Vector location, BaseBlock block) {
        return setBlock(location, block, false, false);
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) {
        return setBlock(location, block, true, true);
    }

    /**
     * Set a block at the given location.
     * 
     * @param location the location to set the block at
     * @param block the block to set
     * @param logHistory true to log this change in the history
     * @param withTransaction true to use the underlying {@link Transaction}
     * @return see {@link Extent#setBlock(Vector, BaseBlock)} for return value
     */
    public boolean setBlock(Vector location, BaseBlock block,
            boolean logHistory, boolean withTransaction) {
        int y = location.getBlockY();
        if (y < 0 || y > world.getMaxY()) {
            return false;
        }

        if (mask != null) {
            if (!mask.matches(this, location)) {
                return false;
            }
        }
        
        if (maxBlocks >= 0 && blockCount + 1 > maxBlocks) {
            throw new MaxChangedBlocksException(maxBlocks);
        }
        
        blockCount++;
        
        if (logHistory) {
            blockChange.setLocation(location);
            blockChange.setPrevious(getBlock(location));
            blockChange.setReplacement(block);
            history.add(blockChange);
        }
        
        if (withTransaction) {
            return transaction.setBlock(location, block);
        } else {
            return world.setBlock(location, block);
        }
    }

    /**
     * Helper method to set a block using a pattern.
     * 
     * @param location the location to set
     * @param pattern the pattern
     * @return see {@link Extent#setBlock(Vector, BaseBlock)} for return value
     * @deprecated use your own method
     */
    @Deprecated
    public boolean setBlock(Vector location, Pattern pattern) {
        return setBlock(location, pattern.next(location));
    }

    /**
     * Helper method to set a block only if the previous block there was air.
     * 
     * @param location the location to set
     * @param block the block
     * @return see {@link Extent#setBlock(Vector, BaseBlock)} for return value
     * @deprecated use your own method
     */
    @Deprecated
    public boolean setBlockIfAir(Vector location, BaseBlock block) {
        if (!getBlock(location).isAir()) {
            return false;
        } else {
            return setBlock(location, block);
        }
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
    @Deprecated
    public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c)
            throws MaxChangedBlocksException {
        if (Math.random() <= c) {
            return setBlockIfAir(pos, block);
        }
        return false;
    }

    /**
     * Add an entry to the history log without actually changing any blocks.
     * 
     * @param location the location of the supposed change
     * @param existing the existing block
     * @param block the new block
     */
    public void rememberChange(Vector location, BaseBlock existing, BaseBlock block) {
        blockChange.setLocation(location);
        blockChange.setPrevious(existing);
        blockChange.setReplacement(block);
        history.add(blockChange);
    }

    @Deprecated
    public void undo(EditSession sess) {
        ApplyChangeLog op = ApplyChangeLog.createUndo(sess, history);
        OperationHelper.completeLegacy(op);
    }

    @Deprecated
    public void redo(EditSession sess) {
        ApplyChangeLog op = ApplyChangeLog.createRedo(sess, history);
        OperationHelper.completeLegacy(op);
    }

    /**
     * Finish off the queue.
     * 
     * @deprecated will block; queue the operation returned by {@link #getFlushOperation()}
     */
    @Deprecated
    public void flushQueue() {
        Operation op = transaction.getFlushOperation();
        if (op != null) {
            OperationHelper.completeLegacy(op);
        }
    }

    /**
     * Get the number of changed blocks.
     *
     * @return the number of changed blocks
     */
    public int size() {
        return blockCount;
    }

    @Override
    public int getChangeCount() {
        return size();
    }

    /**
     * Get the number of blocks changed, including repeated block changes.
     *
     * @return the number of blocks changed
     * @deprecated see {@link #getChangeCount()}
     */
    @Deprecated
    public int getBlockChangeCount() {
        return size();
    }
    
    /* ------------------------------------------------- */

    /**
     * Returns the highest solid 'terrain' block which can occur naturally.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param minY minimal height
     * @param maxY maximal height
     * @return height of highest block found or 'minY'
     * @deprecated do this yourself
     */
    @Deprecated
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        return getHighestTerrainBlock(x, z, minY, maxY, false);
    }

    /**
     * Returns the highest solid 'terrain' block which can occur naturally.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param minY minimal height
     * @param maxY maximal height
     * @param naturalOnly look at natural blocks or all blocks
     * @return height of highest block found or 'minY'
     * @deprecated do this yourself
     */
    @Deprecated
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

    @Deprecated
    public int countBlock(Region region, Set<Integer> searchIDs) {
        Set<BaseBlock> passOn = new HashSet<BaseBlock>();
        for (Integer i : searchIDs) {
            passOn.add(new BaseBlock(i, -1));
        }
        return countBlocks(region, passOn);
    }

    /**
     * Count the number of blocks of a list of types in a region.
     *
     * @param region
     * @param searchBlocks
     * @return the number of blocks
     */
    @Deprecated
    public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
        int count = 0;

        // allow -1 data in the searchBlocks to match any type
        Set<BaseBlock> newSet = new HashSet<BaseBlock>() {
            private static final long serialVersionUID = -6365410788588283289L;

            @Override
            public boolean contains(Object o) {
                for (BaseBlock b : this.toArray(new BaseBlock[this.size()])) {
                    if (o instanceof BaseBlock) {
                        if (b.equalsFuzzy((BaseBlock) o)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        newSet.addAll(searchBlocks);

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

                        BaseBlock compare = new BaseBlock(getBlockType(pt), getBlockData(pt));
                        if (newSet.contains(compare)) {
                            ++count;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BaseBlock compare = new BaseBlock(getBlockType(pt), getBlockData(pt));
                if (newSet.contains(compare)) {
                    ++count;
                }
            }
        }

        return count;
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public int removeAbove(Vector pos, int size, int height)
            throws MaxChangedBlocksException {
        int maxY = Math.min(world.getMaxY(), pos.getBlockY() + height - 1);
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
    @Deprecated
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
    @Deprecated
    public int removeNear(Vector pos, int blockType, int size)
            throws MaxChangedBlocksException {
        int affected = 0;
        BaseBlock air = new BaseBlock(BlockID.AIR);

        int minX = pos.getBlockX() - size;
        int maxX = pos.getBlockX() + size;
        int minY = Math.max(0, pos.getBlockY() - size);
        int maxY = Math.min(world.getMaxY(), pos.getBlockY() + size);
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
    @Deprecated
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
     * @param region the region
     * @param pattern the pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException too many blocks changed
     */
    @Deprecated
    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return replaceBlocks(region, new AnyMask(), pattern);
    }

    /**
     * Sets all the blocks that match a given mask inside a region 
     * to a certain block type.
     *
     * @param region the region
     * @param mask the mask
     * @param pattern the pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException too many blocks changed
     */
    @Deprecated
    public int replaceBlocks(Region region, Mask mask, Pattern pattern)
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
                        
                        if (!mask.matches(this, pt)) {
                            continue;
                        }

                        if (setBlock(pt, pattern.next(pt))) {
                            ++affected;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (!mask.matches(this, pt)) {
                    continue;
                }
                
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
    @Deprecated
    public int replaceBlocks(Region region, Set<BaseBlock> fromBlockTypes, BaseBlock toBlock) throws MaxChangedBlocksException {
        Set<BaseBlock> definiteBlockTypes = new HashSet<BaseBlock>();
        Set<Integer> fuzzyBlockTypes = new HashSet<Integer>();

        if (fromBlockTypes != null) {
            for (BaseBlock block : fromBlockTypes) {
                if (block.getData() == -1) {
                    fuzzyBlockTypes.add(block.getType());
                } else {
                    definiteBlockTypes.add(block);
                }
            }
        }

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

                        if (fromBlockTypes == null) {
                            //replace <to-block>
                            if (curBlockType.isAir()) {
                                continue;
                            }
                        } else {
                            //replace <from-block> <to-block>
                            if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
                                continue;
                            }
                        }

                        if (setBlock(pt, toBlock)) {
                            ++affected;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BaseBlock curBlockType = getBlock(pt);

                if (fromBlockTypes == null) {
                    //replace <to-block>
                    if (curBlockType.isAir()) {
                        continue;
                    }
                } else {
                    //replace <from-block> <to-block>
                    if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
                        continue;
                    }
                }

                if (setBlock(pt, toBlock)) {
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
     * @param pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    @Deprecated
    public int replaceBlocks(Region region, Set<BaseBlock> fromBlockTypes, Pattern pattern) throws MaxChangedBlocksException {
        if (pattern instanceof SingleBlockPattern) {
            return replaceBlocks(region, fromBlockTypes, 
                    ((SingleBlockPattern) pattern).getBlock());
        }
        
        Set<BaseBlock> definiteBlockTypes = new HashSet<BaseBlock>();
        Set<Integer> fuzzyBlockTypes = new HashSet<Integer>();
        if (fromBlockTypes != null) {
            for (BaseBlock block : fromBlockTypes) {
                if (block.getData() == -1) {
                    fuzzyBlockTypes.add(block.getType());
                } else {
                    definiteBlockTypes.add(block);
                }
            }
        }

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

                        if (fromBlockTypes == null) {
                            //replace <to-block>
                            if (curBlockType.isAir()) {
                                continue;
                            }
                        } else {
                            //replace <from-block> <to-block>
                            if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
                                continue;
                            }
                        }

                        if (setBlock(pt, pattern.next(pt))) {
                            ++affected;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BaseBlock curBlockType = getBlock(pt);

                if (fromBlockTypes == null) {
                    //replace <to-block>
                    if (curBlockType.isAir()) {
                        continue;
                    }
                } else {
                    //replace <from-block> <to-block>
                    if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
                        continue;
                    }
                }

                if (setBlock(pt, pattern.next(pt))) {
                    ++affected;
                }
            }
        }

        return affected;
    }

    @Deprecated
    public int center(Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        Vector center = region.getCenter();
        int x2 = center.getBlockX();
        int y2 = center.getBlockY();
        int z2 = center.getBlockZ();

        int affected = 0;
        for (int x = (int) center.getX(); x <= x2; x++) {
            for (int y = (int) center.getY(); y <= y2; y++) {
                for (int z = (int) center.getZ(); z <= z2; z++) {
                    if (setBlock(new Vector(x, y, z), pattern)) {
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
    @Deprecated
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
    @Deprecated
    public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException {
        if (pattern instanceof SingleBlockPattern) {
            return makeCuboidFaces(region, ((SingleBlockPattern) pattern).getBlock());
        }
        
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
    @Deprecated
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
     * @param region the region
     * @param pattern the patternt o use
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    @Deprecated
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
    @Deprecated
    public int overlayCuboidBlocks(Region region, BaseBlock block)
            throws MaxChangedBlocksException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int upperY = Math.min(world.getMaxY(), max.getBlockY() + 1);
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

                    if (y + 1 <= world.getMaxY() && !getBlock(new Vector(x, y, z)).isAir()
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
    @Deprecated
    public int overlayCuboidBlocks(Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        if (pattern instanceof SingleBlockPattern) {
            return overlayCuboidBlocks(region, ((SingleBlockPattern) pattern).getBlock());
        }
        
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int upperY = Math.min(world.getMaxY(), max.getBlockY() + 1);
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

                    if (y + 1 <= world.getMaxY() && !getBlock(new Vector(x, y, z)).isAir()
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
    @Deprecated
    public int naturalizeCuboidBlocks(Region region)
            throws MaxChangedBlocksException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int upperY = Math.min(world.getMaxY(), max.getBlockY() + 1);
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
    @Deprecated
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
     * @param region the region
     * @param dir the direction
     * @param distance the distance
     * @param copyAir the air to copy
     * @param fill the pattern to fill with
     * @return number of blocks moved
     * @throws MaxChangedBlocksException
     */
    @Deprecated
    public int moveCuboidRegion(
            Region region, Vector dir, int distance, boolean copyAir, Pattern fill)
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
                            setBlock(pos, fill.next(pos));
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
    @Deprecated
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
    @Deprecated
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

            if (setBlock(cur, stationaryBlock)) {
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
     * Makes a cylinder.
     *
     * @param pos Center of the cylinder
     * @param block The block pattern to use
     * @param radius The cylinder's radius
     * @param height The cylinder's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException
     */
    @Deprecated
    public int makeCylinder(Vector pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException {
        return makeCylinder(pos, block, radius, radius, height, filled);
    }

    /**
     * Makes a cylinder.
     *
     * @param pos Center of the cylinder
     * @param block The block pattern to use
     * @param radiusX The cylinder's largest north/south extent
     * @param radiusZ The cylinder's largest east/west extent
     * @param height The cylinder's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException
     */
    @Deprecated
    public int makeCylinder(Vector pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radiusX += 0.5;
        radiusZ += 0.5;

        if (height == 0) {
            return 0;
        } else if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
        }

        if (pos.getBlockY() < 0) {
            pos = pos.setY(0);
        } else if (pos.getBlockY() + height - 1 > world.getMaxY()) {
            height = world.getMaxY() - pos.getBlockY() + 1;
        }

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                if (!filled) {
                    if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                        continue;
                    }
                }

                for (int y = 0; y < height; ++y) {
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

        return affected;
    }

    /**
    * Makes a sphere.
    *
    * @param pos Center of the sphere or ellipsoid
    * @param block The block pattern to use
    * @param radius The sphere's radius
    * @param filled If false, only a shell will be generated.
    * @return number of blocks changed
    * @throws MaxChangedBlocksException
    */
    @Deprecated
    public int makeSphere(Vector pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
        return makeSphere(pos, block, radius, radius, radius, filled);
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
    @Deprecated
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
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
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

    private static final double lengthSq(double x, double z) {
        return (x * x) + (z * z);
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
    @Deprecated
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
    @Deprecated
    public int thaw(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius * radius;

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

                for (int y = world.getMaxY(); y >= 1; --y) {
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
    @Deprecated
    public int simulateSnow(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius * radius;

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

                for (int y = world.getMaxY(); y >= 1; --y) {
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
                    if (BlockType.isTranslucent(id)) {
                        break;
                    }

                    // Too high?
                    if (y == world.getMaxY()) {
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
    @Deprecated
    public int green(Vector pos, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        final double radiusSq = radius * radius;

        final int ox = pos.getBlockX();
        final int oy = pos.getBlockY();
        final int oz = pos.getBlockZ();

        final BaseBlock grass = new BaseBlock(BlockID.GRASS);

        final int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(pos) > radiusSq) {
                    continue;
                }

                loop: for (int y = world.getMaxY(); y >= 1; --y) {
                    final Vector pt = new Vector(x, y, z);
                    final int id = getBlockType(pt);

                    switch (id) {
                    case BlockID.DIRT:
                        if (setBlock(pt, grass)) {
                            ++affected;
                        }
                        break loop;

                    case BlockID.WATER:
                    case BlockID.STATIONARY_WATER:
                    case BlockID.LAVA:
                    case BlockID.STATIONARY_LAVA:
                        // break on liquids...
                        break loop;

                    default:
                        // ...and all non-passable blocks
                        if (!BlockType.canPassThrough(id)) {
                            break loop;
                        }
                    }
                }
            }
        }

        return affected;
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

        switch (t) {
        case 0:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(1, 0, 0));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(1, h, -1), log);
            }
            setBlockIfAir(pos.add(0, 0, -1), new BaseBlock(BlockID.PUMPKIN, prng.nextInt(4)));
            break;

        case 1:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(0, 0, 1));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(1, h, 0), log);
            }
            setBlockIfAir(pos.add(1, 0, 1), new BaseBlock(BlockID.PUMPKIN, prng.nextInt(4)));
            break;

        case 2:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(0, 0, -1));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(-1, h, 0), log);
            }
            setBlockIfAir(pos.add(-1, 0, 1), new BaseBlock(BlockID.PUMPKIN, prng.nextInt(4)));
            break;

        case 3:
            if (prng.nextBoolean()) {
                makePumpkinPatchVine(basePos, pos.add(-1, 0, 0));
            }
            if (prng.nextBoolean()) {
                setBlockIfAir(pos.add(-1, h, -1), log);
            }
            setBlockIfAir(pos.add(-1, 0, -1), new BaseBlock(BlockID.PUMPKIN, prng.nextInt(4)));
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
    @Deprecated
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
    @Deprecated
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
     * Get the block distribution inside a region.
     *
     * @param region the region to check
     * @return a block distribution list
     */
    @Deprecated
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
     * Get the block distribution (with data values) inside a region.
     *
     * @param region the region to check
     * @return a block distribution
     */
    @Deprecated
    // TODO reduce code duplication - probably during ops-redux
    public List<Countable<BaseBlock>> getBlockDistributionWithData(Region region) {
        List<Countable<BaseBlock>> distribution = new ArrayList<Countable<BaseBlock>>();
        Map<BaseBlock, Countable<BaseBlock>> map = new HashMap<BaseBlock, Countable<BaseBlock>>();

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

                        BaseBlock blk = new BaseBlock(getBlockType(pt), getBlockData(pt));

                        if (map.containsKey(blk)) {
                            map.get(blk).increment();
                        } else {
                            Countable<BaseBlock> c = new Countable<BaseBlock>(blk, 1);
                            map.put(blk, c);
                            distribution.add(c);
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BaseBlock blk = new BaseBlock(getBlockType(pt), getBlockData(pt));

                if (map.containsKey(blk)) {
                    map.get(blk).increment();
                } else {
                    Countable<BaseBlock> c = new Countable<BaseBlock>(blk, 1);
                    map.put(blk, c);
                }
            }
        }

        Collections.sort(distribution);
        // Collections.reverse(distribution);

        return distribution;
    }

    @Deprecated
    public int makeShape(final Region region, final Vector zero, final Vector unit, final Pattern pattern, final String expressionString, final boolean hollow) throws ExpressionException, MaxChangedBlocksException {
        final Expression expression = Expression.compile(expressionString, "x", "y", "z", "type", "data");
        expression.optimize();

        final RValue typeVariable = expression.getVariable("type", false);
        final RValue dataVariable = expression.getVariable("data", false);

        final ArbitraryShape shape = new ArbitraryShape(region) {
            @Override
            protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
                final Vector scaled = new Vector(x, y, z).subtract(zero).divide(unit);

                try {
                    if (expression.evaluate(scaled.getX(), scaled.getY(), scaled.getZ(), defaultMaterial.getType(), defaultMaterial.getData()) <= 0) {
                        return null;
                    }

                    return new BaseBlock((int) typeVariable.getValue(), (int) dataVariable.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        return shape.generate(this, pattern, hollow);
    }

    @Deprecated
    public int deformRegion(final Region region, final Vector zero, final Vector unit, final String expressionString) throws ExpressionException, MaxChangedBlocksException {
        final Expression expression = Expression.compile(expressionString, "x", "y", "z");
        expression.optimize();

        final RValue x = expression.getVariable("x", false);
        final RValue y = expression.getVariable("y", false);
        final RValue z = expression.getVariable("z", false);

        Vector zero2 = zero.add(0.5, 0.5, 0.5);

        final ChangeList queue = new ChangeList(false);

        for (BlockVector position : region) {
            // offset, scale
            final Vector scaled = position.subtract(zero).divide(unit);

            // transform
            expression.evaluate(scaled.getX(), scaled.getY(), scaled.getZ());

            final Vector sourceScaled = new Vector(x.getValue(), y.getValue(), z.getValue());

            // unscale, unoffset, round-nearest
            final BlockVector sourcePosition = sourceScaled.multiply(unit).add(zero2).toBlockPoint();

            // read block from world
            BaseBlock material = new BaseBlock(world.getBlockType(sourcePosition), world.getBlockData(sourcePosition));

            // queue operation
            queue.put(position, material);
        }

        int affected = 0;
        for (Map.Entry<BlockVector, BaseBlock> entry : queue) {
            BlockVector position = entry.getKey();
            BaseBlock material = entry.getValue();

            // set at new position
            if (setBlock(position, material)) {
                ++affected;
            }
        }

        return affected;
    }

    Vector[] recurseDirections = {
        PlayerDirection.NORTH.vector(),
        PlayerDirection.EAST.vector(),
        PlayerDirection.SOUTH.vector(),
        PlayerDirection.WEST.vector(),
        PlayerDirection.UP.vector(),
        PlayerDirection.DOWN.vector(),
    };

    /**
     * Hollows out the region (Semi-well-defined for non-cuboid selections).
     *
     * @param region the region to hollow out.
     * @param thickness the thickness of the shell to leave (manhattan distance)
     * @param pattern The block pattern to use
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    @Deprecated
    public int hollowOutRegion(Region region, int thickness, Pattern pattern) throws MaxChangedBlocksException {
        int affected = 0;

        final Set<BlockVector> outside = new HashSet<BlockVector>();

        final Vector min = region.getMinimumPoint();
        final Vector max = region.getMaximumPoint();

        final int minX = min.getBlockX();
        final int minY = min.getBlockY();
        final int minZ = min.getBlockZ();
        final int maxX = max.getBlockX();
        final int maxY = max.getBlockY();
        final int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                recurseHollow(region, new BlockVector(x, y, minZ), outside);
                recurseHollow(region, new BlockVector(x, y, maxZ), outside);
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                recurseHollow(region, new BlockVector(minX, y, z), outside);
                recurseHollow(region, new BlockVector(maxX, y, z), outside);
            }
        }

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                recurseHollow(region, new BlockVector(x, minY, z), outside);
                recurseHollow(region, new BlockVector(x, maxY, z), outside);
            }
        }

        for (int i = 1; i < thickness; ++i) {
            final Set<BlockVector> newOutside = new HashSet<BlockVector>();
            outer: for (BlockVector position : region) {
                for (Vector recurseDirection: recurseDirections) {
                    BlockVector neighbor = position.add(recurseDirection).toBlockVector();

                    if (outside.contains(neighbor)) {
                        newOutside.add(position);
                        continue outer;
                    }
                }
            }

            outside.addAll(newOutside);
        }

        outer: for (BlockVector position : region) {
            for (Vector recurseDirection: recurseDirections) {
                BlockVector neighbor = position.add(recurseDirection).toBlockVector();

                if (outside.contains(neighbor)) {
                    continue outer;
                }
            }

            if (setBlock(position, pattern.next(position))) {
                ++affected;
            }
        }

        return affected;
    }

    private void recurseHollow(Region region, BlockVector origin, Set<BlockVector> outside) {
        final LinkedList<BlockVector> queue = new LinkedList<BlockVector>();
        queue.addLast(origin);

        while (!queue.isEmpty()) {
            final BlockVector current = queue.removeFirst();
            if (!BlockType.canPassThrough(getBlockType(current))) {
                continue;
            }

            if (!outside.add(current)) {
                continue;
            }

            if (!region.contains(current)) {
                continue;
            }

            for (Vector recurseDirection: recurseDirections) {
                queue.addLast(current.add(recurseDirection).toBlockVector());
            }
        } // while
    }

}
