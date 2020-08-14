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

package com.sk89q.worldedit.history;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.history.change.BlockChange;

import javax.annotation.Nullable;

/**
 * Provides context for undo and redo operations.
 *
 * <p>For example, {@link BlockChange}s take the {@link Extent} from the
 * context rather than store a reference to one.</p>
 */
public class UndoContext {

    private Extent extent;

    /**
     * Get the extent set on this context.
     *
     * @return an extent or null
     */
    public @Nullable Extent getExtent() {
        return extent;
    }

    /**
     * Set the extent on this context.
     *
     * @param extent an extent or null
     */
    public void setExtent(@Nullable Extent extent) {
        this.extent = extent;
    }
}
