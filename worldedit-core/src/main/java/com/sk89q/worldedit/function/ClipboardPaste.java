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

package com.sk89q.worldedit.function;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

public final class ClipboardPaste implements RegionFunction {

    private final Extent destination;
    private final ClipboardHolder holder;
    private final boolean ignoreAirBlocks;
    private final boolean ignoreStructureVoidBlocks;
    private final boolean usingOrigin;
    private final boolean pasteEntities;
    private final boolean pasteBiomes;
    private final Mask sourceMask;

    public ClipboardPaste(Extent destination, ClipboardHolder holder, boolean ignoreAirBlocks,
                          boolean ignoreStructureVoidBlocks, boolean usingOrigin, boolean pasteEntities,
                          boolean pasteBiomes, Mask sourceMask) {
        this.destination = destination;
        this.holder = holder;
        this.ignoreAirBlocks = ignoreAirBlocks;
        this.ignoreStructureVoidBlocks = ignoreStructureVoidBlocks;
        this.usingOrigin = usingOrigin;
        this.pasteEntities = pasteEntities;
        this.pasteBiomes = pasteBiomes;
        this.sourceMask = sourceMask;
    }

    @Override
    public boolean apply(BlockVector3 position) throws MaxChangedBlocksException {
        Clipboard clipboard = holder.getClipboard();
        Region region = clipboard.getRegion();
        BlockVector3 centerOffset = region.getCenter().toBlockPoint().subtract(clipboard.getOrigin());
        BlockVector3 transformedCenterOffset = holder.getTransform().apply(centerOffset.toVector3()).toBlockPoint();

        ForwardExtentCopy operation = (ForwardExtentCopy) holder
            .createPaste(destination)
            .to(usingOrigin ? position : position.subtract(transformedCenterOffset))
            .ignoreAirBlocks(ignoreAirBlocks)
            .ignoreStructureVoidBlocks(ignoreStructureVoidBlocks)
            .copyEntities(pasteEntities)
            .copyBiomes(pasteBiomes)
            .maskSource(sourceMask)
            .build();

        Operations.completeLegacy(operation);
        return operation.getAffected() > 0;
    }
}
