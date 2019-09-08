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

import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BundledItemRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;

public class ForgeItemRegistry extends BundledItemRegistry {

    @Nullable
    @Override
    public String getName(ItemType itemType) {
        if (FMLLoader.getDist().isClient()) {
            final Item item = RegistryManager.ACTIVE.getRegistry(Item.class)
                    .getValue(ResourceLocation.tryCreate(itemType.getId()));
            if (item != null) {
                return I18n.format(item.getTranslationKey());
            }
        }
        return super.getName(itemType);
    }
}
