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

package com.sk89q.worldedit.world.block;

import com.google.errorprone.annotations.Immutable;
import com.sk89q.worldedit.registry.state.Property;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;

import java.util.Collections;
import java.util.Map;

/**
 * A compact immutable index of the possible states for one block type.
 */
@Immutable
abstract class BlockTypeStateList extends AbstractObjectList<BlockState> {

    static BlockTypeStateList createFor(BlockType blockType) {
        if (blockType.getProperties().isEmpty()) {
            return new SingletonBlockTypeStateList(
                new BlockState(blockType, Collections.emptyMap(), 0)
            );
        }
        return new DefaultBlockTypeStateList(blockType);
    }

    /**
     * Calculates the state index for a complete property map.
     *
     * @param state a map containing one value for each property
     * @return the matching state index
     */
    public abstract int calculateIndex(Map<Property<?>, ?> state);

    /**
     * Calculates the state index after changing one property.
     *
     * @param currentIndex the current state index
     * @param property the property being changed
     * @param oldValue the current property value
     * @param newValue the new property value
     * @return the new index, or {@code -1} if the property or value is invalid
     */
    public abstract int updateIndexOrInvalid(
        int currentIndex,
        Property<?> property,
        Object oldValue,
        Object newValue
    );
}
