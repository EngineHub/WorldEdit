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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;

public class ClipboardBrush implements Brush {

    private Clipboard clipboard;
    private boolean noAir;

    public ClipboardBrush(Clipboard clipboard, boolean noAir) {
        this.clipboard = clipboard;
        this.noAir = noAir;
    }

    @Override
    public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
        Region region = clipboard.getRegion();
        Vector centerOffset = region.getCenter().subtract(region.getMinimumPoint());
        ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), editSession, pos.subtract(centerOffset));
        if (noAir) {
            copy.setSourceMask(new ExistingBlockMask(clipboard));
        }
        Operations.completeLegacy(copy);
    }

}
