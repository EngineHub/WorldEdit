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
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

class SpongeEntity implements Entity {

    private final WeakReference<org.spongepowered.api.entity.Entity> entityRef;

    SpongeEntity(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public BaseEntity getState() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return SpongeWorldEdit.inst().getAdapter().createBaseEntity(entity);
        } else {
            return null;
        }
    }

    @Override
    public Location getLocation() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            org.spongepowered.api.world.Location<World> entityLoc = entity.getLocation();
            Vector3d entityRot = entity.getRotation();

            return SpongeWorldEdit.inst().getAdapter().adapt(entityLoc, entityRot);
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public Extent getExtent() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return SpongeWorldEdit.inst().getAdapter().getWorld(entity.getWorld());
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public boolean remove() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.remove();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            if (EntityProperties.class.isAssignableFrom(cls)) {
                return (T) new SpongeEntityProperties(entity);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
