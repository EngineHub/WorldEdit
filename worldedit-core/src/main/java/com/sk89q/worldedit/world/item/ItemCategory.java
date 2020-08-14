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

package com.sk89q.worldedit.world.item;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

import java.util.Set;

/**
 * A category of items. This is due to the splitting up of
 * items such as wool into separate ids.
 */
public class ItemCategory extends Category<ItemType> implements Keyed {

    public static final NamespacedRegistry<ItemCategory> REGISTRY = new NamespacedRegistry<>("item tag");

    public ItemCategory(final String id) {
        super(id);
    }

    @Override
    protected Set<ItemType> load() {
        return WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries()
                .getItemCategoryRegistry().getAll(this);
    }

    /**
     * Checks whether the BaseItem is contained within
     * this category.
     *
     * @param baseItem The item
     * @return If it's a part of this category
     */
    public boolean contains(BaseItem baseItem) {
        return this.getAll().contains(baseItem.getType());
    }
}
