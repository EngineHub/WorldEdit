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

package com.sk89q.worldedit.blocks;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Collection;
import java.util.Map;

/**
 * Block-related utility methods.
 */
public final class Blocks {

    private Blocks() {
    }

    /**
     * Checks whether a given block is in a list of base blocks.
     *
     * @param collection the collection
     * @param o the block
     * @return true if the collection contains the given block
     */
    public static <B extends BlockStateHolder<B>> boolean containsFuzzy(Collection<? extends BlockStateHolder<?>> collection, B o) {
        // Allow masked data in the searchBlocks to match various types
        for (BlockStateHolder<?> b : collection) {
            if (b.equalsFuzzy(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a string->string map to find the matching Property and values for the given BlockType.
     *
     * @param states the desired states and values
     * @param type the block type to get properties and values for
     * @return a property->value map
     */
    public static Map<Property<Object>, Object> resolveProperties(Map<String, String> states, BlockType type) {
        Map<String, ? extends Property<?>> existing = type.getPropertyMap();
        Map<Property<Object>, Object> newMap = Maps.newHashMap();
        states.forEach((key, value) -> {
            @SuppressWarnings("unchecked")
            Property<Object> prop = (Property<Object>) existing.get(key);
            if (prop == null) return;
            Object val = null;
            try {
                val = prop.getValueFor(value);
            } catch (IllegalArgumentException ignored) {
            }
            if (val == null) return;
            newMap.put(prop, val);
        });
        return newMap;
    }
}
