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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.forge.internal.ForgeWorldNativeAccess;
import com.sk89q.worldedit.forge.internal.NBTConverter;
import com.sk89q.worldedit.forge.internal.TileEntityUtils;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
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
import com.sk89q.worldedit.util.io.file.SafeFiles;
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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IClearable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.biome.IBiomeMagnifier;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
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
public class ForgeWorld extends AbstractWorld {

    private static final Random random = new Random();

    private final WeakReference<World> worldRef;
    private final ForgeWorldNativeAccess nativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    ForgeWorld(World world) {
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
        return ((IServerWorldInfo) getWorld().getWorldInfo()).getWorldName();
    }

    @Override
    public String getId() {
        return getName() + "_" + getWorld().func_234922_V_().func_240901_a_();
    }

    @Override
    public Path getStoragePath() {
        final World world = getWorld();
        if (world instanceof ServerWorld) {
            // see Fabric mixin for what all of this is
            SaveFormat.LevelSave session = ((ServerWorld) world).getServer().anvilConverterForAnvilFile;
            return session.func_237291_a_(world.func_234923_W_()).toPath();
        }
        return null;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
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
    public boolean fullySupports3DBiomes() {
        IBiomeMagnifier magnifier = getWorld().func_230315_m_().getMagnifier();
        return !(magnifier instanceof ColumnFuzzedBiomeMagnifier);
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);

        IChunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4);
        return getBiomeInChunk(position, chunk);
    }

    private BiomeType getBiomeInChunk(BlockVector3 position, IChunk chunk) {
        BiomeContainer biomes = checkNotNull(chunk.getBiomes());
        return ForgeAdapter.adapt(biomes.getNoiseBiome(position.getX() >> 2, position.getY() >> 2, position.getZ() >> 2));
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        IChunk chunk = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4);
        BiomeContainer container = checkNotNull(chunk.getBiomes());
        int idx = BiomeMath.computeBiomeIndex(position.getX(), position.getY(), position.getZ());
        container.biomes[idx] = ForgeAdapter.adapt(biome);
        chunk.setModified(true);
        return true;
    }

    private static final LoadingCache<ServerWorld, WorldEditFakePlayer> fakePlayers
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

    // For unmapped regen names, see Fabric!

    @Override
    public boolean regenerate(Region region, EditSession editSession, RegenOptions options) {
        // Don't even try to regen if it's going to fail.
        AbstractChunkProvider provider = getWorld().getChunkProvider();
        if (!(provider instanceof ServerChunkProvider)) {
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
        SaveFormat levelStorage = SaveFormat.func_237269_a_(tempDir);
        try (SaveFormat.LevelSave session = levelStorage.func_237274_c_("WorldEditTempGen")) {
            ServerWorld originalWorld = (ServerWorld) getWorld();
            long seed = options.getSeed().orElse(originalWorld.getSeed());
            ServerWorldInfo levelProperties =
                (ServerWorldInfo) originalWorld.getServer().func_240793_aU_();
            DimensionGeneratorSettings originalOpts = levelProperties.field_237343_c_;

            Codec<DimensionGeneratorSettings> dimCodec = DimensionGeneratorSettings.field_236201_a_;
            DimensionGeneratorSettings newOpts = dimCodec
                .encodeStart(NBTDynamicOps.INSTANCE, originalOpts)
                .flatMap(tag ->
                    dimCodec.parse(
                        recursivelySetSeed(new Dynamic<>(NBTDynamicOps.INSTANCE, tag), seed, new HashSet<>())
                    )
                )
                .result()
                .orElseThrow(() -> new IllegalStateException("Unable to map GeneratorOptions"));

            levelProperties.field_237343_c_ = newOpts;
            RegistryKey<World> worldRegKey = originalWorld.func_234923_W_();
            Dimension dimGenOpts = newOpts.func_236224_e_()
                .getOrDefault(worldRegKey.func_240901_a_());
            checkNotNull(dimGenOpts, "No DimensionOptions for %s", worldRegKey);
            try (ServerWorld serverWorld = new ServerWorld(
                originalWorld.getServer(), Util.getServerExecutor(), session,
                ((IServerWorldInfo) originalWorld.getWorldInfo()),
                worldRegKey,
                originalWorld.func_234922_V_(),
                originalWorld.func_230315_m_(),
                new WorldEditGenListener(),
                dimGenOpts.func_236064_c_(),
                originalWorld.func_234925_Z_(),
                seed,
                // No spawners are needed for this world.
                ImmutableList.of(),
                // This controls ticking, we don't need it so set it to false.
                false
            )) {
                regenForWorld(region, editSession, serverWorld, options);

                // drive the server executor until all tasks are popped off
                while (originalWorld.getServer().driveOne()) {
                    Thread.yield();
                }
            } finally {
                levelProperties.field_237343_c_ = originalOpts;
            }
        } finally {
            SafeFiles.tryHardToDeleteDir(tempDir);
        }
    }

    @SuppressWarnings("unchecked")
    private Dynamic<INBT> recursivelySetSeed(Dynamic<INBT> dynamic, long seed, Set<Dynamic<INBT>> seen) {
        if (!seen.add(dynamic)) {
            return dynamic;
        }
        return dynamic.updateMapValues(pair -> {
            if (pair.getFirst().asString("").equals("seed")) {
                return pair.mapSecond(v -> v.createLong(seed));
            }
            if (pair.getSecond().getValue() instanceof CompoundNBT) {
                return pair.mapSecond(v -> recursivelySetSeed((Dynamic<INBT>) v, seed, seen));
            }
            return pair;
        });
    }

    private void regenForWorld(Region region, EditSession editSession, ServerWorld serverWorld,
                               RegenOptions options) throws MaxChangedBlocksException {
        List<CompletableFuture<IChunk>> chunkLoadings = submitChunkLoadTasks(region, serverWorld);

        // drive executor until loading finishes
        ThreadTaskExecutor<Runnable> executor = serverWorld.getChunkProvider().executor;
        executor.driveUntil(() -> {
            // bail out early if a future fails
            if (chunkLoadings.stream().anyMatch(ftr ->
                ftr.isDone() && Futures.getUnchecked(ftr) == null
            )) {
                return false;
            }
            return chunkLoadings.stream().allMatch(CompletableFuture::isDone);
        });

        Map<ChunkPos, IChunk> chunks = new HashMap<>();
        for (CompletableFuture<IChunk> future : chunkLoadings) {
            @Nullable
            IChunk chunk = future.getNow(null);
            checkState(chunk != null, "Failed to generate a chunk, regen failed.");
            chunks.put(chunk.getPos(), chunk);
        }

        for (BlockVector3 vec : region) {
            BlockPos pos = ForgeAdapter.toBlockPos(vec);
            IChunk chunk = chunks.get(new ChunkPos(pos));
            BlockStateHolder<?> state = ForgeAdapter.adapt(chunk.getBlockState(pos));
            TileEntity blockEntity = chunk.getTileEntity(pos);
            if (blockEntity != null) {
                CompoundNBT tag = new CompoundNBT();
                blockEntity.write(tag);
                state = state.toBaseBlock(NBTConverter.fromNative(tag));
            }
            editSession.setBlock(vec, state);

            if (options.shouldRegenBiomes()) {
                if (!editSession.fullySupports3DBiomes()) {
                    vec = vec.withY(0);
                }
                editSession.setBiome(vec, getBiomeInChunk(vec, chunk));
            }
        }
    }

    private List<CompletableFuture<IChunk>> submitChunkLoadTasks(Region region, ServerWorld world) {
        List<CompletableFuture<IChunk>> chunkLoadings = new ArrayList<>();
        // Pre-gen all the chunks
        for (BlockVector2 chunk : region.getChunks()) {
            chunkLoadings.add(
                world.getChunkProvider().func_217233_c(chunk.getX(), chunk.getZ(), ChunkStatus.FEATURES, true)
                    .thenApply(either -> either.left().orElse(null))
            );
        }
        return chunkLoadings;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> createTreeFeatureGenerator(TreeType type) {
        switch (type) {
            case TREE: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.OAK_TREE_CONFIG);
            case BIG_TREE: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.FANCY_TREE_CONFIG);
            case REDWOOD: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.SPRUCE_TREE_CONFIG);
            case TALL_REDWOOD: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.MEGA_SPRUCE_TREE_CONFIG);
            case MEGA_REDWOOD: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.MEGA_PINE_TREE_CONFIG);
            case BIRCH: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.BIRCH_TREE_CONFIG);
            case JUNGLE: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.MEGA_JUNGLE_TREE_CONFIG);
            case SMALL_JUNGLE: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.JUNGLE_TREE_CONFIG);
            case SHORT_JUNGLE: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.JUNGLE_SAPLING_TREE_CONFIG);
            case JUNGLE_BUSH: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.JUNGLE_GROUND_BUSH_CONFIG);
            case SWAMP: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.SWAMP_TREE_CONFIG);
            case ACACIA: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.ACACIA_TREE_CONFIG);
            case DARK_OAK: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.DARK_OAK_TREE_CONFIG);
            case TALL_BIRCH: return Feature.field_236291_c_.withConfiguration(DefaultBiomeFeatures.field_230130_i_);
            case RED_MUSHROOM: return Feature.HUGE_RED_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_RED_MUSHROOM);
            case BROWN_MUSHROOM: return Feature.HUGE_BROWN_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_BROWN_MUSHROOM);
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) {
        ConfiguredFeature<?, ?> generator = createTreeFeatureGenerator(type);
        ServerWorld world = (ServerWorld) getWorld();
        ServerChunkProvider chunkManager = world.getChunkProvider();
        return generator != null && generator.func_236265_a_(
            world, world.func_241112_a_(), chunkManager.getChunkGenerator(), random,
            ForgeAdapter.toBlockPos(position)
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
        IWorldInfo info = getWorld().getWorldInfo();
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
        IServerWorldInfo info = (IServerWorldInfo) getWorld().getWorldInfo();
        if (info.isThundering()) {
            return info.getThunderTime();
        }
        if (info.isRaining()) {
            return info.getRainTime();
        }
        return info.func_230395_g_();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        setWeather(weatherType, 0);
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        IServerWorldInfo info = (IServerWorldInfo) getWorld().getWorldInfo();
        if (weatherType == WeatherTypes.THUNDER_STORM) {
            info.func_230391_a_(0);
            info.setThundering(true);
            info.setThunderTime((int) duration);
        } else if (weatherType == WeatherTypes.RAIN) {
            info.func_230391_a_(0);
            info.setRaining(true);
            info.setRainTime((int) duration);
        } else if (weatherType == WeatherTypes.CLEAR) {
            info.setRaining(false);
            info.setThundering(false);
            info.func_230391_a_((int) duration);
        }
    }

    @Override
    public int getMinY() {
        // Note: This method exists to be re-written by mods that vary world height
        return 0;
    }

    @Override
    public int getMaxY() {
        return getWorld().getHeight() - 1;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        IWorldInfo worldInfo = getWorld().getWorldInfo();
        return BlockVector3.at(
            worldInfo.getSpawnX(),
            worldInfo.getSpawnY(),
            worldInfo.getSpawnZ()
        );
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
        return ((ServerWorld) world).getEntities().filter(e -> region.contains(ForgeAdapter.adapt(e.func_233580_cy_())))
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
        if (!entityType.isPresent()) {
            return null;
        }
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

}
