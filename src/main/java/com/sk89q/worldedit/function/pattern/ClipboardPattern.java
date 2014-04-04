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

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A pattern that reads from {@link CuboidClipboard}.
 *
 * @deprecated May be removed without notice, but there is no direct replacement yet
 */
@Deprecated
public class ClipboardPattern extends AbstractPattern {

    private final CuboidClipboard clipboard;
    private final Vector size;

    /**
     * Create a new clipboard pattern.
     *
     * @param clipboard the clipboard
     */
    public ClipboardPattern(CuboidClipboard clipboard) {
        checkNotNull(clipboard);
        this.clipboard = clipboard;
        this.size = clipboard.getSize();
    }

    @Override
    public BaseBlock apply(Vector position) {
        int xp = Math.abs(position.getBlockX()) % size.getBlockX();
        int yp = Math.abs(position.getBlockY()) % size.getBlockY();
        int zp = Math.abs(position.getBlockZ()) % size.getBlockZ();

        return clipboard.getPoint(new Vector(xp, yp, zp));
    }

}
