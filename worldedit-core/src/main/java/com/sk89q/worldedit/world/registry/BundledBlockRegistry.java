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

import com.sk89q.worldedit.blocks.BlockMaterial;
import com.sk89q.worldedit.blocks.type.BlockState;
import com.sk89q.worldedit.blocks.type.BlockStateHolder;
import com.sk89q.worldedit.blocks.type.BlockTypes;
import com.sk89q.worldedit.world.registry.state.State;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * A block registry that uses {@link BundledBlockData} to serve information
 * about blocks.
 */
public class BundledBlockRegistry implements BlockRegistry {

    @Nullable
    @Override
    public BlockState createFromId(String id) {
        return BlockTypes.getBlockType(id).getDefaultState();
    }

    @Nullable
    @Override
    public BlockMaterial getMaterial(String id) {
        return BundledBlockData.getInstance().getMaterialById(id);
    }

    @Nullable
    @Override
    public Map<String, ? extends State> getStates(BlockStateHolder block) {
        return BundledBlockData.getInstance().getStatesById(block.getBlockType().getId());
    }

}
