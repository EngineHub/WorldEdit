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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.data.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class can wrap all block editing operations into one "edit session" that
 * stores the state of the blocks before modification. This allows for easy
 * undo or redo. In addition to that, this class can use a "queue mode" that
 * will know how to handle some special types of items such as signs and
 * torches. For example, torches must be placed only after there is already
 * a block below it, otherwise the torch will be placed as an item.
 *
 * @author sk89q
 */
public class EditSession {
    /**
     * Server interface.
     */
    protected ServerInterface server;
    
    /**
     * Stores the original blocks before modification.
     */
    private Map<BlockVector,BaseBlock> original = new HashMap<BlockVector,BaseBlock>();
    /**
     * Stores the current blocks.
     */
    private Map<BlockVector,BaseBlock> current = new HashMap<BlockVector,BaseBlock>();
    /**
     * Queue.
     */
    private Map<BlockVector,BaseBlock> queue = new HashMap<BlockVector,BaseBlock>();
    /**
     * The maximum number of blocks to change at a time. If this number is
     * exceeded, a MaxChangedBlocksException exception will be
     * raised. -1 indicates no limit.
     */
    private int maxBlocks = -1;
    /**
     * Indicates whether some types of blocks should be queued for best
     * reproduction.
     */
    private boolean queued = false;
    /**
     * Random number generator.
     */
    private static Random prng = new Random();

    /**
     * Default constructor. There is no maximum blocks limit.
     */
    public EditSession() {
        server = WorldEditController.getServer();
    }

