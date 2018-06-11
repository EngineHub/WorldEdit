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

package com.sk89q.worldedit.blocks.type;

import com.sk89q.worldedit.world.registry.BundledItemData;

public class ItemType {

    private String id;

    public ItemType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Gets the legacy ID. Needed for legacy reasons.
     *
     * DO NOT USE THIS.
     *
     * @return legacy id or 0, if unknown
     */
    @Deprecated
    public int getLegacyId() {
        Integer id = BundledItemData.getInstance().toLegacyId(this.id);
        if (id != null) {
            return id;
        } else {
            return 0;
        }
    }

    @Deprecated
    public com.sk89q.worldedit.blocks.ItemType getLegacyType() {
        return com.sk89q.worldedit.blocks.ItemType.fromID(getLegacyId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemType && this.id.equals(((ItemType) obj).id);
    }
}
