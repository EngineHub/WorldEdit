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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.mojang.serialization.Dynamic;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.fabric.internal.ExtendedMinecraftServer;
import com.sk89q.worldedit.fabric.internal.FabricWorldNativeAccess;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.fabric.mixin.AccessorLevelProperties;
import com.sk89q.worldedit.fabric.mixin.AccessorServerChunkManager;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccessType;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.HorizontalVoronoiBiomeAccessType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class FabricWorld extends AbstractWorld {

    private static final Random random = new Random();

    private final WeakReference<World> worldRef;
    private final FabricWorldNativeAccess worldNativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    FabricWorld(World world) {
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
        WorldProperties levelProperties = getWorld().getLevelProperties();
        return ((ServerWorldProperties) levelProperties).getLevelName();
    }

    @Override
    public String getId() {
        return getName() + "_" + getWorld().getDimensionRegistryKey().getValue();
    }

    @Override
    public Path getStoragePath() {
        final World world = getWorld();
        MinecraftServer server = world.getServer();
        checkState(server instanceof ExtendedMinecraftServer, "Need a server world");
        return ((ExtendedMinecraftServer) server).getStoragePath(world);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
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
    public boolean fullySupports3DBiomes() {
        BiomeAccessType biomeAccessType = getWorld().getDimension().getBiomeAccessType();
        return !(biomeAccessType instanceof HorizontalVoronoiBiomeAccessType);
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);
        Chunk chunk = getWorld().getChunk(position.getX() >> 4, position.getZ() >> 4);
        BiomeArray biomeArray = checkNotNull(chunk.getBiomeArray());
        return FabricAdapter.adapt(biomeArray.getBiomeForNoiseGen(position.getX() >> 2, position.getY() >> 2, position.getZ() >> 2));
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        Chunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4);
        setBiomeInChunk(position, FabricAdapter.adapt(biome), chunk);
        return true;
    }

    private void setBiomeInChunk(BlockVector3 position, Biome biome, Chunk chunk) {
        MutableBiomeArray biomeArray = MutableBiomeArray.inject(checkNotNull(chunk.getBiomeArray()));
        biomeArray.setBiome(position.getX(), position.getY(), position.getZ(), biome);
        chunk.setShouldSave(true);
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
    public boolean regenerate(Region region, EditSession editSession, RegenOptions options) {
        // Don't even try to regen if it's going to fail.
        ChunkManager provider = getWorld().getChunkManager();
        if (!(provider instanceof ServerChunkManager)) {
            return false;
        }

        try {
            doRegen(region, editSession, options);
        } catch (Exception e) {
            throw new IllegalStateException("Regen failed", e);
        }

        return true;
    }

    private void doRegen(Region region, EditSession editSession, RegenOptions options) throws Exception {
        Path tempDir = Files.createTempDirectory("WorldEditWorldGen");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException ignored) {
            }
        }));
        LevelStorage levelStorage = LevelStorage.create(tempDir);
        try (LevelStorage.Session session = levelStorage.createSession("WorldEditTempGen")) {
            ServerWorld originalWorld = (ServerWorld) getWorld();
            long seed = options.getSeed().orElse(originalWorld.getSeed());
            AccessorLevelProperties levelProperties = (AccessorLevelProperties)
                originalWorld.getServer().getSaveProperties();
            GeneratorOptions originalOpts = levelProperties.getGeneratorOptions();

            GeneratorOptions newOpts = GeneratorOptions.CODEC
                .encodeStart(NbtOps.INSTANCE, originalOpts)
                .flatMap(tag ->
                    GeneratorOptions.CODEC.parse(
                        recursivelySetSeed(new Dynamic<>(NbtOps.INSTANCE, tag), seed, new HashSet<>())
                    )
                )
                .result()
                .orElseThrow(() -> new IllegalStateException("Unable to map GeneratorOptions"));

            levelProperties.setGeneratorOptions(newOpts);
            RegistryKey<World> worldRegKey = originalWorld.getRegistryKey();
            DimensionOptions dimGenOpts = newOpts.getDimensionMap().get(worldRegKey.getValue());
            checkNotNull(dimGenOpts, "No DimensionOptions for %s", worldRegKey);
            try (ServerWorld serverWorld = new ServerWorld(
                originalWorld.getServer(), Util.getServerWorkerExecutor(), session,
                ((ServerWorldProperties) originalWorld.getLevelProperties()),
                worldRegKey,
                originalWorld.getDimensionRegistryKey(),
                originalWorld.getDimension(),
                new WorldEditGenListener(),
                dimGenOpts.getChunkGenerator(),
                originalWorld.isDebugWorld(),
                seed,
                // No spawners are needed for this world.
                ImmutableList.of(),
                // This controls ticking, we don't need it so set it to false.
                false
            )) {
                regenForWorld(region, editSession, originalWorld, serverWorld, options);

                // drive the server executor until all tasks are popped off
                while (originalWorld.getServer().runTask()) {
                    Thread.yield();
                }
            } finally {
                levelProperties.setGeneratorOptions(originalOpts);
            }
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
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

    private void regenForWorld(Region region, EditSession editSession, ServerWorld originalWorld,
                               ServerWorld serverWorld, RegenOptions options) throws MaxChangedBlocksException {
        List<CompletableFuture<Chunk>> chunkLoadings = submitChunkLoadTasks(region, serverWorld);

        // drive executor until loading finishes
        ((AccessorServerChunkManager) serverWorld.getChunkManager()).getMainThreadExecutor()
            .runTasks(() -> {
                // bail out early if a future fails
                if (chunkLoadings.stream().anyMatch(ftr ->
                    ftr.isDone() && Futures.getUnchecked(ftr) == null
                )) {
                    return false;
                }
                return chunkLoadings.stream().allMatch(CompletableFuture::isDone);
            });

        Map<ChunkPos, Chunk> chunks = new HashMap<>();
        for (CompletableFuture<Chunk> future : chunkLoadings) {
            @Nullable
            Chunk chunk = future.getNow(null);
            checkState(chunk != null, "Failed to generate a chunk, regen failed.");
            chunks.put(chunk.getPos(), chunk);
        }

        for (BlockVector3 vec : region) {
            BlockPos pos = FabricAdapter.toBlockPos(vec);
            Chunk chunk = chunks.get(new ChunkPos(pos));
            BlockStateHolder<?> state = FabricAdapter.adapt(chunk.getBlockState(pos));
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity != null) {
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                blockEntity.toTag(tag);
                state = state.toBaseBlock(NBTConverter.fromNative(tag));
            }
            editSession.setBlock(vec, state);

            if (options.isRegenBiomes()) {
                setBiomeInChunk(
                    vec,
                    checkNotNull(chunk.getBiomeArray())
                        .getBiomeForNoiseGen(vec.getX() >> 2, vec.getY() >> 2, vec.getZ() >> 2),
                    originalWorld.getChunk(pos)
                );
            }
        }
    }

    private List<CompletableFuture<Chunk>> submitChunkLoadTasks(Region region, ServerWorld world) {
        AccessorServerChunkManager chunkManager = (AccessorServerChunkManager) world.getChunkManager();
        List<CompletableFuture<Chunk>> chunkLoadings = new ArrayList<>();
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
        switch (type) {
            // Based off of the SaplingGenerator class, as well as uses of DefaultBiomeFeatures fields
            case TREE: return Feature.TREE.configure(DefaultBiomeFeatures.OAK_TREE_CONFIG);
            case BIG_TREE: return Feature.TREE.configure(DefaultBiomeFeatures.FANCY_TREE_CONFIG);
            case REDWOOD: return Feature.TREE.configure(DefaultBiomeFeatures.SPRUCE_TREE_CONFIG);
            case TALL_REDWOOD: return Feature.TREE.configure(DefaultBiomeFeatures.MEGA_SPRUCE_TREE_CONFIG);
            case MEGA_REDWOOD: return Feature.TREE.configure(DefaultBiomeFeatures.MEGA_PINE_TREE_CONFIG);
            case BIRCH: return Feature.TREE.configure(DefaultBiomeFeatures.BIRCH_TREE_CONFIG);
            case JUNGLE: return Feature.TREE.configure(DefaultBiomeFeatures.MEGA_JUNGLE_TREE_CONFIG);
            case SMALL_JUNGLE: return Feature.TREE.configure(DefaultBiomeFeatures.JUNGLE_TREE_CONFIG);
            case SHORT_JUNGLE: return Feature.TREE.configure(DefaultBiomeFeatures.JUNGLE_SAPLING_TREE_CONFIG);
            case JUNGLE_BUSH: return Feature.TREE.configure(DefaultBiomeFeatures.JUNGLE_GROUND_BUSH_CONFIG);
            case SWAMP: return Feature.TREE.configure(DefaultBiomeFeatures.SWAMP_TREE_CONFIG);
            case ACACIA: return Feature.TREE.configure(DefaultBiomeFeatures.ACACIA_TREE_CONFIG);
            case DARK_OAK: return Feature.TREE.configure(DefaultBiomeFeatures.DARK_OAK_TREE_CONFIG);
            case TALL_BIRCH: return Feature.TREE.configure(DefaultBiomeFeatures.LARGE_BIRCH_TREE_CONFIG);
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
        ServerWorld world = (ServerWorld) getWorld();
        ServerChunkManager chunkManager = world.getChunkManager();
        return generator != null && generator.generate(
            world, world.getStructureAccessor(), chunkManager.getChunkGenerator(), random,
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
        // TODO update sound API
        // getWorld().playSound(type, FabricAdapter.toBlockPos(position.toBlockPoint()), data);
        return true;
    }

    @Override
    public WeatherType getWeather() {
        WorldProperties info = getWorld().getLevelProperties();
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
        ServerWorldProperties info = (ServerWorldProperties) getWorld().getLevelProperties();
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
        ServerWorldProperties info = (ServerWorldProperties) getWorld().getLevelProperties();
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
        WorldProperties worldProps = getWorld().getLevelProperties();
        return BlockVector3.at(
            worldProps.getSpawnX(),
            worldProps.getSpawnY(),
            worldProps.getSpawnZ()
        );
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

}
