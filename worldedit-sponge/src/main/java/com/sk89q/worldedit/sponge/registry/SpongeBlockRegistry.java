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

package com.sk89q.worldedit.sponge.registry;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import com.sk89q.worldedit.sponge.SpongeTextAdapter;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BundledBlockRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;

import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import javax.annotation.Nullable;

public class SpongeBlockRegistry extends BundledBlockRegistry {

    @Override
    public Component getRichName(BlockType blockType) {
        return SpongeTextAdapter.convert(SpongeAdapter.adapt(blockType).asComponent());
    }

    @Nullable
    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        Map<String, Property<?>> map = new TreeMap<>();
        org.spongepowered.api.block.BlockType type = SpongeAdapter.adapt(blockType);
        for (StateProperty<?> stateProperty : type.getStateProperties()) {
            map.put(stateProperty.getName(), SpongeAdapter.adaptProperty(stateProperty));
        }
        return map;
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        return PaletteTypes.BLOCK_STATE_PALETTE.get()
            .create(Sponge.getGame().registries(), RegistryTypes.BLOCK_TYPE)
            .get(SpongeAdapter.adapt(state));
    }
}
