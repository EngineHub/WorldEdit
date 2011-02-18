// $Id$
/*
 * CraftBook
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

package com.sk89q.worldedit.bags;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.*;

/**
 * Represents a source to get blocks from and store removed ones.
 *
 * @author sk89q
 */
public abstract class BlockBag {
    /**
     * Stores a block as if it was mined.
     * 
     * @param id
     * @throws BlockBagException
     */
    public void storeDroppedBlock(int id) throws BlockBagException {
        int dropped = BlockType.getDroppedBlock(id);
        if (dropped > 0) {
            storeBlock(dropped);
        }
    }
    
    /**
     * Sets a block as if it was placed by hand.
     *
     * @param id
     * @throws BlockBagException
     */
    public void fetchPlacedBlock(int id) throws BlockBagException {
        try {
            // Blocks that can't be fetched...
            if (id == BlockID.BEDROCK
                    || id == BlockID.GOLD_ORE
                    || id == BlockID.IRON_ORE
                    || id == BlockID.COAL_ORE
                    || id == BlockID.DIAMOND_ORE
                    || id == BlockID.LEAVES
                    || id == BlockID.TNT
                    || id == BlockID.MOB_SPAWNER
                    || id == BlockID.CROPS
                    || id == BlockID.REDSTONE_ORE
                    || id == BlockID.GLOWING_REDSTONE_ORE
                    || id == BlockID.SNOW
                    || id == BlockID.LIGHTSTONE
                    || id == BlockID.PORTAL) {
                throw new UnplaceableBlockException();
            }

            // Override liquids
            if (id == BlockID.WATER
                    || id == BlockID.STATIONARY_WATER
                    || id == BlockID.LAVA
                    || id == BlockID.STATIONARY_LAVA) {
                return;
            }

            fetchBlock(id);
        } catch (OutOfBlocksException e) {
            // Look for cobblestone
            if (id == BlockID.STONE) {
                fetchBlock(BlockID.COBBLESTONE);
            // Look for dirt
            } else if (id == BlockID.GRASS) {
                fetchBlock(BlockID.DIRT);
            // Look for redstone dust
            } else if (id == BlockID.REDSTONE_WIRE) {
                fetchBlock(331);
            // Look for furnace
            } else if (id == BlockID.BURNING_FURNACE) {
                fetchBlock(BlockID.FURNACE);
            // Look for lit redstone torch
            } else if (id == BlockID.REDSTONE_TORCH_OFF) {
                fetchBlock(BlockID.REDSTONE_TORCH_ON);
            // Look for signs
            } else if (id == BlockID.WALL_SIGN || id == BlockID.SIGN_POST) {
                fetchBlock(323);
            } else {
                throw e;
            }
        }
    }

    /**
     * Get a block.
     *
     * @param id
     * @throws BlockBagException 
     */
    public abstract void fetchBlock(int id) throws BlockBagException;
    
    /**
     * Store a block.
     * 
     * @param id
     * @throws BlockBagException 
     */
    public abstract void storeBlock(int id) throws BlockBagException;
    
    /**
     * Checks to see if a block exists without removing it.
     * 
     * @param id
     * @return whether the block exists
     */
    public boolean peekBlock(int id) {
        try {
            fetchBlock(id);
            storeBlock(id);
            return true;
        } catch (BlockBagException e) {
            return false;
        }
    }
    
    /**
     * Flush any changes. This is called at the end.
     */
    public abstract void flushChanges();

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     */
    public abstract void addSourcePosition(Vector pos);
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     */
    public abstract void addSingleSourcePosition(Vector pos);
}
