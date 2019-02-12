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

package com.sk89q.worldedit.function.pattern;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Map;
import java.util.Map.Entry;

import static com.sk89q.worldedit.blocks.Blocks.resolveProperties;

public class StateApplyingPattern extends AbstractExtentPattern {

    private final Map<String, String> states;
    private Map<BlockType, Map<Property<Object>, Object>> cache = Maps.newHashMap();

    public StateApplyingPattern(Extent extent, Map<String, String> statesToSet) {
        super(extent);
        this.states = statesToSet;
    }

    @Override
    public BaseBlock apply(BlockVector3 position) {
        BlockState block = getExtent().getBlock(position);
        for (Entry<Property<Object>, Object> entry : cache
                .computeIfAbsent(block.getBlockType(), (b -> resolveProperties(states, b))).entrySet()) {
            block = block.with(entry.getKey(), entry.getValue());
        }
        return block.toBaseBlock();
    }
}
