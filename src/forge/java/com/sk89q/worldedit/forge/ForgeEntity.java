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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityType;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

class ForgeEntity implements Entity {

    private final net.minecraft.entity.Entity entity;

    ForgeEntity(net.minecraft.entity.Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    /**
     * Return the underlying entity.
     *
     * @return the underlying entity
     */
    net.minecraft.entity.Entity getEntity() {
        return entity;
    }

    @Override
    public BaseEntity getState() {
        String id = EntityList.getEntityString(entity);
        if (id != null) {
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            return new BaseEntity(id, NBTConverter.fromNative(tag));
        } else {
            return null;
        }
    }

    @Override
    public Location getLocation() {
        Vector position = new Vector(entity.posX, entity.posY, entity.posZ);
        float yaw = (float) Math.toRadians(entity.rotationYaw);
        float pitch = (float) Math.toRadians(entity.rotationPitch);

        return new Location(ForgeAdapter.adapt(entity.worldObj), position, yaw, pitch);
    }

    @Override
    public Extent getExtent() {
        return ForgeAdapter.adapt(entity.worldObj);
    }

    @Override
    public boolean remove() {
        entity.setDead();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        if (EntityType.class.isAssignableFrom(cls)) {
            return (T) new ForgeEntityType(entity);
        } else {
            return null;
        }
    }
}
