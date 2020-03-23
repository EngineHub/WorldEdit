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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.item.ItemType;

import javax.annotation.Nullable;

/**
 * A item registry that uses {@link BundledItemRegistry} to serve information
 * about items.
 */
public class BundledItemRegistry implements ItemRegistry {

    private BundledItemData.ItemEntry getEntryById(ItemType itemType) {
        return BundledItemData.getInstance().findById(itemType.getId());
    }

    @Override
    public Component getRichName(ItemType itemType) {
        BundledItemData.ItemEntry itemEntry = getEntryById(itemType);
        if (itemEntry != null && !itemEntry.localizedName.equals("Air")) {
            // This is more likely to be "right", but not translated
            // Some vanilla MC items have overrides so we need this name here
            // Most platforms should be overriding this anyways, so it likely doesn't matter
            // too much!
            return TextComponent.of(itemEntry.localizedName);
        }
        return TranslatableComponent.of(
            TranslationManager.makeTranslationKey("item", itemType.getId())
        );
    }

    @Nullable
    @Override
    public ItemMaterial getMaterial(ItemType itemType) {
        return new PassthroughItemMaterial(BundledItemData.getInstance().getMaterialById(itemType.getId()));
    }
}
