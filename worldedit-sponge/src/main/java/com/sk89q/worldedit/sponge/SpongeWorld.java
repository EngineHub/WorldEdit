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

package com.sk89q.worldedit.sponge;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weather;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public abstract class SpongeWorld extends AbstractWorld {

    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    protected SpongeWorld(World world) {
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
        return getWorld().getName();
    }

    @SuppressWarnings("WeakerAccess")
    protected BlockState getBlockState(BlockStateHolder<?> block) {
        if (block instanceof com.sk89q.worldedit.world.block.BlockState) {
            BlockState state = Sponge.getRegistry().getType(BlockType.class, block.getBlockType().getId()).orElse(BlockTypes.AIR).getDefaultState();
            for (Map.Entry<Property<?>, Object> entry : block.getStates().entrySet()) {
                // TODO Convert across states
            }
            return state;
        } else {
            throw new UnsupportedOperationException("Missing Sponge adapter for WorldEdit!");
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract void applyTileEntityData(TileEntity entity, BaseBlock block);

    private static final BlockSnapshot.Builder builder = BlockSnapshot.builder();

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();

        // First set the block
        Vector3i pos = new Vector3i(position.getX(), position.getY(), position.getZ());
        BlockState newState = getBlockState(block);

        BlockSnapshot snapshot = builder.reset()
                .blockState(newState)
                .position(pos)
                .world(world.getProperties())
                .build();

        snapshot.restore(true, notifyAndLight ? BlockChangeFlags.ALL : BlockChangeFlags.NONE);

        // Create the TileEntity
        if (block instanceof BaseBlock && ((BaseBlock) block).hasNbtData()) {
            // Kill the old TileEntity
            world.getTileEntity(pos).ifPresent(tileEntity -> applyTileEntityData(tileEntity, (BaseBlock) block));
        }

        return true;
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState previousType) throws WorldEditException {
        // TODO Move this to adapter
        return false;
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return false;
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);

        BlockState state = getWorld().getBlock(new Vector3i(position.getX(), position.getY(), position.getZ()));

        Optional<GroundLuminanceProperty> groundLuminanceProperty = state.getProperty(GroundLuminanceProperty.class);
        Optional<SkyLuminanceProperty> skyLuminanceProperty = state.getProperty(SkyLuminanceProperty.class);

        if (!groundLuminanceProperty.isPresent() || !skyLuminanceProperty.isPresent()) {
            return 0;
        }

        //noinspection ConstantConditions
        return (int) Math.max(groundLuminanceProperty.get().getValue(), skyLuminanceProperty.get().getValue());

    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        checkNotNull(position);
        return SpongeAdapter.adapt(getWorld().getBiome(position.getBlockX(), 0, position.getBlockZ()));
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        getWorld().setBiome(position.getBlockX(), 0, position.getBlockZ(), SpongeAdapter.adapt(biome));
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
                EntityTypes.ITEM,
                new Vector3d(position.getX(), position.getY(), position.getZ())
        );

        entity.offer(Keys.REPRESENTED_ITEM, SpongeWorldEdit.toSpongeItemStack(item).createSnapshot());
        getWorld().spawnEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        // TODO
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
            World otherWorld = other.worldRef.get();
            World thisWorld = worldRef.get();
            return otherWorld != null && otherWorld.equals(thisWorld);
        } else {
            return o instanceof com.sk89q.worldedit.world.World
                    && ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList<>();
        for (org.spongepowered.api.entity.Entity entity : getWorld().getEntities()) {
            org.spongepowered.api.world.Location<World> loc = entity.getLocation();
            if (region.contains(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))) {
                entities.add(new SpongeEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        for (org.spongepowered.api.entity.Entity entity : getWorld().getEntities()) {
            entities.add(new SpongeEntity(entity));
        }
        return entities;
    }

    protected abstract void applyEntityData(org.spongepowered.api.entity.Entity entity, BaseEntity data);

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();

        EntityType entityType = Sponge.getRegistry().getType(EntityType.class, entity.getType().getId()).get();
        Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        org.spongepowered.api.entity.Entity newEnt = world.createEntity(entityType, pos);
        if (entity.hasNbtData()) {
            applyEntityData(newEnt, entity);
        }

        // Overwrite any data set by the NBT application
        Vector3 dir = location.getDirection();

        newEnt.setLocationAndRotation(
                new org.spongepowered.api.world.Location<>(getWorld(), pos),
                new Vector3d(dir.getX(), dir.getY(), dir.getZ())
        );

        if (world.spawnEntity(newEnt)) {
            return new SpongeEntity(newEnt);
        }

        return null;
    }

    @Override
    public WeatherType getWeather() {
        return WeatherTypes.get(getWorld().getWeather().getId());
    }

    @Override
    public long getRemainingWeatherDuration() {
        return getWorld().getRemainingDuration();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        getWorld().setWeather(Sponge.getRegistry().getType(Weather.class, weatherType.getId()).get());
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        getWorld().setWeather(Sponge.getRegistry().getType(Weather.class, weatherType.getId()).get(), duration);
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return SpongeAdapter.asBlockVector(getWorld().getSpawnLocation());
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    private static class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

}
