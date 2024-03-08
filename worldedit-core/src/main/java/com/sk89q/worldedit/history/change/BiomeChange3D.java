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
import com.sk89q.worldedit.world.biome.BiomeType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a biome change that may be undone or replayed.
 *
 * <p>This biome change does not have an {@link Extent} assigned to it because
 * one will be taken from the passed {@link UndoContext}. If the context
 * does not have an extent (it is null), cryptic errors may occur.</p>
 *
 * @param position the position
 * @param previous the previous biome
 * @param current the current biome
 */
public record BiomeChange3D(BlockVector3 position, BiomeType previous, BiomeType current) implements Change {

    /**
     * Create a new biome change.
     *
     */
    public BiomeChange3D {
        checkNotNull(position);
        checkNotNull(previous);
        checkNotNull(current);
    }

    /**
     * Get the position.
     *
     * @return the position
     * @deprecated Use {@link #position()}.
     */
    @Deprecated(forRemoval = true)
    public BlockVector3 getPosition() {
        return position;
    }

    /**
     * Get the previous biome.
     *
     * @return the previous biome
     * @deprecated Use {@link #previous()}.
     */
    @Deprecated(forRemoval = true)
    public BiomeType getPrevious() {
        return previous;
    }

    /**
     * Get the current biome.
     *
     * @return the current biome
     * @deprecated Use {@link #current()}.
     */
    @Deprecated(forRemoval = true)
    public BiomeType getCurrent() {
        return current;
    }

    @Override
    public void undo(UndoContext context) throws WorldEditException {
        checkNotNull(context.getExtent()).setBiome(position, previous);
    }

    @Override
    public void redo(UndoContext context) throws WorldEditException {
        checkNotNull(context.getExtent()).setBiome(position, current);
    }

}
