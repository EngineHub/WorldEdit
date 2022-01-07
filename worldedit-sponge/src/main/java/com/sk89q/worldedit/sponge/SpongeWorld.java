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
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.internal.NbtAdapter;
import com.sk89q.worldedit.sponge.internal.SpongeWorldNativeAccess;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.LightTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public final class SpongeWorld extends AbstractWorld {

    private static final Random random = new Random();

    private final WeakReference<ServerWorld> worldRef;
    private final SpongeWorldNativeAccess worldNativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    protected SpongeWorld(ServerWorld world) {
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
    public String getId() {
        return getWorld().key().asString();
    }

    @Override
    public Path getStoragePath() {
        return getWorld().directory();
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return SpongeAdapter.adapt(getWorld().block(
            position.getX(), position.getY(), position.getZ()
        ));
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        CompoundTag entity = getWorld()
            .blockEntity(position.getX(), position.getY(), position.getZ())
            .map(e -> NbtAdapter.adaptToWorldEdit(e.createArchetype().blockEntityData()))
            .orElse(null);
        return getBlock(position).toBaseBlock(entity);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        // Switch back to https://gist.github.com/octylFractal/d1fd5b46574b43e13bb4eb673e53deda
        // When applySideEffects can be implemented in API and we don't need WNA!
        return worldNativeAccess.setBlock(position, block, sideEffects);
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
        getWorld().removeBlockEntity(position.getX(), position.getY(), position.getZ());
        return true;
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        // TODO get sponge team to implement this for us
        return false;
    }


    @Nullable
    private static ConfiguredFeature<?, ?> createTreeFeatureGenerator(TreeGenerator.TreeType type) {
        switch (type) {
            // Based off of the SaplingGenerator class, as well as uses of DefaultBiomeFeatures fields
            case TREE:
                return Features.OAK;
            case BIG_TREE:
                return Features.FANCY_OAK;
            case REDWOOD:
                return Features.SPRUCE;
            case TALL_REDWOOD:
                return Features.MEGA_SPRUCE;
            case MEGA_REDWOOD:
                return Features.MEGA_PINE;
            case BIRCH:
                return Features.BIRCH;
            case JUNGLE:
                return Features.MEGA_JUNGLE_TREE;
            case SMALL_JUNGLE:
                return Features.JUNGLE_TREE;
            case SHORT_JUNGLE:
                return Features.JUNGLE_TREE_NO_VINE;
            case JUNGLE_BUSH:
                return Features.JUNGLE_BUSH;
            case SWAMP:
                return Features.SWAMP_TREE;
            case ACACIA:
                return Features.ACACIA;
            case DARK_OAK:
                return Features.DARK_OAK;
            case TALL_BIRCH:
                return Features.BIRCH_TALL;
            case RED_MUSHROOM:
                return Features.HUGE_RED_MUSHROOM;
            case BROWN_MUSHROOM:
                return Features.HUGE_BROWN_MUSHROOM;
            case WARPED_FUNGUS:
                return Features.WARPED_FUNGI;
            case CRIMSON_FUNGUS:
                return Features.CRIMSON_FUNGI;
            case CHORUS_PLANT:
                return Features.CHORUS_PLANT;
            case RANDOM:
                return createTreeFeatureGenerator(TreeGenerator.TreeType.values()[ThreadLocalRandom.current().nextInt(TreeGenerator.TreeType.values().length)]);
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) {
        ConfiguredFeature<?, ?> generator = createTreeFeatureGenerator(type);
        ServerLevel world = (ServerLevel) getWorld();
        return generator != null && generator.place(
            world, world.getChunkSource().getGenerator(), random,
            new BlockPos(position.getX(), position.getY(), position.getZ())
        );
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);

        int skyLight = getWorld().light(LightTypes.SKY, position.getX(), position.getY(), position.getZ());
        int groundLight = getWorld().light(LightTypes.BLOCK, position.getX(), position.getY(), position.getZ());

        return Math.max(skyLight, groundLight);

    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);
        return BiomeType.REGISTRY.get(
            getWorld().registries().registry(RegistryTypes.BIOME)
                .valueKey(getWorld().biome(position.getBlockX(), position.getBlockY(), position.getBlockZ()))
                .asString()
        );
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        getWorld().setBiome(
            position.getBlockX(), position.getY(), position.getBlockZ(),
            getWorld().registries().registry(RegistryTypes.BIOME).value(
                ResourceKey.resolve(biome.getId())
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
            new Vector3d(position.getX(), position.getY(), position.getZ())
        );

        itemEntity.item().set(
            SpongeAdapter.adapt(item).createSnapshot()
        );
        getWorld().spawnEntity(itemEntity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        getWorld().destroyBlock(
            new Vector3i(position.getX(), position.getY(), position.getZ()),
            true
        );
    }

    @Override
    public boolean canPlaceAt(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState blockState) {
        return ((net.minecraft.world.level.block.state.BlockState) SpongeAdapter.adapt(blockState))
            .canSurvive(
                ((LevelReader) getWorld()),
                new BlockPos(position.getX(), position.getY(), position.getZ())
            );
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if ((o instanceof SpongeWorld)) {
            SpongeWorld other = ((SpongeWorld) o);
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
        ServerWorld world = getWorld();

        EntityType<?> entityType = Sponge.game().registries().registry(RegistryTypes.ENTITY_TYPE)
            .value(ResourceKey.resolve(entity.getType().getId()));
        Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        org.spongepowered.api.entity.Entity newEnt = world.createEntity(entityType, pos);
        if (entity.hasNbtData()) {
            newEnt.setRawData(DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED)
                .set(Constants.Sponge.UNSAFE_NBT, entity.getNbtData()));
        }

        // Overwrite any data set by the NBT application
        Vector3 dir = location.getDirection();

        newEnt.setLocationAndRotation(
            ServerLocation.of(getWorld(), pos),
            new Vector3d(dir.getX(), dir.getY(), dir.getZ())
        );

        if (world.spawnEntity(newEnt)) {
            return new SpongeEntity(newEnt);
        }

        return null;
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
            Sponge.game().registries().registry(RegistryTypes.WEATHER_TYPE).value(
                ResourceKey.resolve(weatherType.getId())
            )
        );
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        getWorld().setWeather(
            Sponge.game().registries().registry(RegistryTypes.WEATHER_TYPE).value(
                ResourceKey.resolve(weatherType.getId())
            ),
            Ticks.of(duration)
        );
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return SpongeAdapter.adaptVector3i(getWorld().properties().spawnPosition());
    }

}
