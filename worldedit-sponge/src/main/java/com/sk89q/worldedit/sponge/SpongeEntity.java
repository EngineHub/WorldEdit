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

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.sponge.internal.NbtAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.entity.EntityType;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.lang.ref.WeakReference;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

class SpongeEntity implements Entity {

    private final WeakReference<org.spongepowered.api.entity.Entity> entityRef;

    SpongeEntity(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public BaseEntity getState() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity == null || entity.vehicle().isPresent()) {
            return null;
        }
        EntityType entityType = EntityType.REGISTRY.get(entity.type().key(RegistryTypes.ENTITY_TYPE).asString());
        if (entityType == null) {
            return null;
        }
        DataView dataView = entity.toContainer().getView(Constants.Sponge.UNSAFE_NBT)
            .orElse(null);
        return new BaseEntity(
            entityType,
            dataView == null ? null : LazyReference.from(() -> NbtAdapter.adaptToWorldEdit(dataView))
        );
    }

    @Override
    public Location getLocation() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            ServerLocation entityLoc = entity.serverLocation();
            Vector3d entityRot = entity.rotation();

            return SpongeAdapter.adapt(entityLoc, entityRot);
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return entity.setLocation(SpongeAdapter.adapt(location));
        } else {
            return false;
        }
    }

    @Override
    public Extent getExtent() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return SpongeAdapter.adapt(entity.serverLocation().world());
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
