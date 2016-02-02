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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public abstract class SpongeWorld extends AbstractWorld {

    protected static final Random random = new Random();
    protected static final int UPDATE = 1, NOTIFY = 2, NOTIFY_CLIENT = 4;
    protected static final Logger logger = Logger.getLogger(SpongeWorld.class.getCanonicalName());

    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    SpongeWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<World>(world);
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

    protected abstract BlockSnapshot createBlockSnapshot(Vector position, BaseBlock block);

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        return createBlockSnapshot(position, block).restore(true, notifyAndLight);
    }

    @Override
    public int getBlockLightLevel(Vector position) {
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
    public BaseBiome getBiome(Vector2D position) {
        checkNotNull(position);
        return new BaseBiome(IDHelper.resolve(getWorld().getBiome(position.getBlockX(), position.getBlockZ())));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        checkNotNull(position);
        checkNotNull(biome);

        getWorld().setBiome(position.getBlockX(), position.getBlockZ(), IDHelper.resolveBiome(biome.getId()));
        return true;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == 0) {
            return;
        }

        Optional<org.spongepowered.api.entity.Entity> optItem = getWorld().createEntity(
                EntityTypes.ITEM,
                new Vector3d(position.getX(), position.getY(), position.getZ())
        );

        if (optItem.isPresent()) {
            org.spongepowered.api.entity.Entity entity = optItem.get();
            entity.offer(Keys.REPRESENTED_ITEM, SpongeWorldEdit.toSpongeItemStack(item).createSnapshot());
            getWorld().spawnEntity(entity, Cause.of(SpongeWorldEdit.inst));
        }
    }



    @Override
    public WorldData getWorldData() {
        return ForgeWorldData.getInstance();
    }

    @Override
    public boolean isValidBlockType(int id) {
        return (id == 0) || (IDHelper.resolveItem(id) != null);
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
            return otherWorld != null && thisWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList<>();
        for (org.spongepowered.api.entity.Entity entity : getWorld().getEntities()) {
            org.spongepowered.api.world.Location<World> loc = entity.getLocation();
            if (region.contains(new Vector(loc.getX(), loc.getY(), loc.getZ()))) {
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

    protected abstract EntitySnapshot createEntitySnapshot(Location location, BaseEntity entity);

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        EntitySnapshot snapshot = createEntitySnapshot(location, entity);
        if (snapshot != null) {
            Optional<org.spongepowered.api.entity.Entity> restoredEnt = snapshot.restore();
            if (restoredEnt.isPresent()) {
                return new SpongeEntity(restoredEnt.get());
            }
        }
        return null;
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
