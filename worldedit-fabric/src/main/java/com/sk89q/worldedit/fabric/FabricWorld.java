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

package com.sk89q.worldedit.fabric;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.util.concurrent.Futures;
import com.mojang.serialization.Dynamic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.fabric.internal.ExtendedMinecraftServer;
import com.sk89q.worldedit.fabric.internal.FabricWorldNativeAccess;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.fabric.mixin.AccessorPrimaryLevelData;
import com.sk89q.worldedit.fabric.mixin.AccessorServerChunkCache;
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
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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
public class FabricWorld extends AbstractWorld {

    private static final Random random = new Random();

    private static ResourceLocation getDimensionRegistryKey(Level world) {
        return Objects.requireNonNull(world.getServer(), "server cannot be null")
            .registryAccess()
            .registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
            .getKey(world.dimensionType());
    }

    private final WeakReference<Level> worldRef;
    private final FabricWorldNativeAccess worldNativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    FabricWorld(Level world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<>(world);
        this.worldNativeAccess = new FabricWorldNativeAccess(worldRef);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public Level getWorld() {
        Level world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        LevelData levelProperties = getWorld().getLevelData();
        return ((ServerLevelData) levelProperties).getLevelName();
    }

    @Override
    public String getId() {
        return getName() + "_" + getDimensionRegistryKey(getWorld());
    }

    @Override
    public Path getStoragePath() {
        final Level world = getWorld();
        MinecraftServer server = world.getServer();
        checkState(server instanceof ExtendedMinecraftServer, "Need a server world");
        return ((ExtendedMinecraftServer) server).getStoragePath(world);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        clearContainerBlockContents(position);
        return worldNativeAccess.setBlock(position, block, sideEffects);
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) {
        worldNativeAccess.applySideEffects(position, previousType, sideEffectSet);
        return Sets.intersection(FabricWorldEdit.inst.getPlatform().getSupportedSideEffects(), sideEffectSet.getSideEffectsToApply());
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);
        return getWorld().getMaxLocalRawBrightness(FabricAdapter.toBlockPos(position));
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);

