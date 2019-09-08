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

package com.sk89q.worldedit.cli;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.cli.data.FileRegistries;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BundledBlockRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class CLIBlockRegistry extends BundledBlockRegistry {

    private Property<?> createProperty(String type, String key, List<String> values) {
        switch (type) {
            case "int": {
                List<Integer> fixedValues = values.stream().map(Integer::parseInt).collect(Collectors.toList());
                return new IntegerProperty(key, fixedValues);
            }
            case "bool": {
                List<Boolean> fixedValues = values.stream().map(Boolean::parseBoolean).collect(Collectors.toList());
                return new BooleanProperty(key, fixedValues);
            }
            case "enum": {
                return new EnumProperty(key, values);
            }
            case "direction": {
                List<Direction> fixedValues = values.stream().map(String::toUpperCase).map(Direction::valueOf).collect(Collectors.toList());
                return new DirectionalProperty(key, fixedValues);
            }
            default:
                throw new RuntimeException("Failed to create property");
        }
    }

    @Nullable
    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        Map<String, FileRegistries.BlockProperty> properties =
                CLIWorldEdit.inst.getFileRegistries().getDataFile().blocks.get(blockType.getId()).properties;
        return ImmutableMap.copyOf(Maps.transformEntries(properties,
                (Maps.EntryTransformer<String, FileRegistries.BlockProperty, Property<?>>)
                        (key, value) -> createProperty(value.type, key, value.values)));
    }
}
