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

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.sponge.internal.SpongeTransmogrifier;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import net.minecraft.world.level.block.Block;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.StateProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;

public class SpongeBlockRegistry implements BlockRegistry {

    private final Map<org.spongepowered.api.block.BlockState, SpongeBlockMaterial> materialMap =
        new HashMap<>();

    @Override
    public Component getRichName(BlockType blockType) {
        return SpongeTextAdapter.convert(Sponge.game().registry(RegistryTypes.BLOCK_TYPE)
            .value(ResourceKey.resolve(blockType.id())).asComponent());
    }

    @Override
    public BlockMaterial getMaterial(BlockType blockType) {
        org.spongepowered.api.block.BlockType spongeBlockType =
            Sponge.game().registry(RegistryTypes.BLOCK_TYPE)
                .value(ResourceKey.resolve(blockType.id()));
        return materialMap.computeIfAbsent(
            spongeBlockType.defaultState(),
            m -> {
                net.minecraft.world.level.block.state.BlockState blockState =
                    (net.minecraft.world.level.block.state.BlockState) m;
                return new SpongeBlockMaterial(
                    blockState
                );
            }
        );
    }

    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        org.spongepowered.api.block.BlockType spongeBlockType =
            Sponge.game().registry(RegistryTypes.BLOCK_TYPE)
                .value(ResourceKey.resolve(blockType.id()));
        Map<String, Property<?>> map = new TreeMap<>();
        Collection<StateProperty<?>> propertyKeys = spongeBlockType
            .defaultState().stateProperties();
        for (StateProperty<?> key : propertyKeys) {
            map.put(key.name(), SpongeTransmogrifier.transmogToWorldEditProperty(key));
        }
        return map;
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        org.spongepowered.api.block.BlockState equivalent = SpongeAdapter.adapt(state);
        return OptionalInt.of(Block.getId(
            (net.minecraft.world.level.block.state.BlockState) equivalent
        ));
    }
}
