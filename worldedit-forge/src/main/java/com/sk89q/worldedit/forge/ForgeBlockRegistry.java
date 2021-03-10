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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.forge.internal.ForgeTransmogrifier;
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

public class ForgeBlockRegistry extends BundledBlockRegistry {

    private final Map<net.minecraft.block.BlockState, ForgeBlockMaterial> materialMap = new HashMap<>();

    @Override
    public Component getRichName(BlockType blockType) {
        return TranslatableComponent.of(ForgeAdapter.adapt(blockType).getTranslationKey());
    }

    @Override
    public BlockMaterial getMaterial(BlockType blockType) {
        Block block = ForgeAdapter.adapt(blockType);
        if (block == null) {
            return super.getMaterial(blockType);
        }
        return materialMap.computeIfAbsent(
            block.getDefaultState(),
            s -> new ForgeBlockMaterial(s.getMaterial(), s, super.getMaterial(blockType))
        );
    }

    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        Block block = ForgeAdapter.adapt(blockType);
        Map<String, Property<?>> map = new TreeMap<>();
        Collection<net.minecraft.state.Property<?>> propertyKeys = block
            .getDefaultState()
            // func_235904_r_ == getProperties
            .func_235904_r_();
        for (net.minecraft.state.Property<?> key : propertyKeys) {
            map.put(key.getName(), ForgeTransmogrifier.transmogToWorldEditProperty(key));
        }
        return map;
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        net.minecraft.block.BlockState equivalent = ForgeAdapter.adapt(state);
        return OptionalInt.of(Block.getStateId(equivalent));
    }
}
