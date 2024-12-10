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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BundledItemRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;

@SuppressWarnings("removal")
public class SpongeItemRegistry extends BundledItemRegistry {

    @Override
    public Component getRichName(ItemType itemType) {
        return SpongeTextAdapter.convert(Sponge.game().registry(RegistryTypes.ITEM_TYPE)
            .value(ResourceKey.resolve(itemType.id())).asComponent());
    }

    @Override
    public Component getRichName(BaseItemStack itemStack) {
        return GsonComponentSerializer.INSTANCE.deserialize(
            net.minecraft.network.chat.Component.Serializer.toJson(
                ((ItemStack) (Object) SpongeAdapter.adapt(itemStack)).getItemName(),
                ((MinecraftServer) Sponge.server()).registryAccess()
            )
        );
    }

}
