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

package com.sk89q.worldedit.history.change;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a block change that may be undone or replayed.
 *
 * <p>This block change does not have an {@link Extent} assigned to it because
 * one will be taken from the passed {@link UndoContext}. If the context
 * does not have an extent (it is null), cryptic errors may occur.</p>
 *
 * @param position the position
 * @param previous the previous block
 * @param current the current block
 */
public record BlockChange(BlockVector3 position, BaseBlock previous, BaseBlock current) implements Change {

    /**
     * Create a new block change.
     */
    public BlockChange {
        checkNotNull(position);
        checkNotNull(previous);
        checkNotNull(current);
    }

    /**
     * Create a new block change.
     *
     * @param position the position
     * @param previous the previous block
     * @param current the current block
     */
    public <BP extends BlockStateHolder<BP>, BC extends BlockStateHolder<BC>> BlockChange(BlockVector3 position, BP previous, BC current) {
        this(position, previous.toBaseBlock(), current.toBaseBlock());
    }

    /**
     * Get the position.
     *
     * @return the position
     * @deprecated use {@link #position()}
     */
    @Deprecated(forRemoval = true)
    public BlockVector3 getPosition() {
        return position;
    }

    /**
     * Get the previous block.
     *
     * @return the previous block
     * @deprecated use {@link #previous()}
     */
    @Deprecated(forRemoval = true)
    public BaseBlock getPrevious() {
        return previous;
    }

    /**
     * Get the current block.
     *
     * @return the current block
     * @deprecated use {@link #current()}
     */
    @Deprecated(forRemoval = true)
    public BaseBlock getCurrent() {
        return current;
    }

    @Override
    public void undo(UndoContext context) throws WorldEditException {
        checkNotNull(context.getExtent()).setBlock(position, previous);
    }

    @Override
    public void redo(UndoContext context) throws WorldEditException {
        checkNotNull(context.getExtent()).setBlock(position, current);
    }

}
