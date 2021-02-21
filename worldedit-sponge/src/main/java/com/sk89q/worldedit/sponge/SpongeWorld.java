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
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.internal.SpongeWorldNativeAccess;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.WorldUnloadedException;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class SpongeWorld extends AbstractWorld {

    private final WeakReference<ServerWorld> worldRef;
    private final WeakReference<ServerLevel> nativeWorldRef;
    private final SpongeWorldNativeAccess worldNativeAccess;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    protected SpongeWorld(ServerWorld world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<>(world);
        this.nativeWorldRef = new WeakReference<>((ServerLevel) world);
        this.worldNativeAccess = new SpongeWorldNativeAccess(nativeWorldRef);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws WorldEditException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public ServerWorld getWorldChecked() throws WorldEditException {
        ServerWorld world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldUnloadedException();
        }
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public ServerWorld getWorld() {
        ServerWorld world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    public ServerLevel getNativeWorld() {
        ServerLevel world = nativeWorldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the native world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return getWorld().getKey().toString();
    }

    @Override
    public String getId() {
        return getName().replace(" ", "_").toLowerCase(Locale.ROOT)
            + getWorld().asTemplate().getKey().getFormatted().toLowerCase(Locale.ROOT);
    }

    @Override
    public Path getStoragePath() {
        return getWorld().getDirectory();
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        clearContainerBlockContents(position);
        return worldNativeAccess.setBlock(position, block, sideEffects);
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
        worldNativeAccess.applySideEffects(position, previousType, sideEffectSet);
        return Sets.intersection(SpongeWorldEdit.inst().getPlatform().getSupportedSideEffects(), sideEffectSet.getSideEffectsToApply());
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);

        return getWorld().getLight(new Vector3i(position.getX(), position.getY(), position.getZ()));
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);
        if (!getBlock(position).getBlockType().getMaterial().hasContainer()) {
            return false;
        }

        BlockEntity tile = getWorld().getBlockEntity(SpongeAdapter.adapt(position)).orElse(null);
        if (tile instanceof CarrierBlockEntity) {
            ((CarrierBlockEntity) tile).getInventory().clear();
            return true;
        }
        return false;
    }

    @Override
    public com.sk89q.worldedit.world.block.BlockState getBlock(BlockVector3 position) {
        return SpongeAdapter.adapt(getWorld().getBlock(position.getX(), position.getY(), position.getZ()));
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        // TODO NBT Data
        return SpongeAdapter.adapt(getWorld().getBlock(position.getX(), position.getY(), position.getZ())).toBaseBlock();
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        checkNotNull(position);
        return SpongeAdapter.adapt(getWorld().getBiome(position.getBlockX(), position.getBlockY(), position.getBlockZ()), getWorld().registries());
    }

    @Override
    public boolean fullySupports3DBiomes() {
        return !getWorld().asTemplate().getKey().equals(WorldTemplate.overworld().getKey());
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        getWorld().setBiome(position.getBlockX(), position.getY(), position.getBlockZ(), SpongeAdapter.adapt(biome, getWorld().registries()));
        return true;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        org.spongepowered.api.entity.Entity entity = getWorld().createEntity(
                EntityTypes.ITEM.get(),
                new Vector3d(position.getX(), position.getY(), position.getZ())
        );

        // TODO
        // entity.offer(Keys.ITEM_STACK_SNAPSHOT, SpongeAdapter.adapt(item).createSnapshot());
        getWorld().spawnEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        getWorld().destroyBlock(SpongeAdapter.adapt(position), true);
    }

    @Override
    public boolean canPlaceAt(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState blockState) {
        // TODO
        return true;
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        return false;
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
        List<Entity> entities = new ArrayList<>();
        // This region may not be cuboid
        for (org.spongepowered.api.entity.Entity entity : getWorld().getEntities(AABB.of(getWorld().getBlockMin(), getWorld().getBlockMax()))) {
            if (region.contains(SpongeAdapter.adapt(entity.getLocation().getBlockPosition()))) {
                entities.add(new SpongeEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        for (org.spongepowered.api.entity.Entity entity : getWorld().getEntities(AABB.of(getWorld().getBlockMin(), getWorld().getBlockMax()))) {
            entities.add(new SpongeEntity(entity));
        }
        return entities;
    }

    protected void applyEntityData(org.spongepowered.api.entity.Entity entity, BaseEntity data) {
        // TODO Adapter
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        ServerWorld world = getWorld();


        EntityType<?> entityType = RegistryTypes.ENTITY_TYPE.get().findValue(ResourceKey.resolve(entity.getType().getId())).get();
        Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        org.spongepowered.api.entity.Entity newEnt = world.createEntity(entityType, pos);
        if (entity.getNbtReference() != null) {
            applyEntityData(newEnt, entity);
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
        return SpongeAdapter.adapt(getWorld().weather().type());
    }

    @Override
    public long getRemainingWeatherDuration() {
        return getWorld().weather().remainingDuration().getTicks();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        getWorld().setWeather(SpongeAdapter.adapt(weatherType));
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        getWorld().setWeather(SpongeAdapter.adapt(weatherType), Ticks.of(duration));
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return SpongeAdapter.adapt(getWorld().getProperties().spawnPosition());
    }

}
