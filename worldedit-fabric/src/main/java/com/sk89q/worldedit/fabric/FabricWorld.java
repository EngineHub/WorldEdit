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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
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
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.BirchTreeFeature;
import net.minecraft.world.gen.feature.DarkOakTreeFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.HugeBrownMushroomFeature;
import net.minecraft.world.gen.feature.HugeRedMushroomFeature;
import net.minecraft.world.gen.feature.JungleGroundBushFeature;
import net.minecraft.world.gen.feature.JungleTreeFeature;
import net.minecraft.world.gen.feature.LargeOakTreeFeature;
import net.minecraft.world.gen.feature.MegaJungleTreeFeature;
import net.minecraft.world.gen.feature.MegaPineTreeFeature;
import net.minecraft.world.gen.feature.OakTreeFeature;
import net.minecraft.world.gen.feature.PineTreeFeature;
import net.minecraft.world.gen.feature.PlantedFeatureConfig;
import net.minecraft.world.gen.feature.SavannaTreeFeature;
import net.minecraft.world.gen.feature.SpruceTreeFeature;
import net.minecraft.world.gen.feature.SwampTreeFeature;
import net.minecraft.world.level.LevelProperties;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class FabricWorld extends AbstractWorld {

    private static final Random random = new Random();
    private static final int UPDATE = 1, NOTIFY = 2;

    private static final net.minecraft.block.BlockState JUNGLE_LOG = Blocks.JUNGLE_LOG.getDefaultState();
    private static final net.minecraft.block.BlockState JUNGLE_LEAF = Blocks.JUNGLE_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, Boolean.TRUE);
    private static final net.minecraft.block.BlockState JUNGLE_SHRUB = Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, Boolean.TRUE);

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

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight) throws WorldEditException {
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
        OptionalInt stateId = BlockStateIdAccess.getBlockStateId(block.toImmutableState());
        net.minecraft.block.BlockState newState = stateId.isPresent() ? Block.getStateFromRawId(stateId.getAsInt()) : FabricAdapter.adapt(block.toImmutableState());
        net.minecraft.block.BlockState successState = chunk.setBlockState(pos, newState, false);
        boolean successful = successState != null;

        // Create the TileEntity
        if (successful || old == newState) {
            if (block instanceof BaseBlock) {
                CompoundTag tag = ((BaseBlock) block).getNbtData();
                if (tag != null) {
                    net.minecraft.nbt.CompoundTag nativeTag = NBTConverter.toNative(tag);
                    BlockEntity tileEntity = getWorld().getWorldChunk(pos).getBlockEntity(pos);
                    if (tileEntity != null) {
                        tileEntity.fromTag(nativeTag);
                        tileEntity.setPos(pos);
                        tileEntity.setWorld(world);
                        successful = true; // update if TE changed as well
                    }
                }
            }
        }

        if (successful && notifyAndLight) {
            world.getChunkManager().getLightingProvider().enqueueLightUpdate(pos);
            world.scheduleBlockRender(pos, old, newState);
            world.updateListeners(pos, old, newState, UPDATE | NOTIFY);
            world.updateNeighbors(pos, newState.getBlock());
            if (old.hasComparatorOutput()) {
                world.updateHorizontalAdjacent(pos, newState.getBlock());
            }
        }

        return successful;
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        net.minecraft.block.BlockState state = getWorld().getBlockState(pos);
        getWorld().updateListeners(pos, FabricAdapter.adapt(previousType), state, 1 | 2);
        getWorld().updateNeighbors(pos, state.getBlock());
        return true;
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
        chunk.getBiomeArray()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = FabricAdapter.adapt(biome);
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
        fakePlayer.setPositionAndAngles(position.getBlockX(), position.getBlockY(), position.getBlockZ(),
                (float) face.toVector().toYaw(), (float) face.toVector().toPitch());
        final BlockPos blockPos = FabricAdapter.toBlockPos(position);
        final BlockHitResult rayTraceResult = new BlockHitResult(FabricAdapter.toVec3(position),
                FabricAdapter.adapt(face), blockPos, false);
        ItemUsageContext itemUseContext = new ItemUsageContext(fakePlayer, Hand.MAIN_HAND, rayTraceResult);
        ActionResult used = stack.useOnBlock(itemUseContext);
        if (used != ActionResult.SUCCESS) {
            // try activating the block
            if (getWorld().getBlockState(blockPos).activate(world, fakePlayer, Hand.MAIN_HAND, rayTraceResult)) {
                used = ActionResult.SUCCESS;
            } else {
                used = stack.getItem().use(world, fakePlayer, Hand.MAIN_HAND).getResult();
            }
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
    private static Feature<? extends FeatureConfig> createTreeFeatureGenerator(TreeType type) {
        switch (type) {
            case TREE: return new OakTreeFeature(DefaultFeatureConfig::deserialize, true);
            case BIG_TREE: return new LargeOakTreeFeature(DefaultFeatureConfig::deserialize, true);
            case REDWOOD: return new PineTreeFeature(DefaultFeatureConfig::deserialize);
            case TALL_REDWOOD: return new SpruceTreeFeature(DefaultFeatureConfig::deserialize, true);
            case BIRCH: return new BirchTreeFeature(DefaultFeatureConfig::deserialize, true, false);
            case JUNGLE: return new MegaJungleTreeFeature(DefaultFeatureConfig::deserialize, true, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
            case SMALL_JUNGLE: return new JungleTreeFeature(DefaultFeatureConfig::deserialize, true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, false);
            case SHORT_JUNGLE: return new JungleTreeFeature(DefaultFeatureConfig::deserialize, true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true);
            case JUNGLE_BUSH: return new JungleGroundBushFeature(DefaultFeatureConfig::deserialize, JUNGLE_LOG, JUNGLE_SHRUB);
            case SWAMP: return new SwampTreeFeature(DefaultFeatureConfig::deserialize);
            case ACACIA: return new SavannaTreeFeature(DefaultFeatureConfig::deserialize, true);
            case DARK_OAK: return new DarkOakTreeFeature(DefaultFeatureConfig::deserialize, true);
            case MEGA_REDWOOD: return new MegaPineTreeFeature(DefaultFeatureConfig::deserialize, true, random.nextBoolean());
            case TALL_BIRCH: return new BirchTreeFeature(DefaultFeatureConfig::deserialize, true, true);
            case RED_MUSHROOM: return new HugeRedMushroomFeature(PlantedFeatureConfig::deserialize);
            case BROWN_MUSHROOM: return new HugeBrownMushroomFeature(PlantedFeatureConfig::deserialize);
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default:
                return null;
        }
    }

    private FeatureConfig createFeatureConfig(TreeType type) {
        if (type == TreeType.RED_MUSHROOM || type == TreeType.BROWN_MUSHROOM) {
            return new PlantedFeatureConfig(true);
        } else {
            return new DefaultFeatureConfig();
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        @SuppressWarnings("unchecked")
        Feature<FeatureConfig> generator = (Feature<FeatureConfig>) createTreeFeatureGenerator(type);
        return generator != null
                && generator.generate(getWorld(), getWorld().getChunkManager().getChunkGenerator(), random,
                FabricAdapter.toBlockPos(position), createFeatureConfig(type));
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
            world.getChunkManager().getLightingProvider().suppressLight(new ChunkPos(chunk.getBlockX(), chunk.getBlockZ()), true);
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

            createdEntity.setPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

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
