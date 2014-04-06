/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.blocks.ContainerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.bukkit.entity.BukkitEntity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitWorld extends LocalWorld {

    private static final Logger logger = WorldEdit.logger;
    private final WeakReference<World> worldRef;
    private static boolean skipNmsAccess = false;
    private static boolean skipNmsSafeSet = false;
    private static boolean skipNmsValidBlockCheck = false;

    /*
     * holder for the nmsblock class that we should use
     */
    private static Class<? extends NmsBlock> nmsBlockType;
    private static Method nmsSetMethod;
    private static Method nmsValidBlockMethod;
    private static Method nmsGetMethod;
    private static Method nmsSetSafeMethod;

    // copied from WG
    private static <T extends Enum<T>> T tryEnum(Class<T> enumType, String ... values) {
        for (String val : values) {
            try {
                return Enum.valueOf(enumType, val);
            } catch (IllegalArgumentException e) {}
        }
        return null;
    }
    private static org.bukkit.entity.EntityType tntMinecartType;
    private static boolean checkMinecartType = true;

    /**
     * Construct the object.
     * @param world
     */
    @SuppressWarnings("unchecked")
    public BukkitWorld(World world) {
        this.worldRef = new WeakReference<World>(world);

        if (checkMinecartType) {
            tntMinecartType = tryEnum(org.bukkit.entity.EntityType.class, "MINECART_TNT");
            checkMinecartType = false;
        }
        // check if we have a class we can use for nms access

        // only run once per server startup
        if (nmsBlockType != null || skipNmsAccess || skipNmsSafeSet || skipNmsValidBlockCheck) return;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (!(plugin instanceof WorldEditPlugin)) return; // hopefully never happens
        WorldEditPlugin wePlugin = ((WorldEditPlugin) plugin);
        File nmsBlocksDir = new File(wePlugin.getDataFolder() + File.separator + "nmsblocks" + File.separator);
        if (nmsBlocksDir.listFiles() == null) { // no files to use
            skipNmsAccess = true; skipNmsSafeSet = true; skipNmsValidBlockCheck = true;
            return;
        }
        try {
            // make a classloader that can handle our blocks
            NmsBlockClassLoader loader = new NmsBlockClassLoader(BukkitWorld.class.getClassLoader(), nmsBlocksDir);
            String filename;
            for (File f : nmsBlocksDir.listFiles()) {
                if (!f.isFile()) continue;
                filename = f.getName();
                // load class using magic keyword
                Class<?> testBlock = null;
                try {
                    testBlock = loader.loadClass("CL-NMS" + filename);
                } catch (Throwable e) {
                    // someone is putting things where they don't belong
                    continue;
                }
                filename = filename.replaceFirst(".class$", ""); // get rid of extension
                if (NmsBlock.class.isAssignableFrom(testBlock)) {
                    // got a NmsBlock, test it now
                    Class<? extends NmsBlock> nmsClass = (Class<? extends NmsBlock>) testBlock;
                    boolean canUse = false;
                    try {
                        canUse = (Boolean) nmsClass.getMethod("verify").invoke(null);
                    } catch (Throwable e) {
                        continue;
                    }
                    if (!canUse) continue; // not for this server
                    nmsBlockType = nmsClass;
                    nmsSetMethod = nmsBlockType.getMethod("set", World.class, Vector.class, BaseBlock.class);
                    nmsValidBlockMethod = nmsBlockType.getMethod("isValidBlockType", int.class);
                    nmsGetMethod = nmsBlockType.getMethod("get", World.class, Vector.class, int.class, int.class);
                    nmsSetSafeMethod = nmsBlockType.getMethod("setSafely",
                            BukkitWorld.class, Vector.class, com.sk89q.worldedit.foundation.Block.class, boolean.class);
                    // phew
                    break;
                }
            }
            if (nmsBlockType != null) {
                logger.info("[WorldEdit] Using external NmsBlock for this version: " + nmsBlockType.getName());
            } else {
                // try our default
                try {
                    nmsBlockType = (Class<? extends NmsBlock>) Class.forName("com.sk89q.worldedit.bukkit.DefaultNmsBlock");
                    boolean canUse = (Boolean) nmsBlockType.getMethod("verify").invoke(null);
                    if (canUse) {
                        nmsSetMethod = nmsBlockType.getMethod("set", World.class, Vector.class, BaseBlock.class);
                        nmsValidBlockMethod = nmsBlockType.getMethod("isValidBlockType", int.class);
                        nmsGetMethod = nmsBlockType.getMethod("get", World.class, Vector.class, int.class, int.class);
                        nmsSetSafeMethod = nmsBlockType.getMethod("setSafely",
                                BukkitWorld.class, Vector.class, com.sk89q.worldedit.foundation.Block.class, boolean.class);
                        logger.info("[WorldEdit] Using inbuilt NmsBlock for this version.");
                    }
                } catch (Throwable e) {
                    // OMG DEVS WAI U NO SUPPORT <xyz> SERVER
                    skipNmsAccess = true; skipNmsSafeSet = true; skipNmsValidBlockCheck = true;
                    logger.warning("[WorldEdit] No compatible nms block class found.");
                }
            }
        } catch (Throwable e) {
            logger.warning("[WorldEdit] Unable to load NmsBlock classes, make sure they are installed correctly.");
            e.printStackTrace();
            skipNmsAccess = true; skipNmsSafeSet = true; skipNmsValidBlockCheck = true;
        }
    }

    private class NmsBlockClassLoader extends ClassLoader {
        public File searchDir;
        public NmsBlockClassLoader(ClassLoader parent, File searchDir) {
            super(parent);
            this.searchDir = searchDir;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (!name.startsWith("CL-NMS")) {
                return super.loadClass(name);
            } else {
                name = name.replace("CL-NMS", ""); // hacky lol
            }
            try {
                URL url = new File(searchDir, name).toURI().toURL();
                InputStream input = url.openConnection().getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int data = input.read();
                while (data != -1) {
                    buffer.write(data);
                    data = input.read();
                }
                input.close();

                byte[] classData = buffer.toByteArray();

                return defineClass(name.replaceFirst(".class$", ""), classData, 0, classData.length);
            } catch (Throwable e) {
                throw new ClassNotFoundException();
            }
        }
    }

    /**
     * Get the world handle.
     *
     * @return the world
     */
    public World getWorld() {
        return checkNotNull(worldRef.get(), "The world was unloaded and the reference is unavailable");
    }

    /**
     * Get the world handle.
     *
     * @return the world
     */
    protected World getWorldChecked() throws WorldEditException {
        World world = worldRef.get();
        if (world == null) {
            throw new WorldUnloadedException();
        }
        return world;
    }

    @Override
    public String getName() {
        return getWorld().getName();
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
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type);
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
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type, false);
    }

    @Override
    public boolean setTypeIdAndData(Vector pt, int type, int data) {
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeIdAndData(type, (byte) data, true);
    }

    @Override
    public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeIdAndData(type, (byte) data, false);
    }

    /**
     * Get block type.
     *
     * @param pt
     * @return
     */
    @Override
    public int getBlockType(Vector pt) {
        return getWorld().getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    @Override
    public void setBlockData(Vector pt, int data) {
        getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte) data);
    }

    @Override
    public void setBlockDataFast(Vector pt, int data) {
        getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte) data, false);
    }

    @Override
    public int getBlockData(Vector pt) {
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
    }

    @Override
    public int getBlockLightLevel(Vector pt) {
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getLightLevel();
    }

    @Override
    public BiomeType getBiome(Vector2D pt) {
        Biome bukkitBiome = getWorld().getBiome(pt.getBlockX(), pt.getBlockZ());
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
            getWorld().setBiome(pt.getBlockX(), pt.getBlockZ(), bukkitBiome);
        }
    }

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
                getWorld().regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
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

    @Override
    public boolean copyToWorld(Vector pt, BaseBlock block) {
        World world = getWorld();

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

        if (block instanceof SkullBlock) {
            // Skull block
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.Skull)) return false;
            Skull bukkit = (Skull) state;
            SkullBlock we = (SkullBlock) block;
            // this is dumb
            SkullType skullType = SkullType.SKELETON;
            switch (we.getSkullType()) {
            case 0:
                skullType = SkullType.SKELETON;
                break;
            case 1:
                skullType = SkullType.WITHER;
                break;
            case 2:
                skullType = SkullType.ZOMBIE;
                break;
            case 3:
                skullType = SkullType.PLAYER;
                break;
            case 4:
                skullType = SkullType.CREEPER;
                break;
            }
            bukkit.setSkullType(skullType);
            BlockFace rotation;
            switch (we.getRot()) {
            // soooo dumb
            case 0:
                rotation = BlockFace.NORTH;
                break;
            case 1:
                rotation = BlockFace.NORTH_NORTH_EAST;
                break;
            case 2:
                rotation = BlockFace.NORTH_EAST;
                break;
            case 3:
                rotation = BlockFace.EAST_NORTH_EAST;
                break;
            case 4:
                rotation = BlockFace.EAST;
                break;
            case 5:
                rotation = BlockFace.EAST_SOUTH_EAST;
                break;
            case 6:
                rotation = BlockFace.SOUTH_EAST;
                break;
            case 7:
                rotation = BlockFace.SOUTH_SOUTH_EAST;
                break;
            case 8:
                rotation = BlockFace.SOUTH;
                break;
            case 9:
                rotation = BlockFace.SOUTH_SOUTH_WEST;
                break;
            case 10:
                rotation = BlockFace.SOUTH_WEST;
                break;
            case 11:
                rotation = BlockFace.WEST_SOUTH_WEST;
                break;
            case 12:
                rotation = BlockFace.WEST;
                break;
            case 13:
                rotation = BlockFace.WEST_NORTH_WEST;
                break;
            case 14:
                rotation = BlockFace.NORTH_WEST;
                break;
            case 15:
                rotation = BlockFace.NORTH_NORTH_WEST;
                break;
            default:
                rotation = BlockFace.NORTH;
                break;
            }
            bukkit.setRotation(rotation);
            if (we.getOwner() != null && !we.getOwner().isEmpty()) bukkit.setOwner(we.getOwner());
            bukkit.update(true);
            return true;
        }

        if (!skipNmsAccess) {
            try {
                return (Boolean) nmsSetMethod.invoke(null, world, pt, block);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "WorldEdit: Failed to do NMS access for direct NBT data copy", t);
                skipNmsAccess = true;
            }
        }

        return false;
    }

    @Override
    public boolean copyFromWorld(Vector pt, BaseBlock block) {
        World world = getWorld();

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

        if (block instanceof SkullBlock) {
            // Skull block
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.Skull)) return false;
            Skull bukkit = (Skull) state;
            SkullBlock we = (SkullBlock) block;
            byte skullType = 0;
            switch (bukkit.getSkullType()) {
            // this is dumb but whoever wrote the class is stupid
            case SKELETON:
                skullType = 0;
                break;
            case WITHER:
                skullType = 1;
                break;
            case ZOMBIE:
                skullType = 2;
                break;
            case PLAYER:
                skullType = 3;
                break;
            case CREEPER:
                skullType = 4;
                break;
            }
            we.setSkullType(skullType);
            byte rot = 0;
            switch (bukkit.getRotation()) {
            // this is even more dumb, hurray for copy/paste
            case NORTH:
                rot = (byte) 0;
                break;
            case NORTH_NORTH_EAST:
                rot = (byte) 1;
                break;
            case NORTH_EAST:
                rot = (byte) 2;
                break;
            case EAST_NORTH_EAST:
                rot = (byte) 3;
                break;
            case EAST:
                rot = (byte) 4;
                break;
            case EAST_SOUTH_EAST:
                rot = (byte) 5;
                break;
            case SOUTH_EAST:
                rot = (byte) 6;
                break;
            case SOUTH_SOUTH_EAST:
                rot = (byte) 7;
                break;
            case SOUTH:
                rot = (byte) 8;
                break;
            case SOUTH_SOUTH_WEST:
                rot = (byte) 9;
                break;
            case SOUTH_WEST:
                rot = (byte) 10;
                break;
            case WEST_SOUTH_WEST:
                rot = (byte) 11;
                break;
            case WEST:
                rot = (byte) 12;
                break;
            case WEST_NORTH_WEST:
                rot = (byte) 13;
                break;
            case NORTH_WEST:
                rot = (byte) 14;
                break;
            case NORTH_NORTH_WEST:
                rot = (byte) 15;
                break;
            }
            we.setRot(rot);
            we.setOwner(bukkit.hasOwner() ? bukkit.getOwner() : "");
            return true;
        }

        return false;
    }

    /**
     * Gets the single block inventory for a potentially double chest.
     * Handles people who have an old version of Bukkit.
     * This should be replaced with {@link org.bukkit.block.Chest#getBlockInventory()}
     * in a few months (now = March 2012) // note from future dev - lol
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

    @Override
    public boolean clearContainerBlockContents(Vector pt) {
        Block block = getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
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

    @Override
    @Deprecated
    public boolean generateTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.TREE, editSession, pt);
    }

    @Override
    @Deprecated
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.BIG_TREE, editSession, pt);
    }

    @Override
    @Deprecated
    public boolean generateBirchTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.BIRCH, editSession, pt);
    }

    @Override
    @Deprecated
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) {
        return generateTree(TreeGenerator.TreeType.REDWOOD, editSession, pt);
    }

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
        for (TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
            try {
                TreeType bukkitType = TreeType.valueOf(type.name());
                treeTypeMapping.put(type, bukkitType);
            } catch (IllegalArgumentException e) {
                // Unhandled TreeType
            }
        }
        // Other mappings for WE-specific values
        treeTypeMapping.put(TreeGenerator.TreeType.SHORT_JUNGLE, TreeType.SMALL_JUNGLE);
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
        World world = getWorld();
        TreeType bukkitType = toBukkitTreeType(type);
        return type != null && world.generateTree(BukkitUtil.toLocation(world, pt), bukkitType,
                new EditSessionBlockChangeDelegate(editSession));
    }

    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        World world = getWorld();
        ItemStack bukkitItem = new ItemStack(item.getType(), item.getAmount(),
                item.getData());
        world.dropItemNaturally(BukkitUtil.toLocation(world, pt), bukkitItem);
    }

    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        World world = getWorld();

        boolean killPets = (flags & KillFlags.PETS) != 0;
        boolean killNPCs = (flags & KillFlags.NPCS) != 0;
        boolean killAnimals = (flags & KillFlags.ANIMALS) != 0;
        boolean withLightning = (flags & KillFlags.WITH_LIGHTNING) != 0;
        boolean killGolems = (flags & KillFlags.GOLEMS) != 0;
        boolean killAmbient = (flags & KillFlags.AMBIENT) != 0;

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
                continue; // tamed pet
            }

            if (!killGolems && ent instanceof Golem) {
                continue;
            }

            if (!killNPCs && ent instanceof Villager) {
                continue;
            }

            if (!killAmbient && ent instanceof Ambient) {
                continue;
            }

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
        World world = getWorld();

        int num = 0;
        double radiusSq = Math.pow(radius, 2);

        for (Entity ent : world.getEntities()) {
            if (radius != -1
                    && origin.distanceSq(BukkitUtil.toVector(ent.getLocation())) > radiusSq) {
                continue;
            }

            switch (type) {
            case ALL:
                if (ent instanceof Projectile || ent instanceof Boat || ent instanceof Item
                        || ent instanceof FallingBlock || ent instanceof Minecart || ent instanceof Hanging
                        || ent instanceof TNTPrimed || ent instanceof ExperienceOrb) {
                    ent.remove();
                    num++;
                }
                break;

            case PROJECTILES:
            case ARROWS:
                if (ent instanceof Projectile) {
                    // covers: arrow, egg, enderpearl, fireball, fish, snowball, throwpotion, thrownexpbottle
                    ent.remove();
                    ++num;
                }
                break;

            case BOATS:
                if (ent instanceof Boat) {
                    ent.remove();
                    ++num;
                }
                break;

            case ITEMS:
                if (ent instanceof Item) {
                    ent.remove();
                    ++num;
                }
                break;

            case FALLING_BLOCKS:
                if (ent instanceof FallingBlock) {
                    ent.remove();
                    ++num;
                }
                break;

            case MINECARTS:
                if (ent instanceof Minecart) {
                    ent.remove();
                    ++num;
                }
                break;

            case PAINTINGS:
                if (ent instanceof Painting) {
                    ent.remove();
                    ++num;
                }
                break;

            case ITEM_FRAMES:
                if (ent instanceof ItemFrame) {
                    ent.remove();
                    ++num;
                }
                break;

            case TNT:
                if (ent instanceof TNTPrimed || ent.getType() == tntMinecartType) {
                    ent.remove();
                    ++num;
                }
                break;

            case XP_ORBS:
                if (ent instanceof ExperienceOrb) {
                    ent.remove();
                    ++num;
                }
                break;
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
        World world = getWorld();

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
        World world = getWorld();

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
        Block block = getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
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
        Block block = getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
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
                return (Boolean) nmsValidBlockMethod.invoke(null, type);
            } catch (Throwable e) {
                skipNmsValidBlockCheck = true;
            }
        }
        return Material.getMaterial(type) != null && Material.getMaterial(type).isBlock();
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
        World world = getWorld();

        if (!world.isChunkLoaded(pt.getBlockX() >> 4, pt.getBlockZ() >> 4)) {
            world.loadChunk(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
        }
    }

    @Override
    public boolean equals(Object other) {
        World world = getWorld();

        if (!(other instanceof BukkitWorld)) {
            return false;
        }

        return ((BukkitWorld) other).getWorld().equals(world);
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public int getMaxY() {
        return getWorld().getMaxHeight() - 1;
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        World world = getWorld();
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
        World world = getWorld();

        final Effect effect = effects.get(type);
        if (effect == null) {
            return false;
        }

        world.playEffect(BukkitUtil.toLocation(world, position), effect, data);

        return true;
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).breakNaturally();
    }

    @Override
    public LocalEntity[] getEntities(Region region) {
        World world = getWorld();

        List<BukkitEntity> entities = new ArrayList<BukkitEntity>();
        for (Vector2D pt : region.getChunks()) {
            if (!world.isChunkLoaded(pt.getBlockX(), pt.getBlockZ())) {
                continue;
            }

            final Entity[] ents = world.getChunkAt(pt.getBlockX(), pt.getBlockZ()).getEntities();
            for (Entity ent : ents) {
                if (region.contains(BukkitUtil.toVector(ent.getLocation()))) {
                    entities.add(BukkitUtil.toLocalEntity(ent));
                }
            }
        }
        return entities.toArray(new BukkitEntity[entities.size()]);
    }

    @Override
    public int killEntities(LocalEntity... entities) {
        World world = getWorld();

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
        case BlockID.HEAD:
            return super.getBlock(pt);
        default:
            if (!skipNmsAccess) {
                try {
                    NmsBlock block = null;
                    block = (NmsBlock) nmsGetMethod.invoke(null, getWorld(), pt, type, data);
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

    @SuppressWarnings("deprecation")
    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        Block bukkitBlock = world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return new LazyBlock(bukkitBlock.getTypeId(), bukkitBlock.getData(), this, position);
    }

    @Override
    public boolean setBlock(Vector pt, BaseBlock block, boolean notifyAdjacent) throws WorldEditException {
        if (!skipNmsSafeSet) {
            try {
                return (Boolean) nmsSetSafeMethod.invoke(null, this, pt, block, notifyAdjacent);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "WorldEdit: Failed to do NMS safe block set", t);
                skipNmsSafeSet = true;
            }
        }

        return super.setBlock(pt, block, notifyAdjacent);
    }
}
