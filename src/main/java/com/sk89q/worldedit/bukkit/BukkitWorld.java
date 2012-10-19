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

package com.sk89q.worldedit.bukkit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ContainerBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.bukkit.entity.BukkitEntity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;

public class BukkitWorld extends LocalWorld {

    private static final Logger logger = Logger.getLogger(BukkitWorld.class.getCanonicalName());
    private World world;
    private boolean skipNmsAccess = false;
    private boolean skipNmsSafeSet = false;
    private boolean skipNmsValidBlockCheck = false;

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
    public boolean setTypeIdAndData(Vector pt, int type, int data) {
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
    public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
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
        world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte) data);
    }

    /**
     * Set block data.
     *
     * @param pt
     * @param data
     */
    @Override
    public void setBlockDataFast(Vector pt, int data) {
        world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte) data, false);
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
     * Get biome type
     *
     * @param pt
     * @return
     */
    @Override
    public BiomeType getBiome(Vector2D pt) {
        Biome bukkitBiome = world.getBiome(pt.getBlockX(), pt.getBlockZ());
        try {
            return BukkitBiomeType.valueOf(bukkitBiome.name());
        } catch (IllegalArgumentException exc) {
            return BiomeType.UNKNOWN;
        }
    }

    @Override
    public void setBiome(Vector2D pt, BiomeType biome) {
        if (biome instanceof BukkitBiomeType) {
            Biome bukkitBiome;
            bukkitBiome = ((BukkitBiomeType) biome).getBukkitBiome();
            world.setBiome(pt.getBlockX(), pt.getBlockZ(), bukkitBiome);
        }
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
        BaseBlock[] history = new BaseBlock[16 * 16 * (getMaxY() + 1)];

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
        if (block instanceof SignBlock) {
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
            bukkit.setCreatureTypeByName(we.getMobType());
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
        }

        if (!skipNmsAccess) {
            try {
                return NmsBlock.set(world, pt, block);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "WorldEdit: Failed to do NMS access for direct NBT data copy", t);
                skipNmsAccess = true;
            }
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
        if (block instanceof SignBlock) {
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
            we.setMobType(bukkit.getCreatureTypeName());
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
            return true;
        }

        return false;
    }

    /**
     * Gets the single block inventory for a potentially double chest.
     * Handles people who have an old version of Bukkit.
     * This should be replaced with {@link org.bukkit.block.Chest#getBlockInventory()}
     * in a few months (now = March 2012)
     *
     * @param chest The chest to get a single block inventory for
     * @return The chest's inventory
     */
    private Inventory getBlockInventory(Chest chest) {
        try {
            return chest.getBlockInventory();
        } catch (Throwable t) {
            if (chest.getInventory() instanceof DoubleChestInventory) {
                DoubleChestInventory inven = (DoubleChestInventory) chest.getInventory();
                if (inven.getLeftSide().getHolder().equals(chest)) {
                    return inven.getLeftSide();
                } else if (inven.getRightSide().getHolder().equals(chest)) {
                    return inven.getRightSide();
                } else {
                    return inven;
                }
            } else {
                return chest.getInventory();
            }
        }
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
        if (!(state instanceof org.bukkit.inventory.InventoryHolder)) {
            return false;
        }

        org.bukkit.inventory.InventoryHolder chest = (org.bukkit.inventory.InventoryHolder) state;
        Inventory inven = chest.getInventory();
        if (chest instanceof Chest) {
            inven = getBlockInventory((Chest) chest);
        }
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
    @Deprecated
    public boolean generateTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.TREE, editSession, pt);
    }

    /**
     * Generate a big tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    @Deprecated
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.BIG_TREE, editSession, pt);
    }

    /**
     * Generate a birch tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    @Deprecated
    public boolean generateBirchTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.BIRCH, editSession, pt);
    }

    /**
     * Generate a redwood tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    @Deprecated
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.REDWOOD, editSession, pt);
    }

    /**
     * Generate a redwood tree at a location.
     *
     * @param pt
     * @return
     */
    @Override
    @Deprecated
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.TALL_REDWOOD, editSession, pt);
    }

    /**
     * An EnumMap that stores which WorldEdit TreeTypes apply to which Bukkit TreeTypes
     */
    private static final EnumMap<TreeGenerator.TreeType, TreeType> treeTypeMapping =
            new EnumMap<TreeGenerator.TreeType, TreeType>(TreeGenerator.TreeType.class);

    static {
        // Mappings for new TreeType values not yet in Bukkit
        treeTypeMapping.put(TreeGenerator.TreeType.SWAMP, TreeType.TREE);
        treeTypeMapping.put(TreeGenerator.TreeType.JUNGLE_BUSH, TreeType.TREE);
        try {
            treeTypeMapping.put(TreeGenerator.TreeType.SHORT_JUNGLE, TreeType.valueOf("SMALL_JUNGLE"));
        } catch (IllegalArgumentException e) {
            treeTypeMapping.put(TreeGenerator.TreeType.SHORT_JUNGLE, TreeType.TREE);
        }
        for (TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
            try {
                TreeType bukkitType = TreeType.valueOf(type.name());
                treeTypeMapping.put(type, bukkitType);
            } catch (IllegalArgumentException e) {
                // Unhandled TreeType
            }
        }
        // Other mappings for WE-specific values
        treeTypeMapping.put(TreeGenerator.TreeType.RANDOM, TreeType.BROWN_MUSHROOM);
        treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_REDWOOD, TreeType.REDWOOD);
        treeTypeMapping.put(TreeGenerator.TreeType.PINE, TreeType.REDWOOD);
        for (TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
            if (treeTypeMapping.get(type) == null) {
                WorldEdit.logger.severe("No TreeType mapping for TreeGenerator.TreeType." + type);
            }
        }
    }

    public static TreeType toBukkitTreeType(TreeGenerator.TreeType type) {
        return treeTypeMapping.get(type);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector pt) {
        TreeType bukkitType = toBukkitTreeType(type);
        return type != null && world.generateTree(BukkitUtil.toLocation(world, pt), bukkitType,
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
                item.getData());
        world.dropItemNaturally(BukkitUtil.toLocation(world, pt), bukkitItem);
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
        boolean killPets = (flags & KillFlags.PETS) != 0;
        boolean killNPCs = (flags & KillFlags.NPCS) != 0;
        boolean killAnimals = (flags & KillFlags.ANIMALS) != 0;
        boolean withLightning = (flags & KillFlags.WITH_LIGHTNING) != 0;
        boolean killGolems = (flags & KillFlags.GOLEMS) != 0;

        int num = 0;
        double radiusSq = radius * radius;

        Location bukkitOrigin = BukkitUtil.toLocation(world, origin);

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
                // Temporary solution to fix Golems being butchered.
                if (!killGolems && Class.forName("org.bukkit.entity.Golem").isAssignableFrom(ent.getClass())) {
                    continue;
                }
            } catch (ClassNotFoundException e) {}

            try {
                // Temporary solution until org.bukkit.entity.NPC is widely deployed.
                if (!killNPCs && Class.forName("org.bukkit.entity.NPC").isAssignableFrom(ent.getClass())) {
                    continue;
                }
            } catch (ClassNotFoundException e) {}

            if (radius < 0 || bukkitOrigin.distanceSquared(ent.getLocation()) <= radiusSq) {
                if (withLightning) {
                    world.strikeLightningEffect(ent.getLocation());
                }
                ent.remove();
                ++num;
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
            } else if (type == EntityType.FALLING_BLOCKS) {
                if (ent instanceof FallingBlock) {
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
        }

        return num;
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
        Sign sign = (Sign) state;
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
        if (!(state instanceof org.bukkit.inventory.InventoryHolder)) {
            return new BaseItemStack[0];
        }

        org.bukkit.inventory.InventoryHolder container = (org.bukkit.inventory.InventoryHolder) state;
        Inventory inven = container.getInventory();
        if (container instanceof Chest) {
            inven = getBlockInventory((Chest) container);
        }
        int size = inven.getSize();
        BaseItemStack[] contents = new BaseItemStack[size];

        for (int i = 0; i < size; ++i) {
            ItemStack bukkitStack = inven.getItem(i);
            if (bukkitStack != null && bukkitStack.getTypeId() > 0) {
                contents[i] = new BaseItemStack(
                        bukkitStack.getTypeId(),
                        bukkitStack.getAmount(),
                        bukkitStack.getDurability());
                try {
                    for (Map.Entry<Enchantment, Integer> entry : bukkitStack.getEnchantments().entrySet()) {
                        contents[i].getEnchantments().put(entry.getKey().getId(), entry.getValue());
                    }
                } catch (Throwable ignore) {}
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
        if (!(state instanceof org.bukkit.inventory.InventoryHolder)) {
            return false;
        }

        org.bukkit.inventory.InventoryHolder chest = (org.bukkit.inventory.InventoryHolder) state;
        Inventory inven = chest.getInventory();
        if (chest instanceof Chest) {
            inven = getBlockInventory((Chest) chest);
        }
        int size = inven.getSize();

        for (int i = 0; i < size; ++i) {
            if (i >= contents.length) {
                break;
            }

            if (contents[i] != null) {
                ItemStack toAdd = new ItemStack(contents[i].getType(),
                        contents[i].getAmount(),
                        contents[i].getData());
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
    }

    /**
     * Returns whether a block has a valid ID.
     *
     * @param type
     * @return
     */
    @Override
    public boolean isValidBlockType(int type) {
        if (!skipNmsValidBlockCheck) {
            try {
                return type == 0 || (type >= 1 && type < net.minecraft.server.Block.byId.length
                        && net.minecraft.server.Block.byId[type] != null);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking NMS valid block type", e);
                skipNmsValidBlockCheck = true;
            }
        }
        return Material.getMaterial(type) != null && Material.getMaterial(type).isBlock();
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
        if (!world.isChunkLoaded(pt.getBlockX() >> 4, pt.getBlockZ() >> 4)) {
            world.loadChunk(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BukkitWorld)) {
            return false;
        }

        return ((BukkitWorld) other).world.equals(world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public int getMaxY() {
        return world.getMaxHeight() - 1;
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        for (BlockVector2D chunkPos : chunks) {
            world.refreshChunk(chunkPos.getBlockX(), chunkPos.getBlockZ());
        }
    }

    private static final Map<Integer, Effect> effects = new HashMap<Integer, Effect>();
    static {
        for (Effect effect : Effect.values()) {
            effects.put(effect.getId(), effect);
        }
    }

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        final Effect effect = effects.get(type);
        if (effect == null) {
            return false;
        }

        world.playEffect(BukkitUtil.toLocation(world, position), effect, data);

        return true;
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).breakNaturally();
    }

    @Override
    public LocalEntity[] getEntities(Region region) {
        List<BukkitEntity> entities = new ArrayList<BukkitEntity>();
        for (Vector2D pt : region.getChunks()) {
            if (world.isChunkLoaded(pt.getBlockX(), pt.getBlockZ())) {
                Entity[] ents = world.getChunkAt(pt.getBlockX(), pt.getBlockZ()).getEntities();
                for (Entity ent : ents) {
                    if (region.contains(BukkitUtil.toVector(ent.getLocation()))) {
                        entities.add(BukkitUtil.toLocalEntity(ent));
                    }
                }
            }
        }
        return entities.toArray(new BukkitEntity[entities.size()]);
    }

    @Override
    public int killEntities(LocalEntity... entities) {
        int amount = 0;
        Set<UUID> toKill = new HashSet<UUID>();
        for (LocalEntity entity : entities) {
            toKill.add(((BukkitEntity) entity).getEntityId());
        }
        for (Entity entity : world.getEntities()) {
            if (toKill.contains(entity.getUniqueId())) {
                entity.remove();
                ++amount;
            }
        }
        return amount;
    }

    @Override
    public BaseBlock getBlock(Vector pt) {
        int type = getBlockType(pt);
        int data = getBlockData(pt);

        switch (type) {
        case BlockID.WALL_SIGN:
        case BlockID.SIGN_POST:
        //case BlockID.CHEST: // Prevent data loss for now
        //case BlockID.FURNACE:
        //case BlockID.BURNING_FURNACE:
        //case BlockID.DISPENSER:
        //case BlockID.MOB_SPAWNER:
        case BlockID.NOTE_BLOCK:
            return super.getBlock(pt);
        default:
            if (!skipNmsAccess) {
                try {
                    NmsBlock block = NmsBlock.get(world, pt, type, data);
                    if (block != null) {
                        return block;
                    }
                } catch (Throwable t) {
                    logger.log(Level.WARNING,
                            "WorldEdit: Failed to do NMS access for direct NBT data copy", t);
                    skipNmsAccess = true;
                }
            }
        }

        return super.getBlock(pt);
    }

    @Override
    public boolean setBlock(Vector pt, com.sk89q.worldedit.foundation.Block block, boolean notifyAdjacent) {
        if (!skipNmsSafeSet) {
            try {
                return NmsBlock.setSafely(this, pt, block, notifyAdjacent);
            } catch (Throwable t) {
                logger.log(Level.WARNING,
                        "WorldEdit: Failed to do NMS safe block set", t);
                skipNmsSafeSet = true;
            }
        }

        return super.setBlock(pt, block, notifyAdjacent);
    }
}
