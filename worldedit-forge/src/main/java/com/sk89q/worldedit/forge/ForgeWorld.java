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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.forge.internal.ForgeWorldNativeAccess;
import com.sk89q.worldedit.forge.internal.NBTConverter;
import com.sk89q.worldedit.forge.internal.TileEntityUtils;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.util.BiomeMath;
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
import net.minecraft.data.worldgen.Features;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
public class ForgeWorld extends AbstractWorld {

    private static final Random random = new Random();

    private static ResourceLocation getDimensionRegistryKey(ServerLevel world) {
        return Objects.requireNonNull(world.getServer(), "server cannot be null")
            .registryAccess()
            .registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
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
        return world.getServer().storageSource.getDimensionPath(
            world.dimension()
        ).toPath();
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
    public boolean fullySupports3DBiomes() {
        BiomeZoomer okZoomer = getWorld().dimensionType().getBiomeZoomer();
        return !(okZoomer instanceof FuzzyOffsetConstantColumnBiomeZoomer);
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);

        LevelChunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4);
        return getBiomeInChunk(position, chunk);
    }

    private BiomeType getBiomeInChunk(BlockVector3 position, ChunkAccess chunk) {
        ChunkBiomeContainer biomes = checkNotNull(chunk.getBiomes());
        return ForgeAdapter.adapt(biomes.getNoiseBiome(position.getX() >> 2, position.getY() >> 2, position.getZ() >> 2));
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        LevelChunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4);
        ChunkBiomeContainer container = checkNotNull(chunk.getBiomes());
        int idx = BiomeMath.computeBiomeIndex(
            position.getX(), position.getY(), position.getZ(), getMinY(), getMaxY()
        );
        container.biomes[idx] = ForgeAdapter.adapt(biome);
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
        fakePlayer.absMoveTo(position.getBlockX(), position.getBlockY(), position.getBlockZ(),
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

        ItemEntity entity = new ItemEntity(getWorld(), position.getX(), position.getY(), position.getZ(), ForgeAdapter.adapt(item));
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
            WorldGenSettings originalOpts = levelProperties.worldGenSettings();

            long seed = options.getSeed().orElse(originalWorld.getSeed());
            WorldGenSettings newOpts = options.getSeed().isPresent()
                ? replaceSeed(originalWorld, seed, originalOpts)
                : originalOpts;

            levelProperties.worldGenSettings = newOpts;
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
                levelProperties.worldGenSettings = originalOpts;
            }
        } finally {
            SafeFiles.tryHardToDeleteDir(tempDir);
        }
    }

    private WorldGenSettings replaceSeed(ServerLevel originalWorld, long seed, WorldGenSettings originalOpts) {
        RegistryWriteOps<Tag> nbtReadRegOps = RegistryWriteOps.create(
            NbtOps.INSTANCE,
            originalWorld.getServer().registryAccess()
        );
        RegistryReadOps<Tag> nbtRegOps = RegistryReadOps.createAndLoad(
            NbtOps.INSTANCE,
            originalWorld.getServer().getResourceManager(),
            originalWorld.getServer().registryAccess()
        );
        Codec<WorldGenSettings> dimCodec = WorldGenSettings.CODEC;
        return dimCodec
            .encodeStart(nbtReadRegOps, originalOpts)
            .flatMap(tag ->
                dimCodec.parse(
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
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                blockEntity.save(tag);
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
        List<CompletableFuture<ChunkAccess>> chunkLoadings = new ArrayList<>();
        // Pre-gen all the chunks
        for (BlockVector2 chunk : region.getChunks()) {
            chunkLoadings.add(
                world.getChunkSource().getChunkFutureMainThread(chunk.getX(), chunk.getZ(), ChunkStatus.FEATURES, true)
                    .thenApply(either -> either.left().orElse(null))
            );
        }
        return chunkLoadings;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> createTreeFeatureGenerator(TreeType type) {
        switch (type) {
            case TREE: return Features.OAK;
            case BIG_TREE: return Features.FANCY_OAK;
            case REDWOOD: return Features.SPRUCE;
            case TALL_REDWOOD: return Features.MEGA_SPRUCE;
            case MEGA_REDWOOD: return Features.MEGA_PINE;
            case BIRCH: return Features.BIRCH;
            case JUNGLE: return Features.MEGA_JUNGLE_TREE;
            case SMALL_JUNGLE: return Features.JUNGLE_TREE;
            case SHORT_JUNGLE: return Features.JUNGLE_TREE_NO_VINE;
            case JUNGLE_BUSH: return Features.JUNGLE_BUSH;
            case SWAMP: return Features.SWAMP_OAK;
            case ACACIA: return Features.ACACIA;
            case DARK_OAK: return Features.DARK_OAK;
            case TALL_BIRCH: return Features.SUPER_BIRCH_BEES_0002;
            case RED_MUSHROOM: return Features.HUGE_RED_MUSHROOM;
            case BROWN_MUSHROOM: return Features.HUGE_BROWN_MUSHROOM;
            case WARPED_FUNGUS: return Features.WARPED_FUNGI;
            case CRIMSON_FUNGUS: return Features.CRIMSON_FUNGI;
            case CHORUS_PLANT: return Features.CHORUS_PLANT;
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) {
        ConfiguredFeature<?, ?> generator = createTreeFeatureGenerator(type);
        ServerLevel world = getWorld();
        ServerChunkCache chunkManager = world.getChunkSource();
        return generator != null && generator.place(
            world, chunkManager.getGenerator(), random, ForgeAdapter.toBlockPos(position)
        );
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
        ServerLevel world = getWorld();
        for (BlockVector2 chunk : chunks) {
            world.getChunkSource().getLightEngine().retainData(new ChunkPos(chunk.getBlockX(), chunk.getBlockZ()), true);
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
        return getWorld().getHeight() - 1;
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
                .getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4)
                .getBlockState(ForgeAdapter.toBlockPos(position));

        return ForgeAdapter.adapt(mcState);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
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
        } else if ((o instanceof ForgeWorld)) {
            ForgeWorld other = ((ForgeWorld) o);
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
            ForgeAdapter.toBlockPos(region.getMinimumPoint()),
            ForgeAdapter.toBlockPos(region.getMaximumPoint().add(BlockVector3.ONE))
        );
        List<net.minecraft.world.entity.Entity> nmsEntities = world.getEntities(
            (net.minecraft.world.entity.Entity) null,
            box,
            e -> region.contains(ForgeAdapter.adapt(e.blockPosition()))
        );
        return ImmutableList.copyOf(Lists.transform(
            nmsEntities,
            ForgeEntity::new
        ));
    }

    @Override
    public List<? extends Entity> getEntities() {
        final ServerLevel world = getWorld();
        return ImmutableList.copyOf(Iterables.transform(
            world.getAllEntities(),
            ForgeEntity::new
        ));
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        ServerLevel world = getWorld();
        final Optional<EntityType<?>> entityType = EntityType.byString(entity.getType().getId());
        if (!entityType.isPresent()) {
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
            return new ForgeEntity(createdEntity);
        } else {
            return null;
        }
    }

}