    /**
     * Construct the object with a maximum number of blocks.
     */
    public EditSession(int maxBlocks) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }
        this.maxBlocks = maxBlocks;

        server = WorldEditController.getServer();
    }

    /**
     * Sets a block without changing history.
     * 
     * @param pt
     * @param blockType
     * @return Whether the block changed
     */
    private boolean rawSetBlock(Vector pt, BaseBlock block) {
        int y = pt.getBlockY();
        if (y < 0 || y > 127) {
            return false;
        }
        
        boolean result = server.setBlockType(pt, block.getID());
        if (block.getID() != 0) {
            server.setBlockData(pt, block.getData());

            // Signs
            if (block instanceof SignBlock) {
                SignBlock signBlock = (SignBlock)block;
                String[] text = signBlock.getText();
                server.setSignText(pt, text);
            // Chests
            } else if (block instanceof ChestBlock) {
                ChestBlock chestBlock = (ChestBlock)block;
                BaseItem blankItem = new BaseItem((short)1);
                Map<Byte,Countable<BaseItem>> items = chestBlock.getItems();
                for (byte i = 0; i <= 26; i++) {
                    Countable<BaseItem> item = items.get(i);
                    if (item != null) {
                        server.setChestSlot(pt, i, item.getID(),
                                item.getAmount());
                    } else {
                        server.setChestSlot(pt, i, blankItem, 0);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Sets the block at position x, y, z with a block type. If queue mode is
     * enabled, blocks may not be actually set in world until flushQueue()
     * is called.
     *
     * @param pt
     * @param block
     * @return Whether the block changed -- not entirely dependable
     */
    public boolean setBlock(Vector pt, BaseBlock block)
        throws MaxChangedBlocksException {
        BlockVector blockPt = pt.toBlockVector();
        
        if (!original.containsKey(blockPt)) {
            original.put(blockPt, getBlock(pt));

            if (maxBlocks != -1 && original.size() > maxBlocks) {
                throw new MaxChangedBlocksException(maxBlocks);
            }
        }

        current.put(pt.toBlockVector(), block);

        return smartSetBlock(pt, block);
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
    private boolean smartSetBlock(Vector pt, BaseBlock block) {
        if (queued) {
            if (!block.isAir() && BlockType.shouldPlaceLast(block.getID())
                    && rawGetBlock(pt.add(0, -1, 0)).isAir()) {
                queue.put(pt.toBlockVector(), block);
                return getBlock(pt).getID() != block.getID();
            } else if (block.isAir()
                    && BlockType.shouldPlaceLast(rawGetBlock(pt.add(0, 1, 0)).getID())) {
                rawSetBlock(pt.add(0, 1, 0), new BaseBlock(0)); // Prevent items from being dropped
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
            BlockVector blockPt = pt.toBlockVector();

            if (current.containsKey(blockPt)) {
                return current.get(blockPt);
            }
        }
        
        return rawGetBlock(pt);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param pt
     * @return BaseBlock
     */
    public BaseBlock rawGetBlock(Vector pt) {
        int type = server.getBlockType(pt);
        int data = server.getBlockData(pt);

        // Sign
        if (type == 63 || type == 68) {
            String[] text = server.getSignText(pt);
            return new SignBlock(type, data, text);
        // Chest
        } else if (type == 54) {
            Map<Byte,Countable<BaseItem>> items = server.getChestContents(pt);
            return new ChestBlock(data, items);
        } else {
            return new BaseBlock(type, data);
        }
    }

    /**
     * Restores all blocks to their initial state.
     */
    public void undo() {
        for (Map.Entry<BlockVector,BaseBlock> entry : original.entrySet()) {
            BlockVector pt = (BlockVector)entry.getKey();
            smartSetBlock(pt, (BaseBlock)entry.getValue());
        }
        flushQueue();
    }

    /**
     * Sets to new state.
     */
    public void redo() {
        for (Map.Entry<BlockVector,BaseBlock> entry : current.entrySet()) {
            BlockVector pt = (BlockVector)entry.getKey();
            smartSetBlock(pt, (BaseBlock)entry.getValue());
        }
        flushQueue();
    }

    /**
     * Get the number of changed blocks.
     * 
     */
    public int size() {
        return original.size();
    }

    /**
     * Get the maximum number of blocks that can be changed. -1 will be
     * returned if disabled.
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
     * Finish off the queue.
     */
    public void flushQueue() {
        if (!queued) { return; }
        
        for (Map.Entry<BlockVector,BaseBlock> entry : queue.entrySet()) {
            BlockVector pt = (BlockVector)entry.getKey();
            rawSetBlock(pt, (BaseBlock)entry.getValue());
        }
    }

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param x
     * @param z
     * @param origin
     * @param block
     * @param radius
     * @param depth
     * @return number of blocks affected
     */
    public int fillXZ(int x, int z, Vector origin, BaseBlock block,
            int radius, int depth)
            throws MaxChangedBlocksException {
        
        int affected = 0;
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        queue.push(new BlockVector(x, 0, z));

        while (!queue.empty()) {
            BlockVector pt = queue.pop();
            int cx = pt.getBlockX();
            int cz = pt.getBlockZ();

            if (visited.contains(pt)) {
                continue;
            }

            visited.add(pt);

            double dist = Math.sqrt(Math.pow(originX - cx, 2)
                    + Math.pow(originZ - cz, 2));
            int minY = originY - depth + 1;

            if (dist > radius) {
                continue;
            }

            if (getBlock(new Vector(cx, originY, cz)).isAir()) {
                affected += fillY(cx, originY, cz, block, minY);
            } else {
                continue;
            }

            queue.push(new BlockVector(cx + 1, 0, cz));
            queue.push(new BlockVector(cx - 1, 0, cz));
            queue.push(new BlockVector(cx, 0, cz + 1));
            queue.push(new BlockVector(cx, 0, cz - 1));
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
     * Remove blocks above.
     * 
     * @param pos
     * @param size
     * @param height
     * @return number of blocks affected
     */
    public int removeAbove(Vector pos, int size, int height) throws
            MaxChangedBlocksException {
        int maxY = Math.min(127, pos.getBlockY() + height - 1);
        size--;
        int affected = 0;

        for (int x = (int)pos.getX() - size; x <= (int)pos.getX() + size; x++) {
            for (int z = (int)pos.getZ() - size; z <= (int)pos.getZ() + size; z++) {
                for (int y = (int)pos.getY(); y <= maxY; y++) {
                    Vector pt = new Vector(x, y, z);

                    if (!getBlock(pt).isAir()) {
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
     */
    public int removeBelow(Vector pos, int size, int height) throws
            MaxChangedBlocksException {
        int minY = Math.max(0, pos.getBlockY() - height);
        size--;
        int affected = 0;

        for (int x = (int)pos.getX() - size; x <= (int)pos.getX() + size; x++) {
            for (int z = (int)pos.getZ() - size; z <= (int)pos.getZ() + size; z++) {
                for (int y = (int)pos.getY(); y >= minY; y--) {
                    Vector pt = new Vector(x, y, z);

                    if (!getBlock(pt).isAir()) {
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
     */
    public int removeNear(Vector pos, int blockType, int size) throws
            MaxChangedBlocksException {
        int affected = 0;
        BaseBlock air = new BaseBlock(0);

        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                for (int z = -size; z <= size; z++) {
                    Vector p = pos.add(x, y, z);

                    if (getBlock(p).getID() == blockType) {
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

            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
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
     * Replaces all the blocks of a type inside a region to another block type.
     *
     * @param region
     * @param fromBlockType -1 for non-air
     * @param toBlockType
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int replaceBlocks(Region region, int fromBlockType, BaseBlock toBlock)
            throws MaxChangedBlocksException {
        int affected = 0;

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        Vector pt = new Vector(x, y, z);
                        int curBlockType = getBlock(pt).getID();

                        if (fromBlockType == -1 && curBlockType != 0 ||
                                curBlockType == fromBlockType) {
                            if (setBlock(pt, toBlock)) {
                                affected++;
                            }
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                int curBlockType = getBlock(pt).getID();

                if (fromBlockType == -1 && curBlockType != 0 ||
                        curBlockType == fromBlockType) {
                    if (setBlock(pt, toBlock)) {
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

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                if (setBlock(new Vector(x, y, min.getBlockZ()), block)) { affected++; }
                if (setBlock(new Vector(x, y, max.getBlockZ()), block)) { affected++; }
                affected++;
            }
        }

        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                if (setBlock(new Vector(min.getBlockX(), y, z), block)) { affected++; }
                if (setBlock(new Vector(max.getBlockX(), y, z), block)) { affected++; }
            }
        }

        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                if (setBlock(new Vector(x, min.getBlockY(), z), block)) { affected++; }
                if (setBlock(new Vector(x, max.getBlockY(), z), block)) { affected++; }
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

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                if (setBlock(new Vector(x, y, min.getBlockZ()), block)) { affected++; }
                if (setBlock(new Vector(x, y, max.getBlockZ()), block)) { affected++; }
                affected++;
            }
        }

        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                if (setBlock(new Vector(min.getBlockX(), y, z), block)) { affected++; }
                if (setBlock(new Vector(max.getBlockX(), y, z), block)) { affected++; }
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
        int lowerY = Math.max(0, min.getBlockY()- 1);

        int affected = 0;

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
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
     * Stack a cuboid region.
     * 
     * @param region
     * @param dir
     * @param count
     * @param copyAir
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int stackCuboidRegion(Region region, Vector dir,
            int count, boolean copyAir)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        int xs = region.getWidth();
        int ys = region.getHeight();
        int zs = region.getLength();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    BaseBlock block = getBlock(new Vector(x, y, z));

                    if (!block.isAir() || copyAir) {
                        for (int i = 1; i <= count; i++) {
                            Vector pos = new Vector(
                                    x + xs * dir.getBlockX() * i,
                                    y + ys * dir.getBlockY() * i,
                                    z + zs * dir.getBlockZ() * i);
                            
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
    public int moveCuboidRegion(Region region, Vector dir,
            int distance, boolean copyAir, BaseBlock replace)
            throws MaxChangedBlocksException {
        int affected = 0;

        Vector shift = dir.multiply(distance);
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Vector newMin = min.add(shift);
        Vector newMax = min.add(shift);
        int xs = region.getWidth();
        int ys = region.getHeight();
        int zs = region.getLength();

        Map<Vector,BaseBlock> delayed = new LinkedHashMap<Vector,BaseBlock>();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    Vector pos = new Vector(x, y, z);
                    BaseBlock block = getBlock(pos);

                    if (!block.isAir() || copyAir) {
                        Vector newPos = pos.add(shift);

                        delayed.put(newPos, getBlock(pos));

                        // Don't want to replace the old block if it's in
                        // the new area
                        if (x >= newMin.getBlockX() && x <= newMax.getBlockX()
                                && y >= newMin.getBlockY() && y <= newMax.getBlockY()
                                && z >= newMin.getBlockZ() && z <= newMax.getBlockZ()) {
                        } else {
                            setBlock(pos, replace);
                        }
                    }
                }
            }
        }

        for (Map.Entry<Vector,BaseBlock> entry : delayed.entrySet()) {
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
    public int drainArea(Vector pos, int radius) throws MaxChangedBlocksException {
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

            int type = getBlock(cur).getID();

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
     * @return number of blocks affected
     * @throws MaxChangedBlocksException
     */
    public int fixWater(Vector pos, int radius) throws MaxChangedBlocksException {
        int affected = 0;

        HashSet<BlockVector> visited = new HashSet<BlockVector>();
        Stack<BlockVector> queue = new Stack<BlockVector>();

        for (int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; x++) {
            for (int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; z++) {
                for (int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; y++) {
                    int type = getBlock(new Vector(x, y, z)).getID();

                    // Check block type
                    if (type == 8 || type == 9) {
                        queue.push(new BlockVector(x, y, z));
                    }
                }
            }
        }

        BaseBlock stationaryWater = new BaseBlock(9);

        while (!queue.empty()) {
            BlockVector cur = queue.pop();

            int type = getBlock(cur).getID();

            // Check block type
            if (type != 8 && type != 9 && type != 0) {
                continue;
            }

            // Don't want to revisit
            if (visited.contains(cur)) {
                continue;
            }

            visited.add(cur);

            if (setBlock(cur, stationaryWater)) {
                affected++;
            }

            // Check radius
            if (pos.distance(cur) > radius) {
                continue;
            }

            for (int x = cur.getBlockX() - 1; x <= cur.getBlockX() + 1; x++) {
                for (int z = cur.getBlockZ() - 1; z <= cur.getBlockZ() + 1; z++) {
                    BlockVector newPos = new BlockVector(x, cur.getBlockY(), z);

                    if (!cur.equals(newPos)) {
                        queue.push(newPos);
                    }
                }
            }
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
    private int makeHCylinderPoints(Vector center, int x, int z,
            int height, BaseBlock block) throws MaxChangedBlocksException {
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
    public int makeHollowCylinder(Vector pos, BaseBlock block,
            int radius, int height) throws MaxChangedBlocksException {
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
    private int makeCylinderPoints(Vector center, int x, int z,
            int height, BaseBlock block) throws MaxChangedBlocksException {
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
    public int makeCylinder(Vector pos, BaseBlock block,
            int radius, int height) throws MaxChangedBlocksException {
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
    public int makeSphere(Vector pos, BaseBlock block, int radius, boolean filled)
            throws MaxChangedBlocksException {
        int affected = 0;
        
        for (int x = 0; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = 0; z <= radius; z++) {
                    Vector vec = pos.add(x, y, z);
                    double d = vec.distance(pos);

                    if (d <= radius + 0.5 && (filled || d >= radius - 0.5)) {
                        if (setBlock(vec, block)) { affected++; }
                        if (setBlock(pos.add(-x, y, z), block)) { affected++; }
                        if (setBlock(pos.add(x, -y, z), block)) { affected++; }
                        if (setBlock(pos.add(x, y, -z), block)) { affected++; }
                        if (setBlock(pos.add(-x, -y, z), block)) { affected++; }
                        if (setBlock(pos.add(x, -y, -z), block)) { affected++; }
                        if (setBlock(pos.add(-x, y, -z), block)) { affected++; }
                        if (setBlock(pos.add(-x, -y, -z), block)) { affected++; }
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
     */
    private boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c)
            throws MaxChangedBlocksException {
        if (Math.random() <= c) {
            return setBlockIfAir(pos, block);
        }
        return false;
    }

    /**
     * Makes a terrible looking pine tree.
     *
     * @param basePos
     */
    private void makePineTree(Vector basePos)
            throws MaxChangedBlocksException {
        int trunkHeight = (int)Math.floor(Math.random() * 2) + 3;
        int height = (int)Math.floor(Math.random() * 5) + 8;

        BaseBlock logBlock = new BaseBlock(17);
        BaseBlock leavesBlock = new BaseBlock(18);

        // Create trunk
        for (int i = 0; i < trunkHeight; i++) {
            if (!setBlockIfAir(basePos.add(0, i, 0), logBlock)) {
                return;
            }
        }

        // Move up
        basePos = basePos.add(0, trunkHeight, 0);

        int pos2[] = {-2, 2};

        // Create tree + leaves
        for (int i = 0; i < height; i++) {
            setBlockIfAir(basePos.add(0, i, 0), logBlock);

            // Less leaves at these levels
            double chance = ((i == 0 || i == height - 1) ? 0.6 : 1);

            // Inner leaves
            setChanceBlockIfAir(basePos.add(-1, i, 0), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(1, i, 0), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(0, i, -1), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(0, i, 1), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(1, i, 1), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(-1, i, 1), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(1, i, -1), leavesBlock, chance);
            setChanceBlockIfAir(basePos.add(-1, i, -1), leavesBlock, chance);

            if (!(i == 0 || i == height - 1)) {
                for (int j = -2; j <= 2; j++) {
                    setChanceBlockIfAir(basePos.add(-2, i, j), leavesBlock, 0.6);
                }
                for (int j = -2; j <= 2; j++) {
                    setChanceBlockIfAir(basePos.add(2, i, j), leavesBlock, 0.6);
                }
                for (int j = -2; j <= 2; j++) {
                    setChanceBlockIfAir(basePos.add(j, i, -2), leavesBlock, 0.6);
                }
                for (int j = -2; j <= 2; j++) {
                    setChanceBlockIfAir(basePos.add(j, i, 2), leavesBlock, 0.6);
                }
            }
        }

        setBlockIfAir(basePos.add(0, height, 0), leavesBlock);
    }

    /**
     * Makes a pumpkin patch.
     *
     * @param basePos
     */
    private void makePumpkinPatch(Vector basePos)
            throws MaxChangedBlocksException {
        BaseBlock logBlock = new BaseBlock(17);
        BaseBlock leavesBlock = new BaseBlock(18);

        setBlock(basePos.subtract(0, 1, 0), logBlock);
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
        if (getBlock(pos).getID() != 0) return;

        for (int i = -1; i > -3; i--) {
            Vector testPos = pos.add(0, i, 0);
            if (getBlock(testPos).getID() == 0) {
                pos = testPos;
            } else {
                break;
            }
        }

        setBlockIfAir(pos, new BaseBlock(18));

        int t = prng.nextInt(4);
        int h = prng.nextInt(3) - 1;

        if (t == 0) {
            if (prng.nextBoolean()) makePumpkinPatchVine(basePos, pos.add(1, 0, 0));
            if (prng.nextBoolean()) setBlockIfAir(pos.add(1, h, -1), new BaseBlock(18));
            setBlockIfAir(pos.add(0, 0, -1), new BaseBlock(86));
        } else if (t == 1) {
            if (prng.nextBoolean()) makePumpkinPatchVine(basePos, pos.add(0, 0, 1));
            if (prng.nextBoolean()) setBlockIfAir(pos.add(1, h, 0), new BaseBlock(18));
            setBlockIfAir(pos.add(1, 0, 1), new BaseBlock(86));
        } else if (t == 2) {
            if (prng.nextBoolean()) makePumpkinPatchVine(basePos, pos.add(0, 0, -1));
            if (prng.nextBoolean()) setBlockIfAir(pos.add(-1, h, 0), new BaseBlock(18));
            setBlockIfAir(pos.add(-1, 0, 1), new BaseBlock(86));
        } else if (t == 3) {
            if (prng.nextBoolean()) makePumpkinPatchVine(basePos, pos.add(-1, 0, 0));
            if (prng.nextBoolean()) setBlockIfAir(pos.add(-1, h, -1), new BaseBlock(18));
            setBlockIfAir(pos.add(-1, 0, -1), new BaseBlock(86));
        }
    }

    /**
     * Makes pumpkin patches.
     *
     * @param basePos
     * @param size
     * @return number of trees created
     */
    public int makePumpkinPatches(Vector basePos, int size)
            throws MaxChangedBlocksException {
        int affected = 0;

        for (int x = basePos.getBlockX() - size; x <= basePos.getBlockX() + size; x++) {
            for (int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ() + size; z++) {
                // Don't want to be in the ground
                if (!getBlock(new Vector(x, basePos.getBlockY(), z)).isAir())
                    continue;
                // The gods don't want a pumpkin patch here
                if (Math.random() < 0.98) { continue; }

                for (int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; y--) {
                    // Check if we hit the ground
                    int t = getBlock(new Vector(x, y, z)).getID();
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
     * Makes a terrible looking pine forest.
     * 
     * @param basePos
     * @param size
     * @return number of trees created
     */
    public int makePineTreeForest(Vector basePos, int size)
            throws MaxChangedBlocksException {
        int affected = 0;
        
        for (int x = basePos.getBlockX() - size; x <= basePos.getBlockX() + size; x++) {
            for (int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ() + size; z++) {
                // Don't want to be in the ground
                if (!getBlock(new Vector(x, basePos.getBlockY(), z)).isAir())
                    continue;
                // The gods don't want a tree here
                if (Math.random() < 0.95) { continue; }

                for (int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; y--) {
                    // Check if we hit the ground
                    int t = getBlock(new Vector(x, y, z)).getID();
                    if (t == 2 || t == 3) {
                        makePineTree(new Vector(x, y + 1, z));
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
}