        BlockEntity tile = getWorld().getBlockEntity(FabricAdapter.toBlockPos(position));
        if ((tile instanceof Clearable)) {
            ((Clearable) tile).clearContent();
            return true;
        }
        return false;
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);
        ChunkAccess chunk = getWorld().getChunk(position.getX() >> 4, position.getZ() >> 4);
        return getBiomeInChunk(position, chunk);
    }

    private BiomeType getBiomeInChunk(BlockVector3 position, ChunkAccess chunk) {
        return FabricAdapter.adapt(chunk.getNoiseBiome(position.getX() >> 2, position.getY() >> 2, position.getZ() >> 2));
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        ChunkAccess chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4);
        PalettedContainer<Biome> biomeArray = chunk.getSection(chunk.getSectionIndex(position.getY())).getBiomes();
        biomeArray.getAndSetUnchecked(
            position.getX() & 3, position.getY() & 3, position.getZ() & 3,
            FabricAdapter.adapt(biome)
        );
        chunk.setUnsaved(true);
        return true;
    }

    private static final LoadingCache<ServerLevel, WorldEditFakePlayer> fakePlayers
            = CacheBuilder.newBuilder().weakKeys().softValues().build(CacheLoader.from(WorldEditFakePlayer::new));

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        ItemStack stack = FabricAdapter.adapt(new BaseItemStack(item.getType(), item.getNbtReference(), 1));
        ServerLevel world = (ServerLevel) getWorld();
        final WorldEditFakePlayer fakePlayer;
        try {
            fakePlayer = fakePlayers.get(world);
        } catch (ExecutionException ignored) {
            return false;
        }
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);
        fakePlayer.absMoveTo(position.getBlockX(), position.getBlockY(), position.getBlockZ(),
                (float) face.toVector().toYaw(), (float) face.toVector().toPitch());
        final BlockPos blockPos = FabricAdapter.toBlockPos(position);
        final BlockHitResult rayTraceResult = new BlockHitResult(FabricAdapter.toVec3(position),
                FabricAdapter.adapt(face), blockPos, false);
        UseOnContext itemUseContext = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, rayTraceResult);
        InteractionResult used = stack.useOn(itemUseContext);
        if (used != InteractionResult.SUCCESS) {
            // try activating the block
            used = getWorld().getBlockState(blockPos).use(world, fakePlayer, InteractionHand.MAIN_HAND, rayTraceResult);
        }
        if (used != InteractionResult.SUCCESS) {
            used = stack.use(world, fakePlayer, InteractionHand.MAIN_HAND).getResult();
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

        ItemEntity entity = new ItemEntity(getWorld(), position.getX(), position.getY(), position.getZ(), FabricAdapter.adapt(item));
        entity.setPickUpDelay(10);
        getWorld().addFreshEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        BlockPos pos = FabricAdapter.toBlockPos(position);
        getWorld().destroyBlock(pos, true);
    }

    @Override
    public boolean canPlaceAt(BlockVector3 position, BlockState blockState) {
        return FabricAdapter.adapt(blockState).canSurvive(getWorld(), FabricAdapter.toBlockPos(position));
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        // Don't even try to regen if it's going to fail.
        ChunkSource provider = getWorld().getChunkSource();
        if (!(provider instanceof ServerChunkCache)) {
            return false;
        }

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
            ServerLevel originalWorld = (ServerLevel) getWorld();
            AccessorPrimaryLevelData levelProperties = (AccessorPrimaryLevelData)
                originalWorld.getLevelData();
            WorldGenSettings originalOpts = levelProperties.worldGenSettings();

            long seed = options.getSeed().orElse(originalWorld.getSeed());
            WorldGenSettings newOpts = options.getSeed().isPresent()
                ? replaceSeed(originalWorld, seed, originalOpts)
                : originalOpts;

            levelProperties.setWorldGenSettings(newOpts);
            ResourceKey<Level> worldRegKey = originalWorld.dimension();
            LevelStem dimGenOpts = newOpts.dimensions().get(worldRegKey.location());
            checkNotNull(dimGenOpts, "No DimensionOptions for %s", worldRegKey);
            try (ServerLevel serverWorld = new ServerLevel(
                originalWorld.getServer(), Util.backgroundExecutor(), session,
                ((ServerLevelData) originalWorld.getLevelData()),
                worldRegKey,
                originalWorld.dimensionType(),
                new WorldEditGenListener(),
                dimGenOpts.generator(),
                originalWorld.isDebug(),
                seed,
                // No spawners are needed for this world.
                ImmutableList.of(),
                // This controls ticking, we don't need it so set it to false.
                false
            )) {
                regenForWorld(region, extent, serverWorld, options);

                // drive the server executor until all tasks are popped off
                while (originalWorld.getServer().pollTask()) {
                    Thread.yield();
                }
            } finally {
                levelProperties.setWorldGenSettings(originalOpts);
            }
        } finally {
            SafeFiles.tryHardToDeleteDir(tempDir);
        }
    }

    private WorldGenSettings replaceSeed(ServerLevel originalWorld, long seed, WorldGenSettings originalOpts) {
        RegistryWriteOps<Tag> nbtRegReadOps = RegistryWriteOps.create(
            NbtOps.INSTANCE,
            originalWorld.getServer().registryAccess()
        );
        RegistryReadOps<Tag> nbtRegOps = RegistryReadOps.create(
            NbtOps.INSTANCE,
            ((ExtendedMinecraftServer) originalWorld.getServer())
                .getResources().getResourceManager(),
            originalWorld.getServer().registryAccess()
        );
        return WorldGenSettings.CODEC
            .encodeStart(nbtRegReadOps, originalOpts)
            .flatMap(tag ->
                WorldGenSettings.CODEC.parse(
                    recursivelySetSeed(new Dynamic<>(nbtRegOps, tag), seed, new HashSet<>())
                )
            )
            .get()
            .map(
                l -> l,
                error -> {
                    throw new IllegalStateException("Unable to map GeneratorOptions: " + error.message());
                }
            );
    }

    @SuppressWarnings("unchecked")
    private Dynamic<Tag> recursivelySetSeed(Dynamic<Tag> dynamic, long seed, Set<Dynamic<Tag>> seen) {
        if (!seen.add(dynamic)) {
            return dynamic;
        }
        return dynamic.updateMapValues(pair -> {
            if (pair.getFirst().asString("").equals("seed")) {
                return pair.mapSecond(v -> v.createLong(seed));
            }
            if (pair.getSecond().getValue() instanceof net.minecraft.nbt.CompoundTag) {
                return pair.mapSecond(v -> recursivelySetSeed((Dynamic<Tag>) v, seed, seen));
            }
            return pair;
        });
    }

    private void regenForWorld(Region region, Extent extent, ServerLevel serverWorld,
                               RegenOptions options) throws WorldEditException {
        List<CompletableFuture<ChunkAccess>> chunkLoadings = submitChunkLoadTasks(region, serverWorld);

        // drive executor until loading finishes
        ((AccessorServerChunkCache) serverWorld.getChunkSource()).getMainThreadProcessor()
            .managedBlock(() -> {
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
            BlockPos pos = FabricAdapter.toBlockPos(vec);
            ChunkAccess chunk = chunks.get(new ChunkPos(pos));
            BlockStateHolder<?> state = FabricAdapter.adapt(chunk.getBlockState(pos));
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity != null) {
                net.minecraft.nbt.CompoundTag tag = blockEntity.saveWithId();
                state = state.toBaseBlock(LazyReference.from(() -> NBTConverter.fromNative(tag)));
            }
            extent.setBlock(vec, state.toBaseBlock());

            if (options.shouldRegenBiomes()) {
                BiomeType biome = getBiomeInChunk(vec, chunk);
                extent.setBiome(vec, biome);
            }
        }
    }

    private List<CompletableFuture<ChunkAccess>> submitChunkLoadTasks(Region region, ServerLevel world) {
        AccessorServerChunkCache chunkManager = (AccessorServerChunkCache) world.getChunkSource();
        List<CompletableFuture<ChunkAccess>> chunkLoadings = new ArrayList<>();
        // Pre-gen all the chunks
        for (BlockVector2 chunk : region.getChunks()) {
            chunkLoadings.add(
                chunkManager.callGetChunkFuture(chunk.getX(), chunk.getZ(), ChunkStatus.FEATURES, true)
                    .thenApply(either -> either.left().orElse(null))
            );
        }
        return chunkLoadings;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> createTreeFeatureGenerator(TreeType type) {
        return switch (type) {
            // Based off of the SaplingGenerator class, as well as uses of DefaultBiomeFeatures fields
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
            case RANDOM -> createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default -> null;
        };
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) {
        ConfiguredFeature<?, ?> generator = createTreeFeatureGenerator(type);
        ServerLevel world = (ServerLevel) getWorld();
        ServerChunkCache chunkManager = world.getChunkSource();
        if (type == TreeType.CHORUS_PLANT) {
            position = position.add(0, 1, 0);
        }
        return generator != null && generator.place(
            world, chunkManager.getGenerator(), random,
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
        Level world = getWorld();
        for (BlockVector2 chunk : chunks) {
            world.getChunkSource().getLightEngine().enableLightSources(
                new ChunkPos(chunk.getBlockX(), chunk.getBlockZ()), true
            );
        }
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        // TODO update sound API
        // getWorld().playSound(type, FabricAdapter.toBlockPos(position.toBlockPoint()), data);
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
        LevelData worldProps = getWorld().getLevelData();
        return BlockVector3.at(
            worldProps.getXSpawn(),
            worldProps.getYSpawn(),
            worldProps.getZSpawn()
        );
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        net.minecraft.world.level.block.state.BlockState mcState = getWorld()
                .getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4)
                .getBlockState(FabricAdapter.toBlockPos(position));

        return FabricAdapter.adapt(mcState);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        // Avoid creation by using the CHECK mode -- if it's needed, it'll be re-created anyways
        BlockEntity tile = ((LevelChunk) getWorld().getChunk(pos)).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);

        if (tile != null) {
            net.minecraft.nbt.CompoundTag tag = tile.saveWithId();
            return getBlock(position).toBaseBlock(LazyReference.from(() -> NBTConverter.fromNative(tag)));
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
        if ((o instanceof FabricWorld other)) {
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
        final Level world = getWorld();
        AABB box = new AABB(
            FabricAdapter.toBlockPos(region.getMinimumPoint()),
            FabricAdapter.toBlockPos(region.getMaximumPoint().add(BlockVector3.ONE))
        );
        List<net.minecraft.world.entity.Entity> nmsEntities = world.getEntities(
            (net.minecraft.world.entity.Entity) null,
            box,
            e -> region.contains(FabricAdapter.adapt(e.blockPosition()))
        );
        return nmsEntities.stream()
            .map(FabricEntity::new)
            .collect(ImmutableList.toImmutableList());
    }

    @Override
    public List<? extends Entity> getEntities() {
        final Level world = getWorld();
        if (!(world instanceof ServerLevel)) {
            return Collections.emptyList();
        }
        return Streams.stream(((ServerLevel) world).getAllEntities())
            .map(FabricEntity::new)
            .collect(ImmutableList.toImmutableList());
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        Level world = getWorld();
        final Optional<EntityType<?>> entityType = EntityType.byString(entity.getType().getId());
        if (entityType.isEmpty()) {
            return null;
        }
        net.minecraft.world.entity.Entity createdEntity = entityType.get().create(world);
        if (createdEntity != null) {
            CompoundBinaryTag nativeTag = entity.getNbt();
            if (nativeTag != null) {
                net.minecraft.nbt.CompoundTag tag = NBTConverter.toNative(nativeTag);
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.remove(name);
                }
                createdEntity.load(tag);
            }

            createdEntity.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            world.addFreshEntity(createdEntity);
            return new FabricEntity(createdEntity);
        } else {
            return null;
        }
    }

}
