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

package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.entity.EntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.ref.WeakReference;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

class FabricEntity implements Entity {

    private final WeakReference<net.minecraft.entity.Entity> entityRef;

    FabricEntity(net.minecraft.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public BaseEntity getState() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            Identifier id = Registry.ENTITY_TYPE.getId(entity.getType());
            CompoundTag tag = new CompoundTag();
            entity.toTag(tag);
            return new BaseEntity(EntityTypes.get(id.toString()), NBTConverter.fromNative(tag));
        } else {
            return null;
        }
    }

    @Override
    public Location getLocation() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            Vector3 position = Vector3.at(entity.getX(), entity.getY(), entity.getZ());
            float yaw = entity.yaw;
            float pitch = entity.pitch;

            return new Location(FabricAdapter.adapt(entity.world), position, yaw, pitch);
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        // TODO unused atm
        return false;
    }

    @Override
    public Extent getExtent() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return FabricAdapter.adapt(entity.world);
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public boolean remove() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.remove();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            if (EntityProperties.class.isAssignableFrom(cls)) {
                return (T) new FabricEntityProperties(entity);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
