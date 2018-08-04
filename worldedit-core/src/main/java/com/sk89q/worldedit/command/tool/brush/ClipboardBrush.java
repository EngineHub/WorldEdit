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
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

public class ClipboardBrush implements Brush {

    private ClipboardHolder holder;
    private boolean ignoreAirBlocks;
    private boolean usingOrigin;

    public ClipboardBrush(ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin) {
        this.holder = holder;
        this.ignoreAirBlocks = ignoreAirBlocks;
        this.usingOrigin = usingOrigin;
    }

    @Override
    public void build(EditSession editSession, Vector position, Pattern pattern, double size) throws MaxChangedBlocksException {
        Clipboard clipboard = holder.getClipboard();
        Region region = clipboard.getRegion();
        Vector centerOffset = region.getCenter().subtract(clipboard.getOrigin());

        Operation operation = holder
                .createPaste(editSession)
                .to(usingOrigin ? position : position.subtract(centerOffset))
                .ignoreAirBlocks(ignoreAirBlocks)
                .build();

        Operations.completeLegacy(operation);
    }

}
