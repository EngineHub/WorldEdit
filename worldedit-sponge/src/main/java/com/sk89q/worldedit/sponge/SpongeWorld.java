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
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public abstract class SpongeWorld extends AbstractWorld {

    private static final Logger log = LoggerFactory.getLogger(SpongeWorld.class);
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

    protected abstract BlockState getBlockState(BaseBlock block);

    protected abstract void applyTileEntityData(TileEntity entity, BaseBlock block);

    private static final BlockSnapshot.Builder builder = BlockSnapshot.builder();

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
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
        if (block.hasNbtData()) {
            // Kill the old TileEntity
            world.getTileEntity(pos).ifPresent(tileEntity -> applyTileEntityData(tileEntity, block));
        }

        return true;
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        Server server = Sponge.getServer();

        final String id = "regenArchetype" + getWorld().getName();
        final WorldArchetype regenerationArchetype = Sponge.getRegistry().getType(WorldArchetype.class, id)
                .orElseGet(() -> WorldArchetype.builder()
                        .from(getWorld().getProperties())
                        .serializationBehavior(SerializationBehaviors.NONE)
                        .build(id, id));

        WorldProperties tempWorldProperties;
        try {
            tempWorldProperties = server.createWorldProperties("worldedittemp", regenerationArchetype);
            tempWorldProperties.setGenerateSpawnOnLoad(false);
        } catch (IOException e) {
            log.error("Error creating world properties", e);
            return false;
        }

        Optional<World> tempWorldOpt = server.loadWorld(tempWorldProperties);
        if (!tempWorldOpt.isPresent()) {
            log.error("Failed to load temp world");
            return false;
        }

        World freshWorld = tempWorldOpt.get();
        try {
            // Pre-gen all the chunks
            // We need to also pull one more chunk in every direction
            CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 0, 16), region.getMaximumPoint().add(16, 0, 16));
            for (Vector2D chunk : expandedPreGen.getChunks()) {
                freshWorld.getChunk(chunk.getBlockX(), 0, chunk.getBlockZ());
            }

            SpongeWorld from = SpongeWorldEdit.inst().getWorld(freshWorld);
            for (BlockVector vec : region) {
                editSession.setBlock(vec, from.getBlock(vec));
            }

        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        } finally {
            // Remove temp world
            server.unloadWorld(freshWorld);
            server.deleteWorld(tempWorldProperties);
        }

        return false;
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
        return new BaseBiome(SpongeWorldEdit.inst().getAdapter().resolve(getWorld().getBiome(position.getBlockX(), 0, position.getBlockZ())));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        checkNotNull(position);
        checkNotNull(biome);

        getWorld().setBiome(position.getBlockX(), 0, position.getBlockZ(), SpongeWorldEdit.inst().getAdapter().resolveBiome(biome.getId()));
        return true;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == 0) {
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
    public WorldData getWorldData() {
        return SpongeWorldData.getInstance();
    }

    @Override
    public boolean isValidBlockType(int id) {
        return id == 0 || SpongeWorldEdit.inst().getAdapter().resolveBlock(id) != null;
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

    protected abstract void applyEntityData(org.spongepowered.api.entity.Entity entity, BaseEntity data);

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();

        EntityType entityType = Sponge.getRegistry().getType(EntityType.class, entity.getTypeId()).get();
        Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        org.spongepowered.api.entity.Entity newEnt = world.createEntity(entityType, pos);
        if (entity.hasNbtData()) {
            applyEntityData(newEnt, entity);
        }

        // Overwrite any data set by the NBT application
        Vector dir = location.getDirection();

        newEnt.setLocationAndRotation(
                new org.spongepowered.api.world.Location<>(getWorld(), pos),
                new Vector3d(dir.getX(), dir.getY(), dir.getZ())
        );

        if (world.spawnEntity(newEnt)) {
            return new SpongeEntity(newEnt);
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
