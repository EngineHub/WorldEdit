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

package com.sk89q.worldedit.action;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the placement of a block.
 */
public final class BlockPlacement implements BlockWorldAction {

    public static BlockPlacement create(BlockVector3 location, BaseBlock oldBlock, BaseBlock block) {
        return create(location, oldBlock, block, SideEffect.getDefault());
    }

    public static BlockPlacement create(BlockVector3 location, BaseBlock oldBlock, BaseBlock block, Collection<SideEffect> sideEffects) {
        return new BlockPlacement(location, oldBlock, block, sideEffects);
    }

    private final BlockVector3 position;
    private final BaseBlock oldBlock;
    private final BaseBlock block;
    private final ImmutableSet<SideEffect> sideEffects;

    private BlockPlacement(BlockVector3 position, BaseBlock oldBlock, BaseBlock block, Collection<SideEffect> sideEffects) {
        this.position = checkNotNull(position);
        this.oldBlock = oldBlock;
        this.block = checkNotNull(block);
        this.sideEffects = Sets.immutableEnumSet(sideEffects);
    }

    @Override
    public BlockVector3 getPosition() {
        return position;
    }

    public BaseBlock getOldBlock() {
        return oldBlock;
    }

    public BaseBlock getBlock() {
        return block;
    }

    public ImmutableSet<SideEffect> getSideEffects() {
        return sideEffects;
    }

    public BlockPlacement withSideEffects(Collection<SideEffect> sideEffects) {
        return new BlockPlacement(position, oldBlock, block, sideEffects);
    }

    @Override
    public void apply(World world) throws WorldEditException {
        world.setBlock(position, block, sideEffects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, block, sideEffects);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        BlockPlacement p = (BlockPlacement) obj;
        return Objects.equals(position, p.position) && Objects.equals(block, p.block)
            && Objects.equals(sideEffects, p.sideEffects);
    }

}
