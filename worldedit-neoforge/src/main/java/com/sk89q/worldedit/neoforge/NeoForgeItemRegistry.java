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

package com.sk89q.worldedit.neoforge;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.neoforge.internal.ComponentConverter;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BundledItemRegistry;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class NeoForgeItemRegistry extends BundledItemRegistry {

    @Override
    public Component getRichName(ItemType itemType) {
        return TranslatableComponent.of(
            NeoForgeAdapter.adapt(itemType).getDescriptionId()
        );
    }

    @Override
    public Component getRichName(BaseItemStack itemStack) {
        return GsonComponentSerializer.INSTANCE.deserialize(
            ComponentConverter.Serializer.toJson(
                NeoForgeAdapter.adapt(itemStack).getItemName(),
                ServerLifecycleHooks.getCurrentServer().registryAccess()
            )
        );
    }

}
