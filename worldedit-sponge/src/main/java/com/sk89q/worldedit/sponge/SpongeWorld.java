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

package com.sk89q.worldedit.sponge;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.internal.NbtAdapter;
import com.sk89q.worldedit.sponge.internal.SpongeWorldNativeAccess;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LightTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public final class SpongeWorld extends AbstractWorld {

    private static final RandomSource random = RandomSource.create();
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final WeakReference<ServerWorld> worldRef;
    private final SpongeWorldNativeAccess worldNativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    SpongeWorld(ServerWorld world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<>(world);
        this.worldNativeAccess = new SpongeWorldNativeAccess(new WeakReference<>((ServerLevel) world));
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was
     *     unloaded)
     */
    ServerWorld getWorld() {
        ServerWorld world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    // This is sus but leaving it for later world name/id reworks
    @Override
    public String getName() {
        return getWorld().key().asString();
    }

    @Override
    public String id() {
        return getWorld().key().asString();
    }

    @Override
    public Path getStoragePath() {
        return getWorld().directory();
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return SpongeAdapter.adapt(getWorld().block(
            position.x(), position.y(), position.z()
        ));
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockEntity blockEntity = getWorld().blockEntity(
            position.x(), position.y(), position.z()
        ).orElse(null);
        LinCompoundTag blockEntityData = null;
        if (blockEntity != null) {
            BlockEntityArchetype blockEntityArchetype = blockEntity.createArchetype();
            BlockEntityType blockEntityType = blockEntityArchetype.blockEntityType();
            ResourceKey blockEntityId = blockEntityType.key(RegistryTypes.BLOCK_ENTITY_TYPE);
            blockEntityData = NbtAdapter.adaptToWorldEdit(blockEntityArchetype.blockEntityData());

            // Add ID and position since Sponge's #blockEntityData does not save metadata
            LinCompoundTag.Builder fullBlockEntityDataBuilder = blockEntityData.toBuilder();
            fullBlockEntityDataBuilder.put("id", LinStringTag.of(blockEntityId.formatted()));
            fullBlockEntityDataBuilder.put("x", LinIntTag.of(position.x()));
            fullBlockEntityDataBuilder.put("y", LinIntTag.of(position.y()));
            fullBlockEntityDataBuilder.put("z", LinIntTag.of(position.z()));
            blockEntityData = fullBlockEntityDataBuilder.build();
        }
        return getBlock(position).toBaseBlock(blockEntityData);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        ServerWorld world = getWorld();

        org.spongepowered.api.block.BlockState newState = SpongeAdapter.adapt(block.toImmutableState());

        boolean didSet = world.setBlock(
            position.x(), position.y(), position.z(),
            newState,
            BlockChangeFlags.NONE
                .withUpdateNeighbors(sideEffects.shouldApply(SideEffect.NEIGHBORS))
                .withNotifyClients(true)
                .withPhysics(sideEffects.shouldApply(SideEffect.UPDATE))
                .withNotifyObservers(sideEffects.shouldApply(SideEffect.UPDATE))
                .withLightingUpdates(sideEffects.shouldApply(SideEffect.LIGHTING))
                .withPathfindingUpdates(sideEffects.shouldApply(SideEffect.ENTITY_AI))
                .withNeighborDropsAllowed(false)
                .withBlocksMoving(false)
                .withForcedReRender(false)
                .withIgnoreRender(false)
        );
        if (!didSet) {
            // still update NBT if the block is the same
            if (world.block(position.x(), position.y(), position.z()) == newState) {
                didSet = block.toBaseBlock().getNbt() != null;
            }
        }

        // Create the TileEntity
        if (didSet && block instanceof BaseBlock baseBlock) {
            LinCompoundTag nbt = baseBlock.getNbt();
            if (nbt != null) {
                BlockEntityArchetype.builder()
                    .state(newState)
                    .blockEntity(
                        Sponge.game().registry(RegistryTypes.BLOCK_ENTITY_TYPE)
                            .<BlockEntityType>value(ResourceKey.resolve(baseBlock.getNbtId()))
                    )
                    .blockEntityData(NbtAdapter.adaptFromWorldEdit(nbt))
                    .build()
                    .apply(ServerLocation.of(world, position.x(), position.y(), position.z()));
            }
        }

        return true;
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
        checkNotNull(position);

        worldNativeAccess.applySideEffects(position, previousType, sideEffectSet);

        return Sets.intersection(
            SpongeWorldEdit.inst().getInternalPlatform().getSupportedSideEffects(),
            sideEffectSet.getSideEffectsToApply()
        );
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        getWorld().removeBlockEntity(position.x(), position.y(), position.z());
        return true;
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        Server server = Sponge.server();

        final String id = "worldedittemp_" + getWorld().key().value();

        WorldTemplate tempWorldProperties = WorldTemplate.builder().from(getWorld())
            .key(ResourceKey.of("worldedit", id))
            .add(Keys.IS_LOAD_ON_STARTUP, false)
            .add(Keys.SERIALIZATION_BEHAVIOR, SerializationBehavior.NONE)
            .add(Keys.SEED, options.getSeed().orElse(getWorld().properties().worldGenerationConfig().seed()))
            .build();

        ServerWorld tempWorld;
        try {
            tempWorld = server.worldManager().loadWorld(tempWorldProperties).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to load temp world", e);
            return false;
        }

        try {
            // Pre-gen all the chunks
            // We need to also pull one more chunk in every direction
            CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 16, 16), region.getMaximumPoint().add(16, 16, 16));
            for (BlockVector3 chunk : expandedPreGen.getChunkCubes()) {
                tempWorld.loadChunk(chunk.x(), chunk.y(), chunk.z(), true);
            }

            World from = SpongeAdapter.adapt(tempWorld);
            for (BlockVector3 vec : region) {
                extent.setBlock(vec, from.getFullBlock(vec));
                if (options.shouldRegenBiomes()) {
                    extent.setBiome(vec, from.getBiome(vec));
                }
            }
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        } finally {
            // Remove temp world
            server.worldManager().unloadWorld(tempWorldProperties.key()).thenRun(() -> server.worldManager().deleteWorld(tempWorldProperties.key()));
        }

        return true;
    }


    @Nullable
    private static net.minecraft.resources.ResourceKey<ConfiguredFeature<?, ?>> createTreeFeatureGenerator(TreeGenerator.TreeType type) {
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
            case MANGROVE -> TreeFeatures.MANGROVE;
            case TALL_MANGROVE -> TreeFeatures.TALL_MANGROVE;
            case CHERRY -> TreeFeatures.CHERRY;
            case RANDOM ->
                createTreeFeatureGenerator(TreeGenerator.TreeType.values()[ThreadLocalRandom.current().nextInt(TreeGenerator.TreeType.values().length)]);
            default -> null;
        };
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) {
        ServerLevel world = (ServerLevel) getWorld();
        ConfiguredFeature<?, ?> generator = Optional.ofNullable(createTreeFeatureGenerator(type))
            .map(k -> world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(k))
            .orElse(null);
        return generator != null && generator.place(
            world, world.getChunkSource().getGenerator(), random,
            new BlockPos(position.x(), position.y(), position.z())
        );
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);

        int skyLight = getWorld().light(LightTypes.SKY, position.x(), position.y(), position.z());
        int groundLight = getWorld().light(LightTypes.BLOCK, position.x(), position.y(), position.z());

        return Math.max(skyLight, groundLight);

    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);
        return BiomeType.REGISTRY.get(
            getWorld().registry(RegistryTypes.BIOME)
                .valueKey(getWorld().biome(position.x(), position.y(), position.z()))
                .asString()
        );
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        getWorld().setBiome(
            position.x(), position.y(), position.z(),
            getWorld().registry(RegistryTypes.BIOME).value(
                ResourceKey.resolve(biome.id())
            )
        );
        return true;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        Item itemEntity = getWorld().createEntity(
            EntityTypes.ITEM,
            new Vector3d(position.x(), position.y(), position.z())
        );

        itemEntity.item().set(
            SpongeAdapter.adapt(item).createSnapshot()
        );
        getWorld().spawnEntity(itemEntity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        getWorld().destroyBlock(
            new Vector3i(position.x(), position.y(), position.z()),
            true
        );
    }

    @Override
    public boolean canPlaceAt(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState blockState) {
        return ((net.minecraft.world.level.block.state.BlockState) SpongeAdapter.adapt(blockState))
            .canSurvive(
                ((LevelReader) getWorld()),
                new BlockPos(position.x(), position.y(), position.z())
            );
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public int getMaxY() {
        return getWorld().max().y();
    }

    @Override
    public int getMinY() {
        return getWorld().min().y();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if ((o instanceof SpongeWorld other)) {
            ServerWorld otherWorld = other.worldRef.get();
            ServerWorld thisWorld = worldRef.get();
            return otherWorld != null && otherWorld.equals(thisWorld);
        } else {
            return o instanceof com.sk89q.worldedit.world.World
                && ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return getWorld()
            .entityStream(
                SpongeAdapter.adaptVector3i(region.getMinimumPoint()),
                SpongeAdapter.adaptVector3i(region.getMaximumPoint()),
                // We don't need to force load or clone to copy entities
                StreamOptions.builder()
                    .setCarbonCopy(false)
                    .setLoadingStyle(StreamOptions.LoadingStyle.NONE)
                    .build()
            )
            .toStream()
            .map(ve -> new SpongeEntity(ve.type()))
            .collect(Collectors.toList());
    }

    @Override
    public List<? extends Entity> getEntities() {
        return getWorld().entities().stream()
            .map(SpongeEntity::new)
            .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        Optional<EntityType<?>> entityType = Sponge.game().registry(RegistryTypes.ENTITY_TYPE)
            .findValue(ResourceKey.resolve(entity.getType().id()));
        if (entityType.isEmpty()) {
            return null;
        }
        EntityArchetype.Builder builder = EntityArchetype.builder().type(entityType.get());
        var nativeTag = entity.getNbt();
        if (nativeTag != null) {
            builder.entityData(NbtAdapter.adaptFromWorldEdit(nativeTag));
        }
        return builder.build().apply(SpongeAdapter.adapt(location)).map(SpongeEntity::new).orElse(null);
    }

    @Override
    public WeatherType getWeather() {
        return WeatherTypes.get(
            getWorld().weather().type().key(RegistryTypes.WEATHER_TYPE).asString()
        );
    }

    @Override
    public long getRemainingWeatherDuration() {
        return getWorld().weather().remainingDuration().ticks();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        getWorld().setWeather(
            Sponge.game().registry(RegistryTypes.WEATHER_TYPE).value(
                ResourceKey.resolve(weatherType.id())
            )
        );
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        getWorld().setWeather(
            Sponge.game().registry(RegistryTypes.WEATHER_TYPE).value(
                ResourceKey.resolve(weatherType.id())
            ),
            Ticks.of(duration)
        );
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return SpongeAdapter.adaptVector3i(getWorld().properties().spawnPosition());
    }

}
