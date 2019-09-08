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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.ItemCategoryRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.util.Set;
import java.util.stream.Collectors;

public class BukkitItemCategoryRegistry implements ItemCategoryRegistry {

    private Set<ItemType> getFromBukkitTag(Tag<Material> tag) {
        return tag.getValues().stream().map(BukkitAdapter::asItemType).collect(Collectors.toSet());
    }

    @Override
    public Set<ItemType> getCategorisedByName(String category) {
        String[] split = category.split(":");
        String namespace = split.length > 1 ? split[0] : "minecraft";
        String key =  split.length > 1 ? split[1] : category;
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, new NamespacedKey(namespace, key), Material.class);
        return getFromBukkitTag(tag);
    }
}
