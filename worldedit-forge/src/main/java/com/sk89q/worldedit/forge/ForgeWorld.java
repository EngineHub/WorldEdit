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
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.ServerChunkProvider;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.feature.BigBrownMushroomFeature;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.BigRedMushroomFeature;
import net.minecraft.world.gen.feature.BigTreeFeature;
import net.minecraft.world.gen.feature.BirchTreeFeature;
import net.minecraft.world.gen.feature.DarkOakTreeFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.JungleTreeFeature;
import net.minecraft.world.gen.feature.MegaJungleFeature;
import net.minecraft.world.gen.feature.MegaPineTree;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.PointyTaigaTreeFeature;
import net.minecraft.world.gen.feature.SavannaTreeFeature;
import net.minecraft.world.gen.feature.ShrubFeature;
import net.minecraft.world.gen.feature.SwampTreeFeature;
import net.minecraft.world.gen.feature.TallTaigaTreeFeature;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

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
    public Path getStoragePath() {
        final World world = getWorld();
        if (world instanceof ServerWorld) {
            return ((ServerWorld) world).getSaveHandler().getWorldDirectory().toPath();
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
        net.minecraft.block.BlockState newState = stateId.isPresent() ? Block.getStateById(stateId.getAsInt()) : ForgeAdapter.adapt(block.toImmutableState());
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

        if (successful && notifyAndLight) {
            world.getChunkProvider().getLightManager().checkBlock(pos);
            world.markAndNotifyBlock(pos, chunk, old, newState, UPDATE | NOTIFY);
        }

        return successful;
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        getWorld().notifyBlockUpdate(pos, ForgeAdapter.adapt(previousType), getWorld().getBlockState(pos), 1 | 2);
        return true;
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
        return ForgeAdapter.adapt(getWorld().getBiomeBody(new BlockPos(position.getBlockX(), 0, position.getBlockZ())));
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        IChunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        }
        chunk.getBiomes()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = ForgeAdapter.adapt(biome);
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
            if (getWorld().getBlockState(blockPos).onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, rayTraceResult)) {
                used = ActionResultType.SUCCESS;
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
    private static Feature<? extends IFeatureConfig> createTreeFeatureGenerator(TreeType type) {
        switch (type) {
            case TREE: return new TreeFeature(NoFeatureConfig::deserialize, true);
            case BIG_TREE: return new BigTreeFeature(NoFeatureConfig::deserialize, true);
            case REDWOOD: return new PointyTaigaTreeFeature(NoFeatureConfig::deserialize);
            case TALL_REDWOOD: return new TallTaigaTreeFeature(NoFeatureConfig::deserialize, true);
            case BIRCH: return new BirchTreeFeature(NoFeatureConfig::deserialize, true, false);
            case JUNGLE: return new MegaJungleFeature(NoFeatureConfig::deserialize, true, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
            case SMALL_JUNGLE: return new JungleTreeFeature(NoFeatureConfig::deserialize, true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, false);
            case SHORT_JUNGLE: return new JungleTreeFeature(NoFeatureConfig::deserialize, true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true);
            case JUNGLE_BUSH: return new ShrubFeature(NoFeatureConfig::deserialize, JUNGLE_LOG, JUNGLE_SHRUB);
            case SWAMP: return new SwampTreeFeature(NoFeatureConfig::deserialize);
            case ACACIA: return new SavannaTreeFeature(NoFeatureConfig::deserialize, true);
            case DARK_OAK: return new DarkOakTreeFeature(NoFeatureConfig::deserialize, true);
            case MEGA_REDWOOD: return new MegaPineTree(NoFeatureConfig::deserialize, true, random.nextBoolean());
            case TALL_BIRCH: return new BirchTreeFeature(NoFeatureConfig::deserialize, true, true);
            case RED_MUSHROOM: return new BigRedMushroomFeature(BigMushroomFeatureConfig::deserialize);
            case BROWN_MUSHROOM: return new BigBrownMushroomFeature(BigMushroomFeatureConfig::deserialize);
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default:
                return null;
        }
    }

    private IFeatureConfig createFeatureConfig(TreeType type) {
        if (type == TreeType.RED_MUSHROOM || type == TreeType.BROWN_MUSHROOM) {
            return new BigMushroomFeatureConfig(true);
        } else {
            return new NoFeatureConfig();
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        @SuppressWarnings("unchecked")
        Feature<IFeatureConfig> generator = (Feature<IFeatureConfig>) createTreeFeatureGenerator(type);
        return generator != null
                && generator.place(getWorld(), getWorld().getChunkProvider().getChunkGenerator(), random,
                ForgeAdapter.toBlockPos(position), createFeatureConfig(type));
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
            world.getChunkProvider().getLightManager().func_215571_a(new ChunkPos(chunk.getBlockX(), chunk.getBlockZ()), true);
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
        public void func_219509_a(ChunkPos chunkPos) {
        }

        @Override
        public void func_219508_a(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        }

        @Override
        public void func_219510_b() {
        }
    }
}
