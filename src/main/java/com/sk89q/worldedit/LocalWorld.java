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

package com.sk89q.worldedit;

import java.util.Random;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.regions.Region;

/**
 * Represents a world.
 * 
 * @author sk89q
 */
public abstract class LocalWorld {
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
    public abstract boolean generateRedwoodTree(EditSession editSession,
            Vector pt) throws MaxChangedBlocksException;

    /**
     * Generate a tall redwood tree at a location.
     * 
     * @param editSession 
     * @param pt
     * @return
     * @throws MaxChangedBlocksException 
     */
    public abstract boolean generateTallRedwoodTree(EditSession editSession,
            Vector pt) throws MaxChangedBlocksException;

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
        int type = getBlockType(pt);
        //setBlockType(pt, 0);

        if (type == BlockID.STONE) { dropItem(pt, new BaseItemStack(BlockID.COBBLESTONE)); } // Stone
        else if (type == BlockID.GRASS) { dropItem(pt, new BaseItemStack(BlockID.DIRT)); } // Grass
        else if (type == BlockID.BEDROCK) { } // Bedrock
        else if (type == BlockID.WATER) { } // Water
        else if (type == BlockID.STATIONARY_WATER) { } // Water
        else if (type == BlockID.LAVA) { } // Lava
        else if (type == BlockID.STATIONARY_LAVA) { } // Lava
        else if (type == BlockID.GRAVEL) { // Gravel
            if (random.nextDouble() >= 0.9) {
                dropItem(pt, new BaseItemStack(ItemType.FLINT.getID()));
            } else {
                dropItem(pt, new BaseItemStack(type));
            }
        }
        else if (type == BlockID.COAL_ORE) { dropItem(pt, new BaseItemStack(ItemType.COAL.getID())); } // Coal ore
        else if (type == BlockID.LOG) { dropItem(pt, new BaseItemStack(type, 1, (short) getBlockData(pt))); } // Log
        else if (type == BlockID.LEAVES) { // Leaves
            if (random.nextDouble() > 0.95) {
                dropItem(pt, new BaseItemStack(BlockID.SAPLING, 1, (short) getBlockData(pt)));
            }
        }
        else if (type == BlockID.GLASS) { } // Glass
        else if (type == BlockID.LAPIS_LAZULI_ORE) {
            dropItem(pt, new BaseItemStack(ItemType.INK_SACK.getID(), 1, (short) 4), (random.nextInt(5) + 4));
        } // Lapis Lazuli ore
        else if (type == BlockID.BED) { dropItem(pt, new BaseItemStack(ItemType.BED_ITEM.getID())); } // Bed
        else if (type == BlockID.LONG_GRASS) { if (random.nextInt(8) == 0) dropItem(pt, new BaseItemStack(ItemType.SEEDS.getID())); } // Tall Grass
        else if (type == BlockID.PISTON_EXTENSION) { } // Piston Head
        else if (type == BlockID.CLOTH) { dropItem(pt, new BaseItemStack(type, 1, (short) getBlockData(pt))); } // Cloth
        else if (type == BlockID.DOUBLE_STEP) { // Double step
            dropItem(pt, new BaseItemStack(BlockID.STEP, 1, (short) getBlockData(pt)), 2);
        }
        else if (type == BlockID.STEP) { dropItem(pt, new BaseItemStack(type, 1, (short) getBlockData(pt))); } // Step 
        else if (type == BlockID.BOOKCASE) { } // Bookshelves
        else if (type == BlockID.FIRE) { } // Fire
        else if (type == BlockID.MOB_SPAWNER) { } // Mob spawner
        else if (type == BlockID.WOODEN_STAIRS) { dropItem(pt, new BaseItemStack(BlockID.WOOD)); } // Wooden stairs
        else if (type == BlockID.REDSTONE_WIRE) { dropItem(pt, new BaseItemStack(ItemType.REDSTONE_DUST.getID())); } // Redstone wire
        else if (type == BlockID.DIAMOND_ORE) { dropItem(pt, new BaseItemStack(ItemType.DIAMOND.getID())); } // Diamond ore
        else if (type == BlockID.CROPS) { dropItem(pt, new BaseItemStack(ItemType.SEEDS.getID())); } // Crops
        else if (type == BlockID.SOIL) { dropItem(pt, new BaseItemStack(BlockID.DIRT)); } // Soil
        else if (type == BlockID.BURNING_FURNACE) { dropItem(pt, new BaseItemStack(BlockID.FURNACE)); } // Furnace
        else if (type == BlockID.SIGN_POST) { dropItem(pt, new BaseItemStack(ItemType.SIGN.getID())); } // Sign post
        else if (type == BlockID.WOODEN_DOOR) { dropItem(pt, new BaseItemStack(ItemType.WOODEN_DOOR_ITEM.getID())); } // Wood door
        else if (type == BlockID.COBBLESTONE_STAIRS) { dropItem(pt, new BaseItemStack(BlockID.COBBLESTONE)); } // Cobblestone stairs
        else if (type == BlockID.WALL_SIGN) { dropItem(pt, new BaseItemStack(ItemType.SIGN.getID())); } // Wall sign
        else if (type == BlockID.IRON_DOOR) { dropItem(pt, new BaseItemStack(ItemType.IRON_DOOR_ITEM.getID())); } // Iron door
        else if (type == BlockID.REDSTONE_ORE) { dropItem(pt, new BaseItemStack(ItemType.REDSTONE_DUST.getID()), (random.nextInt(2) + 4)); } // Redstone ore
        else if (type == BlockID.GLOWING_REDSTONE_ORE) { dropItem(pt, new BaseItemStack(ItemType.REDSTONE_DUST.getID()), (random.nextInt(2) + 4)); } // Glowing redstone ore
        else if (type == BlockID.REDSTONE_TORCH_OFF) { dropItem(pt, new BaseItemStack(BlockID.REDSTONE_TORCH_ON)); } // Redstone torch
        else if (type == BlockID.SNOW) { } // Snow
        else if (type == BlockID.ICE) { } // Ice
        else if (type == BlockID.CLAY) { dropItem(pt, new BaseItemStack(ItemType.CLAY_BALL.getID()), 4); } // Clay
        else if (type == BlockID.REED) { dropItem(pt, new BaseItemStack(ItemType.SUGAR_CANE_ITEM.getID())); } // Reed
        else if (type == BlockID.LIGHTSTONE) { dropItem(pt, new BaseItemStack(ItemType.LIGHTSTONE_DUST.getID()), (random.nextInt(3) + 2)); } // Lightstone
        else if (type == BlockID.PORTAL) { } // Portal
        else if (type == BlockID.REDSTONE_REPEATER_OFF) { dropItem(pt, new BaseItemStack(ItemType.REDSTONE_REPEATER.getID())); } // Repeater
        else if (type == BlockID.REDSTONE_REPEATER_ON) { dropItem(pt, new BaseItemStack(ItemType.REDSTONE_REPEATER.getID())); } // Repeater
        else if (type != BlockID.AIR) {
            dropItem(pt, new BaseItemStack(type));
        }
    }

    /**
     * Kill mobs in an area, excluding pet wolves.
     * 
     * @param origin
     * @param radius
     * @return
     */
    public abstract int killMobs(Vector origin, int radius);

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius
     * @param killPets
     * @return
     */
    public abstract int killMobs(Vector origin, int radius, boolean killPets);

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
        return type >= 0 && type < 96;
    }

    /**
     * Checks if the chunk pt is in is loaded. if not, loads the chunk
     *
     * @param pt Position to check
     */
    public abstract void checkLoadedChuck(Vector pt);

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
}
