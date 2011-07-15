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

package com.sk89q.worldedit.bukkit;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.regions.Region;

public class BukkitWorld extends LocalWorld {
    private World world;
    
    /**
     * Construct the object.
     * @param world
     */
    public BukkitWorld(World world) {
        this.world = world;
    }
    
    /**
     * Get the world handle.
     * 
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    @Override
    public boolean setBlockType(Vector pt, int type) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type);
    }

    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    @Override
    public boolean setBlockTypeFast(Vector pt, int type) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type, false);
    }

    /**
     * set block type & data
     * @param pt
     * @param type
     * @param data
     * @return 
     */
    @Override
    public boolean setTypeIdAndData(Vector pt, int type, int data){
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeIdAndData(type, (byte) data, true);
    }
    
    /**
     * set block type & data
     * @param pt
     * @param type
     * @param data
     * @return 
     */
    @Override
    public boolean setTypeIdAndDataFast(Vector pt, int type, int data){
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeIdAndData(type, (byte) data, false);
    }
    
    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    @Override
    public int getBlockType(Vector pt) {
        return world.getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */
    @Override
    public void setBlockData(Vector pt, int data) {
        world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte)data);
    }

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */
    @Override
    public void setBlockDataFast(Vector pt, int data) {
        world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte)data, false);
    }

    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    @Override
    public int getBlockData(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
    }

    /**
     * Get block light level.
     * 
     * @param pt
     * @return
     */
    @Override
    public int getBlockLightLevel(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getLightLevel();
    }

    /**
     * Regenerate an area.
     * 
     * @param region
     * @param editSession
     * @return
     */
    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        BaseBlock[] history = new BaseBlock[16 * 16 * 128];
        
        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
            
            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 128; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = editSession.getBlock(pt);
                    }
                }
            }
            
            try {
                world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
            // Then restore 
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 128; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        
                        // We have to restore the block if it was outside
                        if (!region.contains(pt)) {
                            editSession.smartSetBlock(pt, history[index]);
                        } else { // Otherwise fool with history
                            editSession.rememberChange(pt, history[index],
                                    editSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * Attempts to accurately copy a BaseBlock's extra data to the world.
     * 
     * @param pt
     * @param block
     * @return
     */
    @Override
    public boolean copyToWorld(Vector pt, BaseBlock block) {
        // Signs
        if (block instanceof SignBlock) {
            setSignText(pt, ((SignBlock)block).getText());
            return true;
        
        // Furnaces
        } else if (block instanceof FurnaceBlock) {
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) return false;
            Furnace bukkit = (Furnace)state;
            FurnaceBlock we = (FurnaceBlock)block;
            bukkit.setBurnTime(we.getBurnTime());
            bukkit.setCookTime(we.getCookTime());
            return setContainerBlockContents(pt, ((ContainerBlock)block).getItems());
            
        // Chests/dispenser
        } else if (block instanceof ContainerBlock) {
            return setContainerBlockContents(pt, ((ContainerBlock)block).getItems());
        
        // Mob spawners
        } else if (block instanceof MobSpawnerBlock) {
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) return false;
            CreatureSpawner bukkit = (CreatureSpawner)state;
            MobSpawnerBlock we = (MobSpawnerBlock)block;
            bukkit.setCreatureTypeId(we.getMobType());
            bukkit.setDelay(we.getDelay());
            return true;
        
        // Note block
        } else if (block instanceof NoteBlock) {
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) return false;
            org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock)state;
            NoteBlock we = (NoteBlock)block;
            bukkit.setNote(we.getNote());
            return true;
        }
        
        return false;
    }

    /**
     * Attempts to read a BaseBlock's extra data from the world.
     * 
     * @param pt
     * @param block
     * @return
     */
    @Override
    public boolean copyFromWorld(Vector pt, BaseBlock block) {
        // Signs
        if (block instanceof SignBlock) {
            ((SignBlock)block).setText(getSignText(pt));
            return true;
        
        // Furnaces
        } else if (block instanceof FurnaceBlock) {
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) return false;
            Furnace bukkit = (Furnace)state;
            FurnaceBlock we = (FurnaceBlock)block;
            we.setBurnTime(bukkit.getBurnTime());
            we.setCookTime(bukkit.getCookTime());
            ((ContainerBlock)block).setItems(getContainerBlockContents(pt));
            return true;

        // Chests/dispenser
        } else if (block instanceof ContainerBlock) {
            ((ContainerBlock)block).setItems(getContainerBlockContents(pt));
            return true;
        
        // Mob spawners
        } else if (block instanceof MobSpawnerBlock) {
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) return false;
            CreatureSpawner bukkit = (CreatureSpawner)state;
            MobSpawnerBlock we = (MobSpawnerBlock)block;
            we.setMobType(bukkit.getCreatureTypeId());
            we.setDelay((short)bukkit.getDelay());
            return true;
        
        // Note block
        } else if (block instanceof NoteBlock) {
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) return false;
            org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock)state;
            NoteBlock we = (NoteBlock)block;
            we.setNote(bukkit.getNote());
        }
        
        return false;
    }

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    @Override
    public boolean clearContainerBlockContents(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return false;
        }

        org.bukkit.block.ContainerBlock chest = (org.bukkit.block.ContainerBlock)state;
        Inventory inven = chest.getInventory();
        inven.clear();
        return true;
    }

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    @Override
    public boolean generateTree(EditSession editSession, Vector pt) {
        return world.generateTree(BukkitUtil.toLocation(world, pt), TreeType.TREE,
                new EditSessionBlockChangeDelegate(editSession));
    }

    /**
     * Generate a big tree at a location.
     * 
     * @param pt
     * @return
     */
    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        return world.generateTree(BukkitUtil.toLocation(world, pt), TreeType.BIG_TREE,
                new EditSessionBlockChangeDelegate(editSession));
    }

    /**
     * Generate a birch tree at a location.
     * 
     * @param pt
     * @return
     */
    @Override
    public boolean generateBirchTree(EditSession editSession, Vector pt) {
        return world.generateTree(BukkitUtil.toLocation(world, pt), TreeType.BIRCH,
                new EditSessionBlockChangeDelegate(editSession));
    }

    /**
     * Generate a redwood tree at a location.
     * 
     * @param pt
     * @return
     */
    @Override
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) {
        return world.generateTree(BukkitUtil.toLocation(world, pt), TreeType.REDWOOD,
                new EditSessionBlockChangeDelegate(editSession));
    }

    /**
     * Generate a redwood tree at a location.
     * 
     * @param pt
     * @return
     */
    @Override
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) {
        return world.generateTree(BukkitUtil.toLocation(world, pt), TreeType.TALL_REDWOOD,
                new EditSessionBlockChangeDelegate(editSession));
    }

    /**
     * Drop an item.
     *
     * @param pt
     * @param item
     */
    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        ItemStack bukkitItem = new ItemStack(item.getType(), item.getAmount(),
                (byte)item.getDamage());
        world.dropItemNaturally(toLocation(pt), bukkitItem);
        
    }

    /**
     * Kill mobs in an area, excluding tamed wolves.
     * 
     * @param origin
     * @param radius -1 for all mobs
     * @return
     */
    @Override
    public int killMobs(Vector origin, int radius) {
        return killMobs(origin, radius, false);
    }

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius -1 for all mobs
     * @param killPets true to kill tames wolves
     * @return
     */
    @Override
    public int killMobs(Vector origin, int radius, boolean killPets) {
        int num = 0;
        double radiusSq = Math.pow(radius, 2);
        
        for (LivingEntity ent : world.getLivingEntities()) {
            if (!killPets && ent instanceof Wolf && ((Wolf) ent).isTamed()) {
                continue; // tamed wolf
            }
            if (ent instanceof Creature || ent instanceof Ghast || ent instanceof Slime) {
                if (radius == -1
                        || origin.distanceSq(BukkitUtil.toVector(ent.getLocation())) <= radiusSq) {
                    ent.remove();
                    ++num;
                }
            }
        }
        
        return num;
    }
    
    /**
     * Remove entities in an area.
     * 
     * @param origin
     * @param radius
     * @return
     */
    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        int num = 0;
        double radiusSq = Math.pow(radius, 2);
        
        for (Entity ent : world.getEntities()) {
            if (radius != -1
                    && origin.distanceSq(BukkitUtil.toVector(ent.getLocation())) > radiusSq) {
                continue;
            }
            
            if (type == EntityType.ARROWS) {
                if (ent instanceof Arrow) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.BOATS) {
                if (ent instanceof Boat) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.ITEMS) {
                if (ent instanceof Item) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.MINECARTS) {
                if (ent instanceof Minecart) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.PAINTINGS) {
                if (ent instanceof Painting) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.TNT) {
                if (ent instanceof TNTPrimed) {
                    ent.remove();
                    ++num;
                }
            }
        }
        
        return num;
    }
    
    private Location toLocation(Vector pt) {
        return new Location(world, pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Set a sign's text.
     * 
     * @param pt
     * @param text
     * @return
     */
    private boolean setSignText(Vector pt, String[] text) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return false;
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return false;
        Sign sign = (Sign)state;
        sign.setLine(0, text[0]);
        sign.setLine(1, text[1]);
        sign.setLine(2, text[2]);
        sign.setLine(3, text[3]);
        sign.update();
        return true;
    }

    /**
     * Get a sign's text.
     * 
     * @param pt
     * @return
     */
    private String[] getSignText(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return new String[] { "", "", "", "" };
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return new String[] { "", "", "", "" };
        Sign sign = (Sign)state;
        String line0 = sign.getLine(0);
        String line1 = sign.getLine(1);
        String line2 = sign.getLine(2);
        String line3 = sign.getLine(3);
        return new String[] {
                line0 != null ? line0 : "",
                line1 != null ? line1 : "",
                line2 != null ? line2 : "",
                line3 != null ? line3 : "",
            };
    }

    /**
     * Get a container block's contents.
     * 
     * @param pt
     * @return
     */
    private BaseItemStack[] getContainerBlockContents(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return new BaseItemStack[0];
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return new BaseItemStack[0];
        }
        
        org.bukkit.block.ContainerBlock container = (org.bukkit.block.ContainerBlock)state;
        Inventory inven = container.getInventory();
        int size = inven.getSize();
        BaseItemStack[] contents = new BaseItemStack[size];
        
        for (int i = 0; i < size; ++i) {
            ItemStack bukkitStack = inven.getItem(i);
            if (bukkitStack.getTypeId() > 0) {
                contents[i] = new BaseItemStack(
                        bukkitStack.getTypeId(),
                        bukkitStack.getAmount(), 
                        bukkitStack.getDurability());
            }
        }
        
        return contents;
    }

    /**
     * Set a container block's contents.
     * 
     * @param pt
     * @param contents
     * @return
     */
    private boolean setContainerBlockContents(Vector pt, BaseItemStack[] contents) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return false;
        }
        
        org.bukkit.block.ContainerBlock chest = (org.bukkit.block.ContainerBlock)state;
        Inventory inven = chest.getInventory();
        int size = inven.getSize();
        
        for (int i = 0; i < size; ++i) {
            if (i >= contents.length) {
                break;
            }

            if (contents[i] != null) {
                inven.setItem(i, new ItemStack(contents[i].getType(),
                        contents[i].getAmount(), 
                        (byte)contents[i].getDamage()));
            } else {
                inven.setItem(i, null);
            }
        }
        
        return true;
    }
    
    /**
     * Returns whether a block has a valid ID.
     * 
     * @param type
     * @return
     */
    @Override
    public boolean isValidBlockType(int type) {
        return Material.getMaterial(type) != null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BukkitWorld)) {
            return false;
        }
        
        return ((BukkitWorld)other).world.equals(world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }
}
