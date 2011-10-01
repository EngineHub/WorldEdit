// $Id$
/*
 * WorldEdit
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
            switch (id) {
            case BlockID.BEDROCK:
            case BlockID.GOLD_ORE:
            case BlockID.IRON_ORE:
            case BlockID.COAL_ORE:
            case BlockID.DIAMOND_ORE:
            case BlockID.LEAVES:
            case BlockID.TNT:
            case BlockID.MOB_SPAWNER:
            case BlockID.CROPS:
            case BlockID.REDSTONE_ORE:
            case BlockID.GLOWING_REDSTONE_ORE:
            case BlockID.SNOW:
            case BlockID.LIGHTSTONE:
            case BlockID.PORTAL:
                throw new UnplaceableBlockException();

            case BlockID.WATER:
            case BlockID.STATIONARY_WATER:
            case BlockID.LAVA:
            case BlockID.STATIONARY_LAVA:
                // Override liquids
                return;

            default:
                fetchBlock(id);
                break;
            }

        } catch (OutOfBlocksException e) {
            switch (id) {
            case BlockID.STONE:
                fetchBlock(BlockID.COBBLESTONE);
                break;

            case BlockID.GRASS:
                fetchBlock(BlockID.DIRT);
                break;

            case BlockID.REDSTONE_WIRE:
                fetchBlock(ItemID.REDSTONE_DUST);
                break;

            case BlockID.REDSTONE_TORCH_OFF:
                fetchBlock(BlockID.REDSTONE_TORCH_ON);
                break;

            case BlockID.WALL_SIGN:
            case BlockID.SIGN_POST:
                fetchBlock(ItemID.SIGN);
                break;

            default:
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
