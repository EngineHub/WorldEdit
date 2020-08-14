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
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.changeset.ChangeSet;

/**
 * Describes a change that can be undone or re-applied.
 */
public interface Change {

    /**
     * Perform an undo. This method may not be available if the object
     * was returned from {@link ChangeSet#forwardIterator()}.
     *
     * @param context a context for undo
     * @throws WorldEditException on an error
     */
    void undo(UndoContext context) throws WorldEditException;

    /**
     * Perform an redo. This method may not be available if the object
     * was returned from {@link ChangeSet#backwardIterator()} ()}.
     *
     * @param context a context for redo
     * @throws WorldEditException on an error
     */
    void redo(UndoContext context) throws WorldEditException;

}
