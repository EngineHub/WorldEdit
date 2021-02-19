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

package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.fabric.internal.FabricTransmogrifier;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BundledBlockRegistry;
import net.minecraft.block.Block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;

public class FabricBlockRegistry extends BundledBlockRegistry {

    private final Map<net.minecraft.block.BlockState, FabricBlockMaterial> materialMap = new HashMap<>();

    @Override
    public Component getRichName(BlockType blockType) {
        return TranslatableComponent.of(FabricAdapter.adapt(blockType).getTranslationKey());
    }

    @Override
    public BlockMaterial getMaterial(BlockType blockType) {
        Block block = FabricAdapter.adapt(blockType);
        return materialMap.computeIfAbsent(
            block.getDefaultState(),
            m -> new FabricBlockMaterial(m.getMaterial(), m, super.getMaterial(blockType))
        );
    }

    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        Block block = FabricAdapter.adapt(blockType);
        Map<String, Property<?>> map = new TreeMap<>();
        Collection<net.minecraft.state.property.Property<?>> propertyKeys = block
                .getDefaultState()
                .getProperties();
        for (net.minecraft.state.property.Property<?> key : propertyKeys) {
            map.put(key.getName(), FabricTransmogrifier.transmogToWorldEditProperty(key));
        }
        return map;
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        net.minecraft.block.BlockState equivalent = FabricAdapter.adapt(state);
        return OptionalInt.of(Block.getRawIdFromState(equivalent));
    }
}
