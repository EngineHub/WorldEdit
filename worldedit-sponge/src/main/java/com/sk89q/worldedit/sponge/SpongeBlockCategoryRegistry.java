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

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockCategoryRegistry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;

import java.util.Set;
import java.util.stream.Collectors;

public class SpongeBlockCategoryRegistry implements BlockCategoryRegistry {
    @Override
    public Set<BlockType> getCategorisedByName(String category) {
        Registry<org.spongepowered.api.block.BlockType> blockTypeRegistry =
            Sponge.game().registry(RegistryTypes.BLOCK_TYPE);

        return blockTypeRegistry.taggedValues(Tag.of(RegistryTypes.BLOCK_TYPE, ResourceKey.resolve(category)))
            .stream()
            .map(blockType -> BlockType.REGISTRY.get(blockTypeRegistry.valueKey(blockType).formatted()))
            .collect(Collectors.toSet());
    }
}
