/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import io.papermc.lib.PaperLib;
import org.bukkit.Effect;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitWorld extends AbstractWorld {

    private static final Logger logger = WorldEdit.logger;

    private static final boolean HAS_3D_BIOMES;

    private static final Map<Integer, Effect> effects = new HashMap<>();

    static {
        for (Effect effect : Effect.values()) {
            @SuppressWarnings("deprecation")
            int id = effect.getId();
            effects.put(id, effect);
        }

        boolean temp;
        try {
            World.class.getMethod("getBiome", int.class, int.class, int.class);
            temp = true;
        } catch (NoSuchMethodException e) {
            temp = false;
        }
        HAS_3D_BIOMES = temp;
    }

    private final WeakReference<World> worldRef;
    private final WorldNativeAccess<?, ?, ?> worldNativeAccess;

    /**
     * Construct the object.
     *
     * @param world the world
     */
    public BukkitWorld(World world) {
        this.worldRef = new WeakReference<>(world);
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            this.worldNativeAccess = adapter.createWorldNativeAccess(world);
        } else {
            this.worldNativeAccess = null;
        }
    }

    @Override
    public List<com.sk89q.worldedit.entity.Entity> getEntities(Region region) {
        World world = getWorld();

        List<Entity> ents = world.getEntities();
        List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<>();
        for (Entity ent : ents) {
            if (region.contains(BukkitAdapter.asBlockVector(ent.getLocation()))) {
                entities.add(BukkitAdapter.adapt(ent));
            }
        }
        return entities;
    }

    @Override
    public List<com.sk89q.worldedit.entity.Entity> getEntities() {
        List<com.sk89q.worldedit.entity.Entity> list = new ArrayList<>();
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
            try {
                Entity createdEntity = adapter.createEntity(BukkitAdapter.adapt(getWorld(), location), entity);
                if (createdEntity != null) {
                    return new BukkitEntity(createdEntity);
                } else {
                    return null;
                }
            } catch (Exception e) {
                logger.warn("Corrupt entity found when creating: " + entity.getType().getId());
                if (entity.getNbt() != null) {
                    logger.warn(entity.getNbt().toString());
                }
                e.printStackTrace();
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

    @Override
    public String getName() {
        return getWorld().getName();
    }

    @Override
    public String getId() {
        return getWorld().getName().replace(" ", "_").toLowerCase(Locale.ROOT);
    }

    @Override
    public Path getStoragePath() {
        Path worldFolder = getWorld().getWorldFolder().toPath();
        switch (getWorld().getEnvironment()) {
            case NETHER:
                return worldFolder.resolve("DIM-1");
            case THE_END:
                return worldFolder.resolve("DIM1");
            case NORMAL:
            default:
                return worldFolder;
        }
    }

    @Override
    public int getBlockLightLevel(BlockVector3 pt) {
        return getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getLightLevel();
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        try {
            if (adapter != null) {
                return adapter.regenerate(getWorld(), region, extent, options);
            } else {
                throw new UnsupportedOperationException("Missing BukkitImplAdapater for this version.");
            }
        } catch (Exception e) {
            logger.warn("Regeneration via adapter failed.", e);
            return false;
        }
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
    public boolean clearContainerBlockContents(BlockVector3 pt) {
        checkNotNull(pt);
        if (!getBlock(pt).getBlockType().getMaterial().hasContainer()) {
            return false;
        }

        Block block = getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        BlockState state = PaperLib.getBlockState(block, false).getState();
        if (!(state instanceof InventoryHolder)) {
            return false;
        }

        InventoryHolder chest = (InventoryHolder) state;
        Inventory inven = chest.getInventory();
        if (chest instanceof Chest) {
            inven = getBlockInventory((Chest) chest);
        }
        inven.clear();
        return true;
    }

    /**
     * An EnumMap that stores which WorldEdit TreeTypes apply to which Bukkit TreeTypes.
     */
    private static final EnumMap<TreeGenerator.TreeType, TreeType> treeTypeMapping =
            new EnumMap<>(TreeGenerator.TreeType.class);

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
                WorldEdit.logger.error("No TreeType mapping for TreeGenerator.TreeType." + type);
            }
        }
    }

    public static TreeType toBukkitTreeType(TreeGenerator.TreeType type) {
        return treeTypeMapping.get(type);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 pt) {
        World world = getWorld();
        TreeType bukkitType = toBukkitTreeType(type);
        if (bukkitType == TreeType.CHORUS_PLANT) {
            pt = pt.add(0, 1, 0); // bukkit skips the feature gen which does this offset normally, so we have to add it back
        }
        return type != null && world.generateTree(BukkitAdapter.adapt(world, pt), bukkitType,
                new EditSessionBlockChangeDelegate(editSession));
    }

    @Override
    public void dropItem(Vector3 pt, BaseItemStack item) {
        World world = getWorld();
        world.dropItemNaturally(BukkitAdapter.adapt(world, pt), BukkitAdapter.adapt(item));
    }

    @Override
    public void checkLoadedChunk(BlockVector3 pt) {
        World world = getWorld();

        world.getChunkAt(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
    }

    @Override
    public boolean equals(Object other) {
        final World ref = worldRef.get();
        if (ref == null) {
            return false;
        } else if (other == null) {
            return false;
        } else if ((other instanceof BukkitWorld)) {
            World otherWorld = ((BukkitWorld) other).worldRef.get();
            return ref.equals(otherWorld);
        } else if (other instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) other).getName().equals(ref.getName());
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

    @SuppressWarnings("deprecation")
    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
        World world = getWorld();
        for (BlockVector2 chunkPos : chunks) {
            world.refreshChunk(chunkPos.getBlockX(), chunkPos.getBlockZ());
        }
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        World world = getWorld();

        final Effect effect = effects.get(type);
        if (effect == null) {
            return false;
        }

        world.playEffect(BukkitAdapter.adapt(world, position), effect, data);

        return true;
    }

    @Override
    public WeatherType getWeather() {
        if (getWorld().isThundering()) {
            return WeatherTypes.THUNDER_STORM;
        } else if (getWorld().hasStorm()) {
            return WeatherTypes.RAIN;
        }

        return WeatherTypes.CLEAR;
    }

    @Override
    public long getRemainingWeatherDuration() {
        return getWorld().getWeatherDuration();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        if (weatherType == WeatherTypes.THUNDER_STORM) {
            getWorld().setThundering(true);
        } else if (weatherType == WeatherTypes.RAIN) {
            getWorld().setStorm(true);
        } else {
            getWorld().setStorm(false);
            getWorld().setThundering(false);
        }
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        // Who named these methods...
        if (weatherType == WeatherTypes.THUNDER_STORM) {
            getWorld().setThundering(true);
            getWorld().setThunderDuration((int) duration);
            getWorld().setWeatherDuration((int) duration);
        } else if (weatherType == WeatherTypes.RAIN) {
            getWorld().setStorm(true);
            getWorld().setWeatherDuration((int) duration);
        } else {
            getWorld().setStorm(false);
            getWorld().setThundering(false);
            getWorld().setWeatherDuration((int) duration);
        }
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return BukkitAdapter.asBlockVector(getWorld().getSpawnLocation());
    }

    @Override
    public void simulateBlockMine(BlockVector3 pt) {
        getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).breakNaturally();
    }

    @Override
    public boolean canPlaceAt(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState blockState) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.canPlaceAt(getWorld(), position, blockState);
        }
        // We can't check, so assume yes.
        return true;
    }

    private static volatile boolean hasWarnedImplError = false;

    @Override
    public com.sk89q.worldedit.world.block.BlockState getBlock(BlockVector3 position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            try {
                return adapter.getBlock(BukkitAdapter.adapt(getWorld(), position)).toImmutableState();
            } catch (Exception e) {
                if (!hasWarnedImplError) {
                    hasWarnedImplError = true;
                    logger.warn("Unable to retrieve block via impl adapter", e);
                }
            }
        }
        Block bukkitBlock = getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return BukkitAdapter.adapt(bukkitBlock.getBlockData());
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) {
        clearContainerBlockContents(position);
        if (worldNativeAccess != null) {
            try {
                return worldNativeAccess.setBlock(position, block, sideEffects);
            } catch (Exception e) {
                if (block instanceof BaseBlock && ((BaseBlock) block).getNbt() != null) {
                    logger.warn("Tried to set a corrupt tile entity at " + position.toString()
                        + ": " + ((BaseBlock) block).getNbt(), e);
                } else {
                    logger.warn("Failed to set block via adapter, falling back to generic", e);
                }
            }
        }
        Block bukkitBlock = getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        bukkitBlock.setBlockData(BukkitAdapter.adapt(block), sideEffects.doesApplyAny());
        return true;
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.getBlock(BukkitAdapter.adapt(getWorld(), position));
        } else {
            return getBlock(position).toBaseBlock();
        }
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState previousType,
            SideEffectSet sideEffectSet) {
        if (worldNativeAccess != null) {
            worldNativeAccess.applySideEffects(position, previousType, sideEffectSet);
            return Sets.intersection(
                    WorldEditPlugin.getInstance().getInternalPlatform().getSupportedSideEffects(),
                    sideEffectSet.getSideEffectsToApply()
            );
        }

        return ImmutableSet.of();
    }

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.simulateItemUse(getWorld(), position, item, face);
        }

        return false;
    }

    @Override
    public boolean fullySupports3DBiomes() {
        // Supports if API does and we're not in the overworld
        return HAS_3D_BIOMES && getWorld().getEnvironment() != World.Environment.NORMAL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BiomeType getBiome(BlockVector3 position) {
        if (HAS_3D_BIOMES) {
            return BukkitAdapter.adapt(getWorld().getBiome(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        } else {
            return BukkitAdapter.adapt(getWorld().getBiome(position.getBlockX(), position.getBlockZ()));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        if (HAS_3D_BIOMES) {
            getWorld().setBiome(position.getBlockX(), position.getBlockY(), position.getBlockZ(), BukkitAdapter.adapt(biome));
        } else {
            getWorld().setBiome(position.getBlockX(), position.getBlockZ(), BukkitAdapter.adapt(biome));
        }
        return true;
    }
}
