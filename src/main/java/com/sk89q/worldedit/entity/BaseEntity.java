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

package com.sk89q.worldedit.entity;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.NbtValued;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A snapshot of an entity that can be reused and passed around.
 */
public class BaseEntity implements NbtValued {

    private CompoundTag nbtData;

    /**
     * Create a new entity with the given entity ID.
     *
     * @param id the ID of the entity, which determines its type
     */
    public BaseEntity(String id) {
        checkNotNull(id);
        Map<String, Tag> map = new HashMap<String, Tag>();
        map.put("id", new StringTag("id", id));
        this.nbtData = new CompoundTag("", map);
    }

    /**
     * Create a new entity with the given NBT data.
     *
     * @param nbtData the NBT data
     */
    public BaseEntity(CompoundTag nbtData) {
        checkNotNull(nbtData);
        this.nbtData = nbtData;
    }

    /**
     * Get the ID of the entity, which determines the type of entity.
     * An example of an entity ID would be "CaveSpider".
     *
     * @return the entity ID, which may be an empty string
     */
    public String getEntityId() {
        CompoundTag nbtData = getNbtData();
        if (nbtData == null) {
            return "";
        }
        Tag idTag = nbtData.getValue().get("id");
        if (idTag != null && idTag instanceof StringTag) {
            return ((StringTag) idTag).getValue();
        } else {
            return "";
        }
    }

    @Override
    public boolean hasNbtData() {
        return getNbtData() != null;
    }

    @Override
    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public void setNbtData(CompoundTag nbtData) throws DataException {
        checkNotNull(nbtData);
        this.nbtData = nbtData;
    }

}
