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

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.registry.state.Property;

import java.util.Map;

/**
 * {@link BlockTypeStateList} with only one possible state.
 */
// Suppress Immutable: Properly annotating BlockState with Immutable is for the future
@SuppressWarnings("Immutable")
final class SingletonBlockTypeStateList extends BlockTypeStateList {
    private final BlockState state;

    SingletonBlockTypeStateList(BlockState state) {
        this.state = state;
    }

    @Override
    public BlockState get(int index) {
        Preconditions.checkElementIndex(index, 1);
        return state;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int calculateIndex(Map<Property<?>, ?> state) {
        if (!state.isEmpty()) {
            throw new IllegalArgumentException("No properties expected for singleton state");
        }
        return 0;
    }

    @Override
    public int updateIndexOrInvalid(int currentIndex, Property<?> property, Object oldValue, Object newValue) {
        return -1;
    }
}
