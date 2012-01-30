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

package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.regions.Region;

import org.spout.api.geo.World;
import org.spout.api.material.MaterialData;

public class SpoutWorld extends LocalWorld {
    private World world;

    /**
     * Construct the object.
     * @param world
     */
    public SpoutWorld(World world) {
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
     * Get the name of the world
     * 
     * @return
     */
    @Override
    public String getName() {
        return world.getName();
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
        return world.setBlockId(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), (short)type, WorldEditPlugin.getInstance());
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
        return setBlockType(pt, type);
    }

    /**
     * set block type & data
     * @param pt
     * @param type
     * @param data
     * @return 
     */
    @Override
    public boolean setTypeIdAndData(Vector pt, int type, int data) {
        return world.setBlockIdAndData(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), (short) type, (short) data, WorldEditPlugin.getInstance());
    }

    /**
     * set block type & data
     * Everything is threaded, so no need for fastmode here.
     * @param pt
     * @param type
     * @param data
     * @return 
     */
    @Override
    public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
        return setTypeIdAndData(pt, type, data);
    }

    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    @Override
    public int getBlockType(Vector pt) {
        return world.getBlockId(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */
    @Override
    public void setBlockData(Vector pt, int data) {
        setTypeIdAndData(pt, world.getBlockId(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()), data);
    }

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */
    @Override
    public void setBlockDataFast(Vector pt, int data) {
        setBlockData(pt, data);
    }

    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    @Override
    public int getBlockData(Vector pt) {
        return world.getBlockData(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Get block light level.
     * 
     * @param pt
     * @return
     */
    @Override
    public int getBlockLightLevel(Vector pt) {
        return world.getBlockMaterial(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getLightLevel();
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
        /*BaseBlock[] history = new BaseBlock[16 * 16 * (getMaxY() + 1)];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (getMaxY() + 1); ++y) {
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
                for (int y = 0; y < (getMaxY() + 1); ++y) {
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

        return true;*/
        return false;
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
        /*if (block instanceof SignBlock) {
            // Signs
            setSignText(pt, ((SignBlock) block).getText());
            return true;
        }

        if (block instanceof FurnaceBlock) {
            // Furnaces
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) return false;
            Furnace bukkit = (Furnace) state;
            FurnaceBlock we = (FurnaceBlock) block;
            bukkit.setBurnTime(we.getBurnTime());
            bukkit.setCookTime(we.getCookTime());
            return setContainerBlockContents(pt, ((ContainerBlock) block).getItems());
        }

        if (block instanceof ContainerBlock) {
            // Chests/dispenser
            return setContainerBlockContents(pt, ((ContainerBlock) block).getItems());
        }

        if (block instanceof MobSpawnerBlock) {
            // Mob spawners
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) return false;
            CreatureSpawner bukkit = (CreatureSpawner) state;
            MobSpawnerBlock we = (MobSpawnerBlock) block;
            bukkit.setCreatureTypeId(we.getMobType());
            bukkit.setDelay(we.getDelay());
            return true;
        }

        if (block instanceof NoteBlock) {
            // Note block
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) return false;
            org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock) state;
            NoteBlock we = (NoteBlock) block;
            bukkit.setRawNote(we.getNote());
            return true;
        }*/

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
        /*if (block instanceof SignBlock) {
            // Signs
            ((SignBlock) block).setText(getSignText(pt));
            return true;
        }

        if (block instanceof FurnaceBlock) {
            // Furnaces
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) return false;
            Furnace bukkit = (Furnace) state;
            FurnaceBlock we = (FurnaceBlock) block;
            we.setBurnTime(bukkit.getBurnTime());
            we.setCookTime(bukkit.getCookTime());
            ((ContainerBlock) block).setItems(getContainerBlockContents(pt));
            return true;
        }

        if (block instanceof ContainerBlock) {
            // Chests/dispenser
            ((ContainerBlock) block).setItems(getContainerBlockContents(pt));
            return true;
        }

        if (block instanceof MobSpawnerBlock) {
            // Mob spawners
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) return false;
            CreatureSpawner bukkit = (CreatureSpawner) state;
            MobSpawnerBlock we = (MobSpawnerBlock) block;
            we.setMobType(bukkit.getCreatureTypeId());
            we.setDelay((short) bukkit.getDelay());
            return true;
        }

        if (block instanceof NoteBlock) {
            // Note block
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) return false;
            org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock) state;
            NoteBlock we = (NoteBlock) block;
            we.setNote(bukkit.getRawNote());
        }*/

        return false;
    }

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    @Override
    public boolean clearContainerBlockContents(Vector pt) {
       /* Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return false;
        }

        org.bukkit.block.ContainerBlock chest = (org.bukkit.block.ContainerBlock) state;
        Inventory inven = chest.getInventory();
        inven.clear();
        return true;*/
        return false;
    }

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    @Override
    public boolean generateTree(EditSession editSession, Vector pt) {
        /*return world.generateTree(SpoutUtil.toLocation(world, pt), TreeType.TREE,
                new EditSessionBlockChangeDelegate(editSession));*/
        return false;
    }

    /**
     * Generate a big tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        /*return world.generateTree(SpoutUtil.toLocation(world, pt), TreeType.BIG_TREE,
                new EditSessionBlockChangeDelegate(editSession));*/
        return false;
    }

    /**
     * Generate a birch tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    public boolean generateBirchTree(EditSession editSession, Vector pt) {
        /*return world.generateTree(SpoutUtil.toLocation(world, pt), TreeType.BIRCH,
                new EditSessionBlockChangeDelegate(editSession));*/
        return false;
    }

    /**
     * Generate a redwood tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) {
        /*return world.generateTree(SpoutUtil.toLocation(world, pt), TreeType.REDWOOD,
                new EditSessionBlockChangeDelegate(editSession));*/
        return false;
    }

    /**
     * Generate a redwood tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) {
        /*return world.generateTree(SpoutUtil.toLocation(world, pt), TreeType.TALL_REDWOOD,
                new EditSessionBlockChangeDelegate(editSession));*/
        return false;
    }

    /**
     * Drop an item.
     *
     * @param pt
     * @param item
     */
    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        /*ItemStack bukkitItem = new ItemStack(item.getType(), item.getAmount(),
                (byte) item.getDamage());
        world.dropItemNaturally(SpoutUtil.toLocation(world, pt), bukkitItem);*/

    }

    /**
     * Kill mobs in an area.
     *
     * @param origin The center of the area to kill mobs in.
     * @param radius Maximum distance to kill mobs at; radius < 0 means kill all mobs
     * @param flags various flags that determine what to kill
     * @return
     */
    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        /*boolean killPets = (flags & KillFlags.PETS) != 0;
        boolean killNPCs = (flags & KillFlags.NPCS) != 0;
        boolean killAnimals = (flags & KillFlags.ANIMALS) != 0;*/

        int num = 0;
        /*double radiusSq = radius * radius;

        Point bukkitOrigin = SpoutUtil.toPoint(world, origin);

        
        for (LivingEntity ent : world.getLivingEntities()) {
            if (ent instanceof HumanEntity) {
                continue;
            }

            if (!killAnimals && ent instanceof Animals) {
                continue;
            }

            if (!killPets && ent instanceof Tameable && ((Tameable) ent).isTamed()) {
                continue; // tamed wolf
            }

            try {
                // Temporary solution until org.bukkit.entity.NPC is widely deployed.
                if (!killNPCs && Class.forName("org.bukkit.entity.NPC").isAssignableFrom(ent.getClass())) {
                    continue;
                }
            } catch (ClassNotFoundException e) {}

            if (radius < 0 || bukkitOrigin.distanceSquared(ent.getLocation()) <= radiusSq) {
                ent.remove();
                ++num;
            }
        }*/

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
        /*double radiusSq = radius * radius;

        for (Entity ent : world.getEntities()) {
            if (radius != -1
                    && origin.distanceSq(SpoutUtil.toVector(ent.getTransform().getPosition())) > radiusSq) {
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
            } else if (type == EntityType.XP_ORBS) {
                if (ent instanceof ExperienceOrb) {
                    ent.remove();
                    ++num;
                }
            }
        }*/

        return num;
    }

    /**
     * Set a sign's text.
     * 
     * @param pt
     * @param text
     * @return
     */
    /*private boolean setSignText(Vector pt, String[] text) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return false;
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return false;
        Sign sign = (Sign) state;
        sign.setLine(0, text[0]);
        sign.setLine(1, text[1]);
        sign.setLine(2, text[2]);
        sign.setLine(3, text[3]);
        sign.update();
        return true;
    }*/

    /**
     * Get a sign's text.
     * 
     * @param pt
     * @return
     */
    /*private String[] getSignText(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return new String[] { "", "", "", "" };
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return new String[] { "", "", "", "" };
        Sign sign = (Sign) state;
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
    }*/

    /**
     * Get a container block's contents.
     * 
     * @param pt
     * @return
     */
    /*private BaseItemStack[] getContainerBlockContents(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return new BaseItemStack[0];
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return new BaseItemStack[0];
        }

        org.bukkit.block.ContainerBlock container = (org.bukkit.block.ContainerBlock) state;
        Inventory inven = container.getInventory();
        int size = inven.getContents().length
        BaseItemStack[] contents = new BaseItemStack[size];

        for (int i = 0; i < size; ++i) {
            ItemStack bukkitStack = inven.getItem(i);
            if (bukkitStack.getMaterial() != MaterialData.air) {
                contents[i] = new BaseItemStack(
                        bukkitStack.getMaterial().getRawId(),
                        bukkitStack.getAmount(),
                        bukkitStack.getDamage());
                try {
                    for (Map.Entry<Enchantment, Integer> entry : bukkitStack.getEnchantments().entrySet()) {
                        contents[i].getEnchantments().put(entry.getKey().getId(), entry.getValue());
                    }
                } catch (Throwable ignore) {}
            }
        }

        return contents;
    }*/

    /**
     * Set a container block's contents.
     * 
     * @param pt
     * @param contents
     * @return
     */
    /*private boolean setContainerBlockContents(Vector pt, BaseItemStack[] contents) {
        Block block = world.getBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return false;
        }

        org.bukkit.block.ContainerBlock chest = (org.bukkit.block.ContainerBlock) state;
        Inventory inven = chest.getInventory();
        int size = inven.getSize();

        for (int i = 0; i < size; ++i) {
            if (i >= contents.length) {
                break;
            }

            if (contents[i] != null) {
                ItemStack toAdd = new ItemStack(contents[i].getType(),
                        contents[i].getAmount(),
                        (byte) contents[i].getDamage());
                try {
                    for (Map.Entry<Integer, Integer> entry : contents[i].getEnchantments().entrySet()) {
                        toAdd.addEnchantment(Enchantment.getById(entry.getKey()), entry.getValue());
                    }
                } catch (Throwable ignore) {}
                inven.setItem(i, toAdd);
            } else {
                inven.setItem(i, null);
            }
        }

        return true;
    }*/

    /**
     * Returns whether a block has a valid ID.
     * 
     * @param type
     * @return
     */
    @Override
    public boolean isValidBlockType(int type) {
        return MaterialData.getBlock((short)type) != null;
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
        /*if (!world.isChunkLoaded(pt.getBlockX() >> 4, pt.getBlockZ() >> 4)) {
            world.loadChunk(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
        }*/
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SpoutWorld)) {
            return false;
        }

        return ((SpoutWorld) other).world.equals(world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public int getMaxY() {
        return world.getHeight() - 1;
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        /*for (BlockVector2D chunkPos : chunks) {
            world.refreshChunk(chunkPos.getBlockX(), chunkPos.getBlockZ());
        }*/
    }

    /*private static final Map<Integer, Effect> effects = new HashMap<Integer, Effect>();
    static {
        for (Effect effect : Effect.values()) {
            effects.put(effect.getId(), effect);
        }
    }*/

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        /*final Effect effect = effects.get(type);
        if (effect == null) {
            return false;
        }

        world.playEffect(SpoutUtil.toLocation(world, position), effect, data);

        return true;
        */
        return false;
    }
}
