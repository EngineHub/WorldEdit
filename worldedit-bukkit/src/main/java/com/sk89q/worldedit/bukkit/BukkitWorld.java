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
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.UnsupportedVersionEditException;
import com.sk89q.worldedit.bukkit.folia.FoliaScheduler;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
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
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import io.papermc.lib.PaperLib;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitWorld extends AbstractWorld {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private static final Map<Integer, Effect> effects = new HashMap<>();

    static {
        for (Effect effect : Effect.values()) {
            int id = effect.getId();
            effects.put(id, effect);
        }
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
        final BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter == null) {
            return null;
        }

        final World bukkitWorld = getWorld();
        final Location bLoc = BukkitAdapter.adapt(bukkitWorld, location);
        final BlockVector3 position = BukkitAdapter.asBlockVector(bLoc);

        try {
            return executeOnRegion(position, () -> {
                try {
                    org.bukkit.entity.Entity created = adapter.createEntity(bLoc, entity);
                    return created != null ? new BukkitEntity(created) : null;
                } catch (Exception e) {
                    LOGGER.warn("Corrupt entity found when creating: {}", entity.getType().id(), e);
                    if (entity.getNbt() != null) {
                        LOGGER.warn(entity.getNbt().toString());
                    }
                    return null;
                }
            }, "Failed to create entity safely at " + bLoc);
        } catch (RuntimeException e) {
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
    public String id() {
        return getWorld().getName().replace(" ", "_").toLowerCase(Locale.ROOT);
    }

    @Override
    public Path getStoragePath() {
        Path worldFolder = getWorld().getWorldFolder().toPath();
        return switch (getWorld().getEnvironment()) {
            case NETHER -> worldFolder.resolve("DIM-1");
            case THE_END -> worldFolder.resolve("DIM1");
            default -> worldFolder;
        };
    }

    @Override
    public int getBlockLightLevel(BlockVector3 pt) {
        return getWorld().getBlockAt(pt.x(), pt.y(), pt.z()).getLightLevel();
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        try {
            if (adapter != null) {
                return adapter.regenerate(getWorld(), region, extent, options);
            } else {
                throw new UnsupportedOperationException("Missing BukkitImplAdapter for this version.");
            }
        } catch (Exception e) {
            LOGGER.warn("Regeneration via adapter failed.", e);
            return false;
        }
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 pt) {
        checkNotNull(pt);

        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            try {
                return adapter.clearContainerBlockContents(getWorld(), pt);
            } catch (Exception ignored) {
                // It's fine if we can't, we'll try the generic way below.
            }
        }

        if (!getBlock(pt).getBlockType().getMaterial().hasContainer()) {
            return false;
        }

        Block block = getWorld().getBlockAt(pt.x(), pt.y(), pt.z());
        BlockState state = PaperLib.getBlockState(block, false).getState();
        if (!(state instanceof InventoryHolder inventoryHolder)) {
            return false;
        }

        Inventory inven = inventoryHolder.getInventory();
        if (inventoryHolder instanceof Chest chest) {
            inven = chest.getBlockInventory();
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
                WorldEdit.logger.error("No TreeType mapping for TreeGenerator.TreeType.{}", type);
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
        return type != null && world.generateTree(
            BukkitAdapter.adapt(world, pt),
            ThreadLocalRandom.current(),
            bukkitType,
            block -> {
                Mask mask = editSession.getMask();
                var blockVector = BukkitAdapter.asBlockVector(block.getLocation());
                if (mask != null && !mask.test(blockVector)) {
                    return false;
                }
                try {
                    editSession.setBlock(blockVector, BukkitAdapter.adapt(block.getBlockData()));
                } catch (MaxChangedBlocksException ignored) {
                    // It's fine, we just stop generating.
                }
                return false;
            }
        );
    }

    @Override
    public void dropItem(Vector3 pt, BaseItemStack item) {
        World world = getWorld();
        Location loc = BukkitAdapter.adapt(world, pt);

        final int chunkX = loc.getBlockX() >> 4;
        final int chunkZ = loc.getBlockZ() >> 4;

        FoliaScheduler.getRegionScheduler().execute(
            WorldEditPlugin.getInstance(), world, chunkX, chunkZ,
            () -> world.dropItemNaturally(loc, BukkitAdapter.adapt(item))
        );
    }

    @Override
    @SuppressWarnings({"FutureReturnValueIgnored", "unused"})
    public void checkLoadedChunk(BlockVector3 pt) {
        World world = getWorld();
        executeOnRegionVoid(pt, () -> {
            int chunkX = pt.x() >> 4;
            int chunkZ = pt.z() >> 4;
            world.getChunkAtAsync(chunkX, chunkZ);
        }, "Failed to ensure chunk [" + (pt.x() >> 4) + "," + (pt.z() >> 4) + "] is loaded safely");
    }

    @Override
    public boolean equals(Object other) {
        final World ref = worldRef.get();
        if (ref == null) {
            return false;
        }
        return switch (other) {
            case BukkitWorld bukkitWorld -> {
                World otherWorld = bukkitWorld.worldRef.get();
                yield ref.equals(otherWorld);
            }
            case com.sk89q.worldedit.world.World world -> world.getName().equals(ref.getName());
            case null, default -> false;
        };
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
    public int getMinY() {
        return getWorld().getMinHeight();
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
        World world = getWorld();
        for (BlockVector2 chunkPos : chunks) {
            world.refreshChunk(chunkPos.x(), chunkPos.z());
        }
    }

    @Override
    public void sendBiomeUpdates(Iterable<BlockVector2> chunks) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            adapter.sendBiomeUpdates(getWorld(), chunks);
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
        getWorld().getBlockAt(pt.x(), pt.y(), pt.z()).breakNaturally();
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

    @Override
    public boolean generateFeature(ConfiguredFeatureType type, EditSession editSession, BlockVector3 position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.generateFeature(type, getWorld(), editSession, position);
        }
        // No adapter, we can't generate this.
        return false;
    }

    @Override
    public boolean generateStructure(StructureType type, EditSession editSession, BlockVector3 position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.generateStructure(type, getWorld(), editSession, position);
        }
        // No adapter, we can't generate this.
        return false;
    }

    @Override
    public com.sk89q.worldedit.world.block.BlockState getBlock(BlockVector3 position) {
        return executeOnRegion(position, () -> {
            World world = getWorld();
            Block block = world.getBlockAt(position.x(), position.y(), position.z());
            return BukkitAdapter.adapt(block.getBlockData());
        }, "Failed to retrieve block state asynchronously");
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) {
        clearContainerBlockContents(position);
        return executeOnRegion(position, () -> {
            World world = getWorld();
            return doSetBlock(world, position, block, sideEffects);
        }, "Failed to set block safely at " + position);
    }

    /**
     * Executes a task on the appropriate region thread for the given position.
     * If already on the correct thread (Folia) or not using Folia, executes immediately.
     *
     * @param position the position to determine the region
     * @param task the task to execute
     * @param errorMessage the error message to use if execution fails
     * @return the result of the task
     */
    private <T> T executeOnRegion(BlockVector3 position, java.util.function.Supplier<T> task, String errorMessage) {
        World world = getWorld();
        int chunkX = position.x() >> 4;
        int chunkZ = position.z() >> 4;

        if (FoliaScheduler.isFolia()) {
            if (Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                return task.get();
            }
        } else {
            return task.get();
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        FoliaScheduler.getRegionScheduler().execute(
            WorldEditPlugin.getInstance(), world, chunkX, chunkZ,
            () -> {
                try {
                    future.complete(task.get());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
        );

        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Executes a void task on the appropriate region thread for the given position.
     * If already on the correct thread (Folia) or not using Folia, executes immediately.
     *
     * @param position the position to determine the region
     * @param task the task to execute
     * @param errorMessage the error message to use if execution fails
     */
    private void executeOnRegionVoid(BlockVector3 position, Runnable task, String errorMessage) {
        World world = getWorld();
        int chunkX = position.x() >> 4;
        int chunkZ = position.z() >> 4;

        if (FoliaScheduler.isFolia()) {
            if (Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                task.run();
                return;
            }
        } else {
            task.run();
            return;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        FoliaScheduler.getRegionScheduler().execute(
            WorldEditPlugin.getInstance(), world, chunkX, chunkZ,
            () -> {
                try {
                    task.run();
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
        );

        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Internal helper to perform the actual block mutation.
     */
    private <B extends BlockStateHolder<B>> boolean doSetBlock(World world, BlockVector3 position, B block, SideEffectSet sideEffects) {
        if (worldNativeAccess != null) {
            try {
                return worldNativeAccess.setBlock(position, block, sideEffects);
            } catch (Exception e) {
                if (block instanceof BaseBlock baseBlock && baseBlock.getNbt() != null) {
                    LOGGER.warn("Tried to set a corrupt tile entity at {}: {}", position.toString(), baseBlock.getNbt(), e);
                } else {
                    LOGGER.warn("Failed to set block via adapter, falling back to generic", e);
                }
            }
        }

        if (WorldEditPlugin.getInstance().getLocalConfiguration().unsupportedVersionEditing) {
            Block bukkitBlock = world.getBlockAt(position.x(), position.y(), position.z());
            bukkitBlock.setBlockData(BukkitAdapter.adapt(block), sideEffects.doesApplyAny());
            return true;
        } else {
            throw new RuntimeException(new UnsupportedVersionEditException());
        }
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return executeOnRegion(position, () -> {
            World world = getWorld();
            Block block = world.getBlockAt(position.x(), position.y(), position.z());
            return BukkitAdapter.adapt(block.getBlockData()).toBaseBlock();
        }, "Failed to get full block asynchronously");
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position,
                                            com.sk89q.worldedit.world.block.BlockState previousType,
                                            SideEffectSet sideEffectSet) {
        return executeOnRegion(position, () -> doApplySideEffects(position, previousType, sideEffectSet),
            "Failed to apply side effects safely");
    }

    private Set<SideEffect> doApplySideEffects(BlockVector3 position,
                                               com.sk89q.worldedit.world.block.BlockState previousType,
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
    public BiomeType getBiome(BlockVector3 position) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            return adapter.getBiome(BukkitAdapter.adapt(getWorld(), position));
        } else {
            return BukkitAdapter.adapt(getWorld().getBiome(position.x(), position.y(), position.z()));
        }
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter != null) {
            adapter.setBiome(BukkitAdapter.adapt(getWorld(), position), biome);
        } else {
            getWorld().setBiome(position.x(), position.y(), position.z(), BukkitAdapter.adapt(biome));
        }
        return true;
    }

    @Override
    public boolean isValid() {
        return worldRef.get() != null;
    }
}
