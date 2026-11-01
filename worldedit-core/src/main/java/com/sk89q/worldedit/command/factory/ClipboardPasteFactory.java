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

package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.function.ClipboardPaste;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.session.ClipboardHolder;

/**
 * A factory for creating clipboard paste functions with given parameters.
 */
public final class ClipboardPasteFactory implements Contextual<RegionFunction> {

    private final ClipboardHolder holder;
    private final boolean ignoreAirBlocks;
    private final boolean ignoreStructureVoidBlocks;
    private final boolean usingOrigin;
    private final boolean pasteEntities;
    private final boolean pasteBiomes;
    private final Mask sourceMask;

    /**
     * Creates a new ClipboardPasteFactory with the specified parameters.
     *
     * @param holder The clipboard holder containing the clipboard to paste.
     * @param ignoreAirBlocks Whether to ignore air blocks during the paste operation.
     * @param ignoreStructureVoidBlocks Whether to ignore structure void blocks during the paste operation.
     * @param usingOrigin Whether to use the origin of the clipboard for positioning.
     * @param pasteEntities Whether to paste entities from the clipboard.
     * @param pasteBiomes Whether to paste biomes from the clipboard.
     * @param sourceMask The source mask to apply during the paste operation, or null if no source mask is given.
     */
    public ClipboardPasteFactory(ClipboardHolder holder, boolean ignoreAirBlocks,
                                 boolean ignoreStructureVoidBlocks, boolean usingOrigin,
                                 boolean pasteEntities, boolean pasteBiomes, Mask sourceMask) {
        this.holder = new ClipboardHolder(holder.getClipboard());
        this.holder.setTransform(holder.getTransform());
        this.ignoreAirBlocks = ignoreAirBlocks;
        this.ignoreStructureVoidBlocks = ignoreStructureVoidBlocks;
        this.usingOrigin = usingOrigin;
        this.pasteEntities = pasteEntities;
        this.pasteBiomes = pasteBiomes;
        this.sourceMask = sourceMask;
    }

    @Override
    public RegionFunction createFromContext(EditContext context) {
        return new ClipboardPaste(
            context.getDestination(),
            holder,
            ignoreAirBlocks,
            ignoreStructureVoidBlocks,
            usingOrigin,
            pasteEntities,
            pasteBiomes,
            sourceMask
        );
    }

    @Override
    public String toString() {
        return "paste clipboard";
    }
}
