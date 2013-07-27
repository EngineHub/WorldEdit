// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Pattern that repeats the clipboard.
 */
public class ClipboardPattern implements Pattern {
    
    private final CuboidClipboard clipboard;
    private final Vector size;

    /**
     * Construct the object.
     *
     * @param clipboard the clipboard
     */
    public ClipboardPattern(CuboidClipboard clipboard) {
        this.clipboard = clipboard;
        this.size = clipboard.getSize();
    }

    /**
     * Get the clipboard.
     * 
     * @return the clipboard
     */
    public CuboidClipboard getClipboard() {
        return clipboard;
    }

    @Override
    public BaseBlock next(Vector pos) {
        return next(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    @Override
    public BaseBlock next(int x, int y, int z) {
        int xp = Math.abs(x) % size.getBlockX();
        int yp = Math.abs(y) % size.getBlockY();
        int zp = Math.abs(z) % size.getBlockZ();

        return clipboard.getPoint(new Vector(xp, yp, zp));
    }

    @Override
    public String toString() {
        return String.format("ClipboardPattern(clipboard=%s)", clipboard);
    }
    
}
