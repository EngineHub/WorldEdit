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

package com.sk89q.worldedit.forge;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.util.concurrent.Futures;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.forge.internal.ForgeEntity;
import com.sk89q.worldedit.forge.internal.ForgeServerLevelDelegateProxy;
import com.sk89q.worldedit.forge.internal.ForgeWorldNativeAccess;
import com.sk89q.worldedit.forge.internal.NBTConverter;
import com.sk89q.worldedit.forge.internal.TileEntityUtils;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

    private static final RandomSource random = RandomSource.create();

    private static ResourceLocation getDimensionRegistryKey(ServerLevel world) {
        return Objects.requireNonNull(world.getServer(), "server cannot be null")
            .registryAccess()
            .registryOrThrow(Registries.DIMENSION_TYPE)
            .getKey(world.dimensionType());
    }

    private final WeakReference<ServerLevel> worldRef;
    private final ForgeWorldNativeAccess nativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    ForgeWorld(ServerLevel world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<>(world);
        this.nativeAccess = new ForgeWorldNativeAccess(worldRef);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public ServerLevel getWorld() {
        ServerLevel world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return ((ServerLevelData) getWorld().getLevelData()).getLevelName();
    }

    @Override
    public String getId() {
        return getName() + "_" + getDimensionRegistryKey(getWorld());
    }

    @Override
    public Path getStoragePath() {
        final ServerLevel world = getWorld();
        return world.getServer().storageSource.getDimensionPath(world.dimension());
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        clearContainerBlockContents(position);
        return nativeAccess.setBlock(position, block, sideEffects);
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) {
        nativeAccess.applySideEffects(position, previousType, sideEffectSet);
        return Sets.intersection(ForgeWorldEdit.inst.getPlatform().getSupportedSideEffects(), sideEffectSet.getSideEffectsToApply());
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);
        return getWorld().getLightEmission(ForgeAdapter.toBlockPos(position));
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);

        BlockEntity tile = getWorld().getBlockEntity(ForgeAdapter.toBlockPos(position));
        if (tile instanceof Clearable) {
            ((Clearable) tile).clearContent();
            return true;
        }
        return false;
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);

        LevelChunk chunk = getWorld().getChunk(position.x() >> 4, position.z() >> 4);
        return getBiomeInChunk(position, chunk);
    }

    private BiomeType getBiomeInChunk(BlockVector3 position, ChunkAccess chunk) {
        return ForgeAdapter.adapt(
            chunk.getNoiseBiome(position.x() >> 2, position.y() >> 2, position.z() >> 2).value()
        );
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        LevelChunk chunk = getWorld().getChunk(position.x() >> 4, position.z() >> 4);
        var biomes = (PalettedContainer<Holder<Biome>>) chunk.getSection(chunk.getSectionIndex(position.y())).getBiomes();
        biomes.getAndSetUnchecked(
            position.x() & 3, position.y() & 3, position.z() & 3,
            getWorld().registryAccess().registry(Registries.BIOME)
                .orElseThrow()
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(biome.id())))
        );
        chunk.setUnsaved(true);
        return true;
    }

    private static final LoadingCache<ServerLevel, WorldEditFakePlayer> fakePlayers
            = CacheBuilder.newBuilder().weakKeys().softValues().build(CacheLoader.from(WorldEditFakePlayer::new));

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        ItemStack stack = ForgeAdapter.adapt(new BaseItemStack(item.getType(), item.getNbtReference(), 1));
        ServerLevel world = getWorld();
        final WorldEditFakePlayer fakePlayer;
        try {
            fakePlayer = fakePlayers.get(world);
        } catch (ExecutionException ignored) {
            return false;
        }
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);
        fakePlayer.absMoveTo(position.x(), position.y(), position.z(),
                (float) face.toVector().toYaw(), (float) face.toVector().toPitch());
        final BlockPos blockPos = ForgeAdapter.toBlockPos(position);
        final BlockHitResult rayTraceResult = new BlockHitResult(ForgeAdapter.toVec3(position),
                ForgeAdapter.adapt(face), blockPos, false);
        UseOnContext itemUseContext = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, rayTraceResult);
        InteractionResult used = stack.onItemUseFirst(itemUseContext);
        if (used != InteractionResult.SUCCESS) {
            // try activating the block
            InteractionResult resultType = getWorld().getBlockState(blockPos)
                .use(world, fakePlayer, InteractionHand.MAIN_HAND, rayTraceResult);
            if (resultType.consumesAction()) {
                used = resultType;
            } else {
                used = stack.getItem().use(world, fakePlayer, InteractionHand.MAIN_HAND).getResult();
            }
        }
        return used == InteractionResult.SUCCESS;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        ItemEntity entity = new ItemEntity(getWorld(), position.x(), position.y(), position.z(), ForgeAdapter.adapt(item));
        entity.setPickUpDelay(10);
        getWorld().addFreshEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        BlockPos pos = ForgeAdapter.toBlockPos(position);
        getWorld().destroyBlock(pos, true);
    }

    @Override
    public boolean canPlaceAt(BlockVector3 position, BlockState blockState) {
        return ForgeAdapter.adapt(blockState).canSurvive(getWorld(), ForgeAdapter.toBlockPos(position));
    }

    // For unmapped regen names, see Fabric!

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        try {
            doRegen(region, extent, options);
        } catch (Exception e) {
            throw new IllegalStateException("Regen failed", e);
        }

        return true;
    }

    private void doRegen(Region region, Extent extent, RegenOptions options) throws Exception {
        Path tempDir = Files.createTempDirectory("WorldEditWorldGen");
        LevelStorageSource levelStorage = LevelStorageSource.createDefault(tempDir);
        try (LevelStorageSource.LevelStorageAccess session = levelStorage.createAccess("WorldEditTempGen")) {
            ServerLevel originalWorld = getWorld();
            PrimaryLevelData levelProperties = (PrimaryLevelData) originalWorld.getServer()
                .getWorldData().overworldData();
            WorldOptions originalOpts = levelProperties.worldGenOptions();

            long seed = options.getSeed().orElse(originalWorld.getSeed());
            WorldOptions newOpts = options.getSeed().isPresent()
                ? originalOpts.withSeed(OptionalLong.of(seed))
                : originalOpts;

            levelProperties.worldOptions = newOpts;
            ResourceKey<Level> worldRegKey = originalWorld.dimension();
            try (ServerLevel serverWorld = new ServerLevel(
                originalWorld.getServer(), Util.backgroundExecutor(), session,
                ((ServerLevelData) originalWorld.getLevelData()),
                worldRegKey,
                new LevelStem(
                    originalWorld.dimensionTypeRegistration(),
                    originalWorld.getChunkSource().getGenerator()
                ),
                new WorldEditGenListener(),
                originalWorld.isDebug(),
                seed,
                // No spawners are needed for this world.
                ImmutableList.of(),
                // This controls ticking, we don't need it so set it to false.
                false,
                originalWorld.getRandomSequences()
            )) {
                regenForWorld(region, extent, serverWorld, options);

                // drive the server executor until all tasks are popped off
                while (originalWorld.getServer().pollTask()) {
                    Thread.yield();
                }
            } finally {
                levelProperties.worldOptions = originalOpts;
            }
        } finally {
            SafeFiles.tryHardToDeleteDir(tempDir);
        }
    }

    private void regenForWorld(Region region, Extent extent, ServerLevel serverWorld,
                               RegenOptions options) throws WorldEditException {
        List<CompletableFuture<ChunkAccess>> chunkLoadings = submitChunkLoadTasks(region, serverWorld);

        // drive executor until loading finishes
        BlockableEventLoop<Runnable> executor = serverWorld.getChunkSource().mainThreadProcessor;
        executor.managedBlock(() -> {
            // bail out early if a future fails
            if (chunkLoadings.stream().anyMatch(ftr ->
                ftr.isDone() && Futures.getUnchecked(ftr) == null
            )) {
                return false;
            }
            return chunkLoadings.stream().allMatch(CompletableFuture::isDone);
        });

        Map<ChunkPos, ChunkAccess> chunks = new HashMap<>();
        for (CompletableFuture<ChunkAccess> future : chunkLoadings) {
            @Nullable
            ChunkAccess chunk = future.getNow(null);
            checkState(chunk != null, "Failed to generate a chunk, regen failed.");
            chunks.put(chunk.getPos(), chunk);
        }

        for (BlockVector3 vec : region) {
            BlockPos pos = ForgeAdapter.toBlockPos(vec);
            ChunkAccess chunk = chunks.get(new ChunkPos(pos));
            BlockStateHolder<?> state = ForgeAdapter.adapt(chunk.getBlockState(pos));
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity != null) {
                state = state.toBaseBlock(LazyReference.from(() -> NBTConverter.fromNative(blockEntity.saveWithFullMetadata())));
            }
            extent.setBlock(vec, state.toBaseBlock());

            if (options.shouldRegenBiomes()) {
                BiomeType biome = getBiomeInChunk(vec, chunk);
                extent.setBiome(vec, biome);
            }
        }
    }

    private List<CompletableFuture<ChunkAccess>> submitChunkLoadTasks(Region region, ServerLevel world) {
        List<CompletableFuture<ChunkAccess>> chunkLoadings = new ArrayList<>();
        // Pre-gen all the chunks
        for (BlockVector2 chunk : region.getChunks()) {
            chunkLoadings.add(
                world.getChunkSource().getChunkFutureMainThread(chunk.x(), chunk.z(), ChunkStatus.FEATURES, true)
                    .thenApply(either -> either.left().orElse(null))
            );
        }
        return chunkLoadings;
    }

    @Nullable
    private static ResourceKey<ConfiguredFeature<?, ?>> createTreeFeatureGenerator(TreeType type) {
        return switch (type) {
            case TREE -> TreeFeatures.OAK;
            case BIG_TREE -> TreeFeatures.FANCY_OAK;
            case REDWOOD -> TreeFeatures.SPRUCE;
            case TALL_REDWOOD -> TreeFeatures.MEGA_SPRUCE;
            case MEGA_REDWOOD -> TreeFeatures.MEGA_PINE;
            case BIRCH -> TreeFeatures.BIRCH;
            case JUNGLE -> TreeFeatures.MEGA_JUNGLE_TREE;
            case SMALL_JUNGLE -> TreeFeatures.JUNGLE_TREE;
            case SHORT_JUNGLE -> TreeFeatures.JUNGLE_TREE_NO_VINE;
            case JUNGLE_BUSH -> TreeFeatures.JUNGLE_BUSH;
            case SWAMP -> TreeFeatures.SWAMP_OAK;
            case ACACIA -> TreeFeatures.ACACIA;
            case DARK_OAK -> TreeFeatures.DARK_OAK;
            case TALL_BIRCH -> TreeFeatures.SUPER_BIRCH_BEES_0002;
            case RED_MUSHROOM -> TreeFeatures.HUGE_RED_MUSHROOM;
            case BROWN_MUSHROOM -> TreeFeatures.HUGE_BROWN_MUSHROOM;
            case WARPED_FUNGUS -> TreeFeatures.WARPED_FUNGUS;
            case CRIMSON_FUNGUS -> TreeFeatures.CRIMSON_FUNGUS;
            case CHORUS_PLANT -> EndFeatures.CHORUS_PLANT;
            case MANGROVE -> TreeFeatures.MANGROVE;
            case TALL_MANGROVE -> TreeFeatures.TALL_MANGROVE;
            case CHERRY -> TreeFeatures.CHERRY;
            case RANDOM -> createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default -> null;
        };
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) {
        ServerLevel world = getWorld();
        ConfiguredFeature<?, ?> generator = Optional.ofNullable(createTreeFeatureGenerator(type))
            .map(k -> world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(k))
            .orElse(null);
        ServerChunkCache chunkManager = world.getChunkSource();
        if (type == TreeType.CHORUS_PLANT) {
            position = position.add(0, 1, 0);
        }
        WorldGenLevel levelProxy = ForgeServerLevelDelegateProxy.newInstance(editSession, world);
        return generator != null && generator.place(
            levelProxy, chunkManager.getGenerator(), random, ForgeAdapter.toBlockPos(position)
        );
    }

    public boolean generateFeature(ConfiguredFeatureType type, EditSession editSession, BlockVector3 position) {
        ServerLevel world = getWorld();
        ConfiguredFeature<?, ?> k = world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(ResourceLocation.tryParse(type.id()));
        ServerChunkCache chunkManager = world.getChunkSource();
        WorldGenLevel levelProxy = ForgeServerLevelDelegateProxy.newInstance(editSession, world);
        return k != null && k.place(levelProxy, chunkManager.getGenerator(), random, ForgeAdapter.toBlockPos(position));
    }

    @Override
    public boolean generateStructure(StructureType type, EditSession editSession, BlockVector3 position) {
        ServerLevel world = getWorld();
        Structure k = world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.tryParse(type.id()));
        if (k == null) {
            return false;
        }

        ServerChunkCache chunkManager = world.getChunkSource();
        WorldGenLevel proxyLevel = ForgeServerLevelDelegateProxy.newInstance(editSession, world);
        ChunkPos chunkPos = new ChunkPos(new BlockPos(position.x(), position.y(), position.z()));
        StructureStart structureStart = k.generate(world.registryAccess(), chunkManager.getGenerator(), chunkManager.getGenerator().getBiomeSource(), chunkManager.randomState(), world.getStructureManager(), world.getSeed(), chunkPos, 0, proxyLevel, biome -> true);

        if (!structureStart.isValid()) {
            return false;
        } else {
            BoundingBox boundingBox = structureStart.getBoundingBox();
            ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
            ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
            ChunkPos.rangeClosed(min, max).forEach((chunkPosx) -> structureStart.placeInChunk(proxyLevel, world.structureManager(), chunkManager.getGenerator(), world.getRandom(), new BoundingBox(chunkPosx.getMinBlockX(), world.getMinBuildHeight(), chunkPosx.getMinBlockZ(), chunkPosx.getMaxBlockX(), world.getMaxBuildHeight(), chunkPosx.getMaxBlockZ()), chunkPosx));
            return true;
        }
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
    public void sendBiomeUpdates(Iterable<BlockVector2> chunks) {
        List<ChunkAccess> nativeChunks = chunks instanceof Collection<BlockVector2> chunkCollection ? Lists.newArrayListWithCapacity(chunkCollection.size()) : Lists.newArrayList();
        for (BlockVector2 chunk : chunks) {
            nativeChunks.add(getWorld().getChunk(chunk.x(), chunk.z(), ChunkStatus.BIOMES, false));
        }
        ((ServerLevel) getWorld()).getChunkSource().chunkMap.resendBiomesForChunks(nativeChunks);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {
        ServerLevel world = getWorld();
        for (BlockVector2 chunk : chunks) {
            // Fetch the chunk after light initialization at least
            // We'll be doing a full relight anyways, so we don't need to be LIGHT yet
            world.getChunkSource().getLightEngine().lightChunk(world.getChunk(
                chunk.x(), chunk.z(), ChunkStatus.INITIALIZE_LIGHT
            ), false);
        }
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        // TODO update sound API
        // getWorld().play(type, ForgeAdapter.toBlockPos(position.toBlockPoint()), data);
        return true;
    }

    @Override
    public WeatherType getWeather() {
        LevelData info = getWorld().getLevelData();
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
        ServerLevelData info = (ServerLevelData) getWorld().getLevelData();
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
        ServerLevelData info = (ServerLevelData) getWorld().getLevelData();
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
    public int getMinY() {
        return getWorld().getMinBuildHeight();
    }

    @Override
    public int getMaxY() {
        return getWorld().getMaxBuildHeight() - 1;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        LevelData worldInfo = getWorld().getLevelData();
        return BlockVector3.at(
            worldInfo.getXSpawn(),
            worldInfo.getYSpawn(),
            worldInfo.getZSpawn()
        );
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        net.minecraft.world.level.block.state.BlockState mcState = getWorld()
                .getChunk(position.x() >> 4, position.z() >> 4)
                .getBlockState(ForgeAdapter.toBlockPos(position));

        return ForgeAdapter.adapt(mcState);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockPos pos = new BlockPos(position.x(), position.y(), position.z());
        BlockEntity tile = getWorld().getChunk(pos).getBlockEntity(pos);

        if (tile != null) {
            net.minecraft.nbt.CompoundTag tag = TileEntityUtils.copyNbtData(tile);
            return getBlock(position).toBaseBlock(
                LazyReference.from(() -> NBTConverter.fromNative(tag))
            );
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
        } else if ((o instanceof ForgeWorld other)) {
            Level otherWorld = other.worldRef.get();
            Level thisWorld = worldRef.get();
            return otherWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        final ServerLevel world = getWorld();
        AABB box = new AABB(
            ForgeAdapter.toVec3(region.getMinimumPoint()),
            ForgeAdapter.toVec3(region.getMaximumPoint().add(BlockVector3.ONE))
        );
        List<net.minecraft.world.entity.Entity> nmsEntities = world.getEntities(
            (net.minecraft.world.entity.Entity) null,
            box,
            e -> region.contains(ForgeAdapter.adapt(e.blockPosition()))
        );
        return nmsEntities.stream().map(ForgeEntity::new).collect(ImmutableList.toImmutableList());
    }

    @Override
    public List<? extends Entity> getEntities() {
        final ServerLevel world = getWorld();
        return Streams.stream(world.getAllEntities())
            .map(ForgeEntity::new)
            .collect(ImmutableList.toImmutableList());
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        ServerLevel world = getWorld();
        String entityId = entity.getType().id();
        final Optional<EntityType<?>> entityType = EntityType.byString(entityId);
        if (entityType.isEmpty()) {
            return null;
        }
        LinCompoundTag nativeTag = entity.getNbt();
        net.minecraft.nbt.CompoundTag tag;
        if (nativeTag != null) {
            tag = NBTConverter.toNative(nativeTag);
            removeUnwantedEntityTagsRecursively(tag);
        } else {
            tag = new net.minecraft.nbt.CompoundTag();
        }
        tag.putString("id", entityId);

        net.minecraft.world.entity.Entity createdEntity = EntityType.loadEntityRecursive(tag, world, (loadedEntity) -> {
            loadedEntity.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            return loadedEntity;
        });
        if (createdEntity != null) {
            world.addFreshEntityWithPassengers(createdEntity);
            return new ForgeEntity(createdEntity);
        }
        return null;
    }

    private void removeUnwantedEntityTagsRecursively(net.minecraft.nbt.CompoundTag tag) {
        for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
            tag.remove(name);
        }

        // Adapted from net.minecraft.world.entity.EntityType#loadEntityRecursive
        if (tag.contains("Passengers", LinTagId.LIST.id())) {
            net.minecraft.nbt.ListTag nbttaglist = tag.getList("Passengers", LinTagId.COMPOUND.id());

            for (int i = 0; i < nbttaglist.size(); ++i) {
                removeUnwantedEntityTagsRecursively(nbttaglist.getCompound(i));
            }
        }
    }

    @Override
    public Mask createLiquidMask() {
        return new AbstractExtentMask(this) {
            @Override
            public boolean test(BlockVector3 vector) {
                return ForgeAdapter.adapt(getExtent().getBlock(vector)).getBlock() instanceof LiquidBlock;
            }

            @Nullable
            @Override
            public Mask2D toMask2D() {
                return null;
            }
        };
    }
}
