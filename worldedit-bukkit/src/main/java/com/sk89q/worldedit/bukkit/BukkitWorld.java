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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitWorld extends LocalWorld {

    private static final Logger logger = WorldEdit.logger;

    private static final Map<Integer, Effect> effects = new HashMap<Integer, Effect>();
    static {
        for (Effect effect : Effect.values()) {
            effects.put(effect.getId(), effect);
        }
    }

    private final WeakReference<World> worldRef;

    /**
     * Construct the object.
     *
     * @param world the world
     */
    @SuppressWarnings("unchecked")
    public BukkitWorld(World world) {
        this.worldRef = new WeakReference<World>(world);
    }

    @Override
    public List<com.sk89q.worldedit.entity.Entity> getEntities(Region region) {
        World world = getWorld();

        List<Entity> ents = world.getEntities();
        List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<com.sk89q.worldedit.entity.Entity>();
        for (Entity ent : ents) {
            if (region.contains(BukkitUtil.toVector(ent.getLocation()))) {
                entities.add(BukkitAdapter.adapt(ent));
            }
        }
        return entities;
    }

    @Override
    public List<com.sk89q.worldedit.entity.Entity> getEntities() {
        List<com.sk89q.worldedit.entity.Entity> list = new ArrayList<com.sk89q.worldedit.entity.Entity>();
        for (Entity entity : getWorld().getEntities()) {
            list.add(BukkitAdapter.adapt(entity));
        }
        return list;
    }

    @Nullable
    @Override
    public com.sk89q.worldedit.entity.Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            Entity createdEntity = adapter.createEntity(BukkitAdapter.adapt(getWorld(), location), entity);
            if (createdEntity != null) {
                return new BukkitEntity(createdEntity);
            } else {
                return null;
            }
        } else {
            return null;
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

    @Override
    public int getBlockLightLevel(Vector pt) {
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getLightLevel();
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
                logger.log(Level.WARNING, "Chunk generation via Bukkit raised an error", t);
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
        treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_BIRCH, TreeType.BIRCH);
        treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_JUNGLE, TreeType.JUNGLE);
        treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_MUSHROOM, TreeType.BROWN_MUSHROOM);
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

    @SuppressWarnings("deprecation")
    @Override
    public boolean isValidBlockType(int type) {
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
        if (other == null) {
            return false;
        } else if ((other instanceof BukkitWorld)) {
            return ((BukkitWorld) other).getWorld().equals(getWorld());
        } else if (other instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) other).getName().equals(getName());
        } else {
            return false;
        }
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
    public WorldData getWorldData() {
        return BukkitWorldData.getInstance();
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).breakNaturally();
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.getBlock(BukkitAdapter.adapt(getWorld(), position));
        } else {
            Block bukkitBlock = getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
            return new BaseBlock(bukkitBlock.getTypeId(), bukkitBlock.getData());
        }
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.setBlock(BukkitAdapter.adapt(getWorld(), position), block, notifyAndLight);
        } else {
            Block bukkitBlock = getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
            return bukkitBlock.setTypeIdAndData(block.getType(), (byte) block.getData(), notifyAndLight);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        Block bukkitBlock = world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return new LazyBlock(bukkitBlock.getTypeId(), bukkitBlock.getData(), this, position);
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            int id = adapter.getBiomeId(getWorld().getBiome(position.getBlockX(), position.getBlockZ()));
            return new BaseBiome(id);
        } else {
            return new BaseBiome(0);
        }
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            Biome bukkitBiome = adapter.getBiome(biome.getId());
            getWorld().setBiome(position.getBlockX(), position.getBlockZ(), bukkitBiome);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @deprecated Use {@link #setBlock(Vector, BaseBlock, boolean)}
     */
    @Deprecated
    public boolean setBlock(Vector pt, com.sk89q.worldedit.foundation.Block block, boolean notifyAdjacent) throws WorldEditException {
        return setBlock(pt, (BaseBlock) block, notifyAdjacent);
    }
}
