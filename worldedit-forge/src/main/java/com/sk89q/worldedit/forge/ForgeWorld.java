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

package com.sk89q.worldedit.forge;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.util.BiomeMath;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IClearable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

    private static final Random random = new Random();
    private static final int UPDATE = 1, NOTIFY = 2;

    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    ForgeWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<>(world);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws WorldEditException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorldChecked() throws WorldEditException {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldReferenceLostException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorld() {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return getWorld().getWorldInfo().getWorldName();
    }

    @Override
    public String getId() {
        return DimensionManager.getRegistry().getKey(getWorld().dimension.getType()).toString();
    }

    @Override
    public Path getStoragePath() {
        final World world = getWorld();
        if (world instanceof ServerWorld) {
            return ((ServerWorld) world).getSaveHandler().getWorldDirectory().toPath();
        }
        return null;
    }

    /**
     * This is a heavily modified function stripped from MC to apply worldedit-modifications.
     *
     * @see World#markAndNotifyBlock
     */
    private void markAndNotifyBlock(World world, BlockPos pos, Chunk chunk, net.minecraft.block.BlockState blockstate,
            net.minecraft.block.BlockState newState, SideEffectSet sideEffectSet) {
        Block block = newState.getBlock();
        net.minecraft.block.BlockState blockstate1 = world.getBlockState(pos);
        if (blockstate1 == newState) {
            if (blockstate != blockstate1) {
                world.markBlockRangeForRenderUpdate(pos, blockstate, blockstate1);
            }

            // Remove redundant branches
            if (chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING)) {
                if (sideEffectSet.shouldApply(SideEffect.ENTITY_AI)) {
                    world.notifyBlockUpdate(pos, blockstate, newState, UPDATE | NOTIFY);
                } else {
                    // If we want to skip entity AI, just call the chunk dirty flag.
                    ((ServerChunkProvider) world.getChunkProvider()).markBlockChanged(pos);
                }
            }

            if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
                world.notifyNeighbors(pos, blockstate.getBlock());
                if (newState.hasComparatorInputOverride()) {
                    world.updateComparatorOutputLevel(pos, block);
                }
            }

            // Make connection updates optional
            if (sideEffectSet.shouldApply(SideEffect.VALIDATION)) {
                blockstate.updateDiagonalNeighbors(world, pos, 2);
                newState.updateNeighbors(world, pos, 2);
                newState.updateDiagonalNeighbors(world, pos, 2);
            }

            // This is disabled for other platforms, but keep it for mods.
            world.onBlockStateChange(pos, blockstate, blockstate1);
        }
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        Chunk chunk = world.getChunk(x >> 4, z >> 4);
        BlockPos pos = new BlockPos(x, y, z);
        net.minecraft.block.BlockState old = chunk.getBlockState(pos);
        int stateId = BlockStateIdAccess.getBlockStateId(block.toImmutableState());
        net.minecraft.block.BlockState newState =
            BlockStateIdAccess.isValidInternalId(stateId)
                ? Block.getStateById(stateId)
                : ForgeAdapter.adapt(block.toImmutableState());
        net.minecraft.block.BlockState successState = chunk.setBlockState(pos, newState, false);
        boolean successful = successState != null;

        // Create the TileEntity
        if (successful || old == newState) {
            if (block instanceof BaseBlock) {
                CompoundTag tag = ((BaseBlock) block).getNbtData();
                if (tag != null) {
                    CompoundNBT nativeTag = NBTConverter.toNative(tag);
                    nativeTag.putString("id", ((BaseBlock) block).getNbtId());
                    TileEntityUtils.setTileEntity(world, position, nativeTag);
                    successful = true; // update if TE changed as well
                }
            }
        }

        if (successful) {
            // update our block first
            if (sideEffects.shouldApply(SideEffect.VALIDATION)) {
                net.minecraft.block.BlockState update = Block.getValidBlockForPosition(
                    newState, world, pos
                );
                if (update != newState) {
                    world.setBlockState(pos, update);
                }
            }
            if (sideEffects.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
                world.getChunkProvider().getLightManager().checkBlock(pos);
            }
            markAndNotifyBlock(world, pos, chunk, old, newState, sideEffects);
        }

        return successful;
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
        World world = getWorldChecked();
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        net.minecraft.block.BlockState oldData = ForgeAdapter.adapt(previousType);
        net.minecraft.block.BlockState newData = world.getBlockState(pos);

        if (sideEffectSet.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
            world.getChunkProvider().getLightManager().checkBlock(pos);
        }

        Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getY() >> 4);
        markAndNotifyBlock(world, pos, chunk, oldData, newData, sideEffectSet); // Update
        return Sets.intersection(ForgeWorldEdit.inst.getPlatform().getSupportedSideEffects(), sideEffectSet.getSideEffectsToApply());
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);
        return getWorld().getLight(ForgeAdapter.toBlockPos(position));
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);
        TileEntity tile = getWorld().getTileEntity(ForgeAdapter.toBlockPos(position));
        if (tile instanceof IClearable) {
            ((IClearable) tile).clear();
            return true;
        }
        return false;
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        checkNotNull(position);
        return ForgeAdapter.adapt(getWorld().getBiome(new BlockPos(position.getBlockX(), 0, position.getBlockZ())));
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        IChunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4, ChunkStatus.FULL, false);
        BiomeContainer container = chunk == null ? null : chunk.getBiomes();
        if (chunk == null || container == null) {
            return false;
        }
        // Temporary, while biome setting is 2D only
        for (int i = 0; i < BiomeMath.VERTICAL_BIT_MASK; i++) {
            int idx = BiomeMath.computeBiomeIndex(position.getX(), i, position.getZ());
            container.biomes[idx] = ForgeAdapter.adapt(biome);
        }
        chunk.setModified(true);
        return true;
    }

    private static LoadingCache<ServerWorld, WorldEditFakePlayer> fakePlayers
            = CacheBuilder.newBuilder().weakKeys().softValues().build(CacheLoader.from(WorldEditFakePlayer::new));

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        ItemStack stack = ForgeAdapter.adapt(new BaseItemStack(item.getType(), item.getNbtData(), 1));
        ServerWorld world = (ServerWorld) getWorld();
        final WorldEditFakePlayer fakePlayer;
        try {
            fakePlayer = fakePlayers.get(world);
        } catch (ExecutionException ignored) {
            return false;
        }
        fakePlayer.setHeldItem(Hand.MAIN_HAND, stack);
        fakePlayer.setLocationAndAngles(position.getBlockX(), position.getBlockY(), position.getBlockZ(),
                (float) face.toVector().toYaw(), (float) face.toVector().toPitch());
        final BlockPos blockPos = ForgeAdapter.toBlockPos(position);
        final BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(ForgeAdapter.toVec3(position),
                ForgeAdapter.adapt(face), blockPos, false);
        ItemUseContext itemUseContext = new ItemUseContext(fakePlayer, Hand.MAIN_HAND, rayTraceResult);
        ActionResultType used = stack.onItemUse(itemUseContext);
        if (used != ActionResultType.SUCCESS) {
            // try activating the block
            ActionResultType resultType = getWorld().getBlockState(blockPos)
                .onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, rayTraceResult);
            if (resultType.isSuccessOrConsume()) {
                used = resultType;
            } else {
                used = stack.getItem().onItemRightClick(world, fakePlayer, Hand.MAIN_HAND).getType();
            }
        }
        return used == ActionResultType.SUCCESS;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        ItemEntity entity = new ItemEntity(getWorld(), position.getX(), position.getY(), position.getZ(), ForgeAdapter.adapt(item));
        entity.setPickupDelay(10);
        getWorld().addEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        BlockPos pos = ForgeAdapter.toBlockPos(position);
        getWorld().destroyBlock(pos, true);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // Don't even try to regen if it's going to fail.
        AbstractChunkProvider provider = getWorld().getChunkProvider();
        if (!(provider instanceof ServerChunkProvider)) {
            return false;
        }

        File saveFolder = Files.createTempDir();
        // register this just in case something goes wrong
        // normally it should be deleted at the end of this method
        saveFolder.deleteOnExit();
        try {
            ServerWorld originalWorld = (ServerWorld) getWorld();

            MinecraftServer server = originalWorld.getServer();
            SaveHandler saveHandler = new SaveHandler(saveFolder, originalWorld.getSaveHandler().getWorldDirectory().getName(), server, server.getDataFixer());
            try (World freshWorld = new ServerWorld(server, server.getBackgroundExecutor(), saveHandler, originalWorld.getWorldInfo(),
                    originalWorld.dimension.getType(), originalWorld.getProfiler(), new NoOpChunkStatusListener())) {

                // Pre-gen all the chunks
                // We need to also pull one more chunk in every direction
                CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 0, 16), region.getMaximumPoint().add(16, 0, 16));
                for (BlockVector2 chunk : expandedPreGen.getChunks()) {
                    freshWorld.getChunk(chunk.getBlockX(), chunk.getBlockZ());
                }

                ForgeWorld from = new ForgeWorld(freshWorld);
                for (BlockVector3 vec : region) {
                    editSession.setBlock(vec, from.getFullBlock(vec));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        } finally {
            saveFolder.delete();
        }

        return true;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> createTreeFeatureGenerator(TreeType type) {
        switch (type) {
            case TREE: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.OAK_TREE_CONFIG);
            case BIG_TREE: return Feature.FANCY_TREE.withConfiguration(DefaultBiomeFeatures.FANCY_TREE_CONFIG);
            case REDWOOD: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.SPRUCE_TREE_CONFIG);
            case TALL_REDWOOD: return Feature.MEGA_SPRUCE_TREE.withConfiguration(DefaultBiomeFeatures.MEGA_SPRUCE_TREE_CONFIG);
            case MEGA_REDWOOD: return Feature.MEGA_SPRUCE_TREE.withConfiguration(DefaultBiomeFeatures.MEGA_PINE_TREE_CONFIG);
            case BIRCH: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.BIRCH_TREE_CONFIG);
            case JUNGLE: return Feature.MEGA_JUNGLE_TREE.withConfiguration(DefaultBiomeFeatures.MEGA_JUNGLE_TREE_CONFIG);
            case SMALL_JUNGLE: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.JUNGLE_TREE_CONFIG);
            case SHORT_JUNGLE: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.JUNGLE_SAPLING_TREE_CONFIG);
            case JUNGLE_BUSH: return Feature.JUNGLE_GROUND_BUSH.withConfiguration(DefaultBiomeFeatures.JUNGLE_GROUND_BUSH_CONFIG);
            case SWAMP: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.SWAMP_TREE_CONFIG);
            case ACACIA: return Feature.ACACIA_TREE.withConfiguration(DefaultBiomeFeatures.ACACIA_TREE_CONFIG);
            case DARK_OAK: return Feature.DARK_OAK_TREE.withConfiguration(DefaultBiomeFeatures.DARK_OAK_TREE_CONFIG);
            case TALL_BIRCH: return Feature.NORMAL_TREE.withConfiguration(DefaultBiomeFeatures.field_230130_i_);
            case RED_MUSHROOM: return Feature.HUGE_RED_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_RED_MUSHROOM);
            case BROWN_MUSHROOM: return Feature.HUGE_BROWN_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_BROWN_MUSHROOM);
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        ConfiguredFeature<?, ?> generator = createTreeFeatureGenerator(type);
        ChunkGenerator<?> chunkGenerator = ((ServerChunkProvider) getWorld().getChunkProvider())
            .getChunkGenerator();
        return generator != null
            && generator.place(getWorld(), chunkGenerator, random,
            ForgeAdapter.toBlockPos(position));
    }

    @Override
    public void checkLoadedChunk(BlockVector3 pt) {
        getWorld().getChunk(ForgeAdapter.toBlockPos(pt));
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
        fixLighting(chunks);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {
        World world = getWorld();
        for (BlockVector2 chunk : chunks) {
            world.getChunkProvider().getLightManager().retainData(new ChunkPos(chunk.getBlockX(), chunk.getBlockZ()), true);
        }
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        getWorld().playEvent(type, ForgeAdapter.toBlockPos(position.toBlockPoint()), data);
        return true;
    }

    @Override
    public WeatherType getWeather() {
        WorldInfo info = getWorld().getWorldInfo();
        if (info.isThundering()) {
            return WeatherTypes.THUNDER_STORM;
        }
        if (info.isRaining()) {
            return WeatherTypes.RAIN;
        }
        return WeatherTypes.CLEAR;
    }

    @Override
    public long getRemainingWeatherDuration() {
        WorldInfo info = getWorld().getWorldInfo();
        if (info.isThundering()) {
            return info.getThunderTime();
        }
        if (info.isRaining()) {
            return info.getRainTime();
        }
        return info.getClearWeatherTime();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        setWeather(weatherType, 0);
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        WorldInfo info = getWorld().getWorldInfo();
        if (weatherType == WeatherTypes.THUNDER_STORM) {
            info.setClearWeatherTime(0);
            info.setThundering(true);
            info.setThunderTime((int) duration);
        } else if (weatherType == WeatherTypes.RAIN) {
            info.setClearWeatherTime(0);
            info.setRaining(true);
            info.setRainTime((int) duration);
        } else if (weatherType == WeatherTypes.CLEAR) {
            info.setRaining(false);
            info.setThundering(false);
            info.setClearWeatherTime((int) duration);
        }
    }

    @Override
    public int getMaxY() {
        return getWorld().getMaxHeight() - 1;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return ForgeAdapter.adapt(getWorld().getSpawnPoint());
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        net.minecraft.block.BlockState mcState = getWorld()
                .getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4)
                .getBlockState(ForgeAdapter.toBlockPos(position));

        BlockState matchingBlock = BlockStateIdAccess.getBlockStateById(Block.getStateId(mcState));
        if (matchingBlock != null) {
            return matchingBlock;
        }

        return ForgeAdapter.adapt(mcState);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        TileEntity tile = getWorld().getChunk(pos).getTileEntity(pos);

        if (tile != null) {
            return getBlock(position).toBaseBlock(NBTConverter.fromNative(TileEntityUtils.copyNbtData(tile)));
        } else {
            return getBlock(position).toBaseBlock();
        }
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if ((o instanceof ForgeWorld)) {
            ForgeWorld other = ((ForgeWorld) o);
            World otherWorld = other.worldRef.get();
            World thisWorld = worldRef.get();
            return otherWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        final World world = getWorld();
        if (!(world instanceof ServerWorld)) {
            return Collections.emptyList();
        }
        return ((ServerWorld) world).getEntities().filter(e -> region.contains(ForgeAdapter.adapt(e.getPosition())))
                .map(ForgeEntity::new).collect(Collectors.toList());
    }

    @Override
    public List<? extends Entity> getEntities() {
        final World world = getWorld();
        if (!(world instanceof ServerWorld)) {
            return Collections.emptyList();
        }
        return ((ServerWorld) world).getEntities().map(ForgeEntity::new).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();
        final Optional<EntityType<?>> entityType = EntityType.byKey(entity.getType().getId());
        if (!entityType.isPresent()) return null;
        net.minecraft.entity.Entity createdEntity = entityType.get().create(world);
        if (createdEntity != null) {
            CompoundTag nativeTag = entity.getNbtData();
            if (nativeTag != null) {
                CompoundNBT tag = NBTConverter.toNative(entity.getNbtData());
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.remove(name);
                }
                createdEntity.read(tag);
            }

            createdEntity.setLocationAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            world.addEntity(createdEntity);
            return new ForgeEntity(createdEntity);
        } else {
            return null;
        }
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    @SuppressWarnings("serial")
    private static final class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

    private static class NoOpChunkStatusListener implements IChunkStatusListener {
        @Override
        public void start(ChunkPos chunkPos) {
        }

        @Override
        public void statusChanged(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        }

        @Override
        public void stop() {
        }
    }
}
