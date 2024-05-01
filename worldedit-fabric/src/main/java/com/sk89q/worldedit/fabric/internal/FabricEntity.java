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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.FabricEntityProperties;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.entity.EntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.lang.ref.WeakReference;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class FabricEntity implements Entity {

    private final WeakReference<net.minecraft.world.entity.Entity> entityRef;

    public FabricEntity(net.minecraft.world.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public BaseEntity getState() {
        net.minecraft.world.entity.Entity entity = entityRef.get();
        if (entity == null || entity.isPassenger()) {
            return null;
        }
        ResourceLocation id = FabricWorldEdit.getRegistry(Registries.ENTITY_TYPE).getKey(entity.getType());
        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        return new BaseEntity(
            EntityTypes.get(id.toString()),
            LazyReference.from(() -> NBTConverter.fromNative(tag))
        );
    }

    @Override
    public Location getLocation() {
        net.minecraft.world.entity.Entity entity = entityRef.get();
        if (entity != null) {
            Vector3 position = Vector3.at(entity.getX(), entity.getY(), entity.getZ());
            float yaw = entity.getYRot();
            float pitch = entity.getXRot();

            return new Location(FabricAdapter.adapt(entity.level()), position, yaw, pitch);
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
        net.minecraft.world.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return FabricAdapter.adapt(entity.level());
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public boolean remove() {
        net.minecraft.world.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        net.minecraft.world.entity.Entity entity = entityRef.get();
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
