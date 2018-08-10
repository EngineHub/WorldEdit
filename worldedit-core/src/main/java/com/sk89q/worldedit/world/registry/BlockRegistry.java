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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Provides information on blocks and provides methods to create them.
 */
public interface BlockRegistry {

    /**
     * Get the material for the given block.
     *
     * @param blockType the block
     * @return the material, or null if the material information is not known
     */
    @Nullable
    BlockMaterial getMaterial(BlockType blockType);

    /**
     * Get an unmodifiable map of states for this block.
     *
     * @param blockType the block
     * @return a map of states where the key is the state's ID
     */
    Map<String, ? extends Property> getProperties(BlockType blockType);

}
