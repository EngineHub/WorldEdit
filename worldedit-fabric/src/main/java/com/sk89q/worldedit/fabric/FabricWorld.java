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

package com.sk89q.worldedit.fabric;

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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.BranchedTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OakTreeFeature;
import net.minecraft.world.level.LevelProperties;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class FabricWorld extends AbstractWorld {

    private static final Random random = new Random();
    private static final int UPDATE = 1, NOTIFY = 2;

    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    FabricWorld(World world) {
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
        return getWorld().getLevelProperties().getLevelName();
    }

    @Override
    public String getId() {
        return getWorld().getLevelProperties().getLevelName()
                .replace(" ", "_").toLowerCase(Locale.ROOT)
                + getWorld().dimension.getType().getSuffix();
    }

    @Override
    public Path getStoragePath() {
        final World world = getWorld();
        if (world instanceof ServerWorld) {
            return ((ServerWorld) world).getSaveHandler().getWorldDir().toPath();
        }
        return null;
    }

    /**
     * This is a heavily modified function stripped from MC to apply worldedit-modifications.
     *
     * It is taken from Forge's World class, not Fabric's. This code should be consistent with the
     * ForgeWorld code.
     */
    private void markAndNotifyBlock(World world, BlockPos pos, WorldChunk worldChunk, net.minecraft.block.BlockState blockState,
            net.minecraft.block.BlockState state, SideEffectSet sideEffectSet) {
        Block block = state.getBlock();
        net.minecraft.block.BlockState blockState2 = world.getBlockState(pos);
        if (blockState2 == state) {
            if (blockState != blockState2) {
                world.checkBlockRerender(pos, blockState, blockState2);
            }

            if (worldChunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING)) {
                if (sideEffectSet.shouldApply(SideEffect.ENTITY_AI)) {
                    world.updateListeners(pos, blockState, state, UPDATE | NOTIFY);
                } else {
                    // If we want to skip entity AI, just call the chunk dirty flag.
                    ((ServerChunkManager) world.getChunkManager()).markForUpdate(pos);
                }
            }

            if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
                world.updateNeighbors(pos, blockState.getBlock());
                if (state.hasComparatorOutput()) {
                    world.updateHorizontalAdjacent(pos, block);
                }
            }

            if (sideEffectSet.shouldApply(SideEffect.VALIDATION)) {
                blockState.method_11637(world, pos, 2);
                state.updateNeighborStates(world, pos, 2);
                state.method_11637(world, pos, 2);
            }

            // This is disabled for other platforms, but keep it for mods.
            world.onBlockChanged(pos, blockState, blockState2);
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
        WorldChunk chunk = world.getChunk(x >> 4, z >> 4);
        BlockPos pos = new BlockPos(x, y, z);
        net.minecraft.block.BlockState old = chunk.getBlockState(pos);
        int stateId = BlockStateIdAccess.getBlockStateId(block.toImmutableState());
        net.minecraft.block.BlockState newState =
            BlockStateIdAccess.isValidInternalId(stateId)
                ? Block.getStateFromRawId(stateId)
                : FabricAdapter.adapt(block.toImmutableState());
        net.minecraft.block.BlockState successState = chunk.setBlockState(pos, newState, false);
        boolean successful = successState != null;

        // Create the TileEntity
        if (successful || old == newState) {
            if (block instanceof BaseBlock) {
                CompoundTag tag = ((BaseBlock) block).getNbtData();
                if (tag != null) {
                    tag = tag.createBuilder()
                        .putInt("x", x)
                        .putInt("y", y)
                        .putInt("z", z)
                        .build();
                    net.minecraft.nbt.CompoundTag nativeTag = NBTConverter.toNative(tag);
                    BlockEntity tileEntity = getWorld().getWorldChunk(pos).getBlockEntity(pos);
                    if (tileEntity != null) {
                        tileEntity.setLocation(world, pos);
                        tileEntity.fromTag(nativeTag);
                        successful = true; // update if TE changed as well
                    }
                }
            }
        }

        if (successful) {
            if (sideEffects.shouldApply(SideEffect.VALIDATION)) {
                net.minecraft.block.BlockState update = Block.getRenderingState(
                    newState, world, pos
                );
                if (update != newState) {
                    world.setBlockState(pos, update);
                }
            }
            if (sideEffects.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
                world.getChunkManager().getLightingProvider().checkBlock(pos);
            }
            markAndNotifyBlock(world, pos, chunk, old, newState, sideEffects);
        }

        return successful;
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
        World world = getWorldChecked();
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        net.minecraft.block.BlockState oldData = FabricAdapter.adapt(previousType);
        net.minecraft.block.BlockState newData = world.getBlockState(pos);

        if (sideEffectSet.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
            world.getChunkManager().getLightingProvider().checkBlock(pos);
        }

        Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getY() >> 4);
        markAndNotifyBlock(world, pos, null, oldData, newData, sideEffectSet); // Update
        return Sets.intersection(FabricWorldEdit.inst.getPlatform().getSupportedSideEffects(), sideEffectSet.getSideEffectsToApply());
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);
        return getWorld().getLightLevel(FabricAdapter.toBlockPos(position));
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);
        BlockEntity tile = getWorld().getBlockEntity(FabricAdapter.toBlockPos(position));
        if ((tile instanceof Clearable)) {
            ((Clearable) tile).clear();
            return true;
        }
        return false;
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        checkNotNull(position);
        return FabricAdapter.adapt(getWorld().getBiome(new BlockPos(position.getBlockX(), 0, position.getBlockZ())));
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        Chunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        }
        MutableBiomeArray biomeArray = MutableBiomeArray.inject(chunk.getBiomeArray());
        // Temporary, while biome setting is 2D only
        for (int i = 0; i < BiomeMath.VERTICAL_BIT_MASK; i++) {
            biomeArray.setBiome(position.getX(), i, position.getZ(), FabricAdapter.adapt(biome));
        }
        chunk.setShouldSave(true);
        return true;
    }

    private static final LoadingCache<ServerWorld, WorldEditFakePlayer> fakePlayers
            = CacheBuilder.newBuilder().weakKeys().softValues().build(CacheLoader.from(WorldEditFakePlayer::new));

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        ItemStack stack = FabricAdapter.adapt(new BaseItemStack(item.getType(), item.getNbtData(), 1));
        ServerWorld world = (ServerWorld) getWorld();
        final WorldEditFakePlayer fakePlayer;
        try {
            fakePlayer = fakePlayers.get(world);
        } catch (ExecutionException ignored) {
            return false;
        }
        fakePlayer.setStackInHand(Hand.MAIN_HAND, stack);
        fakePlayer.updatePositionAndAngles(position.getBlockX(), position.getBlockY(), position.getBlockZ(),
                (float) face.toVector().toYaw(), (float) face.toVector().toPitch());
        final BlockPos blockPos = FabricAdapter.toBlockPos(position);
        final BlockHitResult rayTraceResult = new BlockHitResult(FabricAdapter.toVec3(position),
                FabricAdapter.adapt(face), blockPos, false);
        ItemUsageContext itemUseContext = new ItemUsageContext(fakePlayer, Hand.MAIN_HAND, rayTraceResult);
        ActionResult used = stack.useOnBlock(itemUseContext);
        if (used != ActionResult.SUCCESS) {
            // try activating the block
            used = getWorld().getBlockState(blockPos).onUse(world, fakePlayer, Hand.MAIN_HAND, rayTraceResult);
        }
        if (used != ActionResult.SUCCESS) {
            used = stack.use(world, fakePlayer, Hand.MAIN_HAND).getResult();
        }
        return used == ActionResult.SUCCESS;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        ItemEntity entity = new ItemEntity(getWorld(), position.getX(), position.getY(), position.getZ(), FabricAdapter.adapt(item));
        entity.setPickupDelay(10);
        getWorld().spawnEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        BlockPos pos = FabricAdapter.toBlockPos(position);
        getWorld().breakBlock(pos, true);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // Don't even try to regen if it's going to fail.
        ChunkManager provider = getWorld().getChunkManager();
        if (!(provider instanceof ServerChunkManager)) {
            return false;
        }

        File saveFolder = Files.createTempDir();
        // register this just in case something goes wrong
        // normally it should be deleted at the end of this method
        saveFolder.deleteOnExit();
        try {
            ServerWorld originalWorld = (ServerWorld) getWorld();

            MinecraftServer server = originalWorld.getServer();
            WorldSaveHandler saveHandler = new WorldSaveHandler(saveFolder, originalWorld.getSaveHandler().getWorldDir().getName(), server, server.getDataFixer());
            World freshWorld = new ServerWorld(server, server.getWorkerExecutor(), saveHandler, originalWorld.getLevelProperties(),
                    originalWorld.dimension.getType(), originalWorld.getProfiler(), new NoOpChunkStatusListener());

            // Pre-gen all the chunks
            // We need to also pull one more chunk in every direction
            CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 0, 16), region.getMaximumPoint().add(16, 0, 16));
            for (BlockVector2 chunk : expandedPreGen.getChunks()) {
                freshWorld.getChunk(chunk.getBlockX(), chunk.getBlockZ());
            }

            FabricWorld from = new FabricWorld(freshWorld);
            for (BlockVector3 vec : region) {
                editSession.setBlock(vec, from.getFullBlock(vec));
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
            // Based off of the SaplingGenerator class, as well as uses of DefaultBiomeFeatures fields
            case TREE: return Feature.NORMAL_TREE.configure(DefaultBiomeFeatures.OAK_TREE_CONFIG);
            case BIG_TREE: return Feature.FANCY_TREE.configure(DefaultBiomeFeatures.FANCY_TREE_CONFIG);
            case REDWOOD: return Feature.NORMAL_TREE.configure(DefaultBiomeFeatures.SPRUCE_TREE_CONFIG);
            case TALL_REDWOOD: return Feature.MEGA_SPRUCE_TREE.configure(DefaultBiomeFeatures.MEGA_SPRUCE_TREE_CONFIG);
            case MEGA_REDWOOD: return Feature.MEGA_SPRUCE_TREE.configure(DefaultBiomeFeatures.MEGA_PINE_TREE_CONFIG);
            case BIRCH: return Feature.NORMAL_TREE.configure(DefaultBiomeFeatures.BIRCH_TREE_CONFIG);
            case JUNGLE: return Feature.MEGA_JUNGLE_TREE.configure(DefaultBiomeFeatures.MEGA_JUNGLE_TREE_CONFIG);
            case SMALL_JUNGLE: return Feature.NORMAL_TREE.configure(DefaultBiomeFeatures.JUNGLE_TREE_CONFIG);
            case SHORT_JUNGLE: return new OakTreeFeature(BranchedTreeFeatureConfig::deserialize)
                .configure(DefaultBiomeFeatures.JUNGLE_SAPLING_TREE_CONFIG);
            case JUNGLE_BUSH: return Feature.JUNGLE_GROUND_BUSH.configure(DefaultBiomeFeatures.JUNGLE_GROUND_BUSH_CONFIG);
            case SWAMP: return Feature.NORMAL_TREE.configure(DefaultBiomeFeatures.SWAMP_TREE_CONFIG);
            case ACACIA: return Feature.ACACIA_TREE.configure(DefaultBiomeFeatures.ACACIA_TREE_CONFIG);
            case DARK_OAK: return Feature.DARK_OAK_TREE.configure(DefaultBiomeFeatures.DARK_OAK_TREE_CONFIG);
            case TALL_BIRCH: return Feature.NORMAL_TREE.configure(DefaultBiomeFeatures.LARGE_BIRCH_TREE_CONFIG);
            case RED_MUSHROOM: return Feature.HUGE_RED_MUSHROOM.configure(DefaultBiomeFeatures.HUGE_RED_MUSHROOM_CONFIG);
            case BROWN_MUSHROOM: return Feature.HUGE_BROWN_MUSHROOM.configure(DefaultBiomeFeatures.HUGE_BROWN_MUSHROOM_CONFIG);
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) {
        ConfiguredFeature<?, ?> generator = createTreeFeatureGenerator(type);
        ServerChunkManager chunkManager = (ServerChunkManager) getWorld().getChunkManager();
        return generator != null && generator.generate(
            getWorld(), chunkManager.getChunkGenerator(), random,
            FabricAdapter.toBlockPos(position)
        );
    }

    @Override
    public void checkLoadedChunk(BlockVector3 pt) {
        getWorld().getChunk(FabricAdapter.toBlockPos(pt));
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
        fixLighting(chunks);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {
        World world = getWorld();
        for (BlockVector2 chunk : chunks) {
            world.getChunkManager().getLightingProvider().setLightEnabled(
                new ChunkPos(chunk.getBlockX(), chunk.getBlockZ()), true
            );
        }
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        getWorld().playLevelEvent(type, FabricAdapter.toBlockPos(position.toBlockPoint()), data);
        return true;
    }

    @Override
    public WeatherType getWeather() {
        LevelProperties info = getWorld().getLevelProperties();
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
        LevelProperties info = getWorld().getLevelProperties();
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
        LevelProperties info = getWorld().getLevelProperties();
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
        return getWorld().getHeight() - 1;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return FabricAdapter.adapt(getWorld().getSpawnPos());
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        net.minecraft.block.BlockState mcState = getWorld()
                .getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4)
                .getBlockState(FabricAdapter.toBlockPos(position));

        BlockState matchingBlock = BlockStateIdAccess.getBlockStateById(Block.getRawIdFromState(mcState));
        if (matchingBlock != null) {
            return matchingBlock;
        }

        return FabricAdapter.adapt(mcState);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        // Avoid creation by using the CHECK mode -- if it's needed, it'll be re-created anyways
        BlockEntity tile = ((WorldChunk) getWorld().getChunk(pos)).getBlockEntity(pos, WorldChunk.CreationType.CHECK);

        if (tile != null) {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            tile.toTag(tag);
            return getBlock(position).toBaseBlock(NBTConverter.fromNative(tag));
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
        if ((o instanceof FabricWorld)) {
            FabricWorld other = ((FabricWorld) o);
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
        return ((ServerWorld) world).getEntities(null, entity -> true)
                .stream()
                .filter(e -> region.contains(FabricAdapter.adapt(e.getBlockPos())))
                .map(FabricEntity::new).collect(Collectors.toList());
    }

    @Override
    public List<? extends Entity> getEntities() {
        final World world = getWorld();
        if (!(world instanceof ServerWorld)) {
            return Collections.emptyList();
        }
        return ((ServerWorld) world).getEntities(null, entity -> true)
                .stream()
                .map(FabricEntity::new)
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();
        final Optional<EntityType<?>> entityType = EntityType.get(entity.getType().getId());
        if (!entityType.isPresent()) return null;
        net.minecraft.entity.Entity createdEntity = entityType.get().create(world);
        if (createdEntity != null) {
            CompoundTag nativeTag = entity.getNbtData();
            if (nativeTag != null) {
                net.minecraft.nbt.CompoundTag tag = NBTConverter.toNative(entity.getNbtData());
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.remove(name);
                }
                createdEntity.fromTag(tag);
            }

            createdEntity.updatePositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            world.spawnEntity(createdEntity);
            return new FabricEntity(createdEntity);
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

    private static class NoOpChunkStatusListener implements WorldGenerationProgressListener {
        @Override
        public void start(ChunkPos chunkPos) {
        }

        @Override
        public void setChunkStatus(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        }

        @Override
        public void stop() {
        }
    }
}
