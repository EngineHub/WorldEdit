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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ItemTypes {

    private ItemTypes() {
    }

    // TODO Add items.

    private static final Map<String, ItemType> itemMapping = new HashMap<>();

    static {
        for (Field field : ItemTypes.class.getFields()) {
            if (field.getType() == ItemType.class) {
                try {
                    registerItem((ItemType) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerItem(ItemType itemType) {
        if (itemMapping.containsKey(itemType.getId()) && !itemType.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Existing item with this ID already registered");
        }

        itemMapping.put(itemType.getId(), itemType);
    }

    @Nullable
    public static ItemType getItemType(String id) {
        return itemMapping.get(id);
    }
}
