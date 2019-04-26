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

package com.sk89q.worldedit.session;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;

/**
 * Builds an operation to paste the contents of a clipboard.
 */
public class PasteBuilder {

    private final Clipboard clipboard;
    private final Transform transform;
    private final Extent targetExtent;

    private Mask sourceMask = Masks.alwaysTrue();

    private BlockVector3 to = BlockVector3.ZERO;
    private boolean ignoreAirBlocks;
    private boolean copyEntities = true; // default because it used to be this way
    private boolean copyBiomes;

    /**
     * Create a new instance.
     *
     * @param holder the clipboard holder
     * @param targetExtent an extent
     */
    PasteBuilder(ClipboardHolder holder, Extent targetExtent) {
        checkNotNull(holder);
        checkNotNull(targetExtent);
        this.clipboard = holder.getClipboard();
        this.transform = holder.getTransform();
        this.targetExtent = targetExtent;
    }

    /**
     * Set the target location.
     *
     * @param to the target location
     * @return this builder instance
     */
    public PasteBuilder to(BlockVector3 to) {
        this.to = to;
        return this;
    }

    /**
     * Set a custom mask of blocks to ignore from the source.
     * This provides a more flexible alternative to {@link #ignoreAirBlocks(boolean)}, for example
     * one might want to ignore structure void if copying a Minecraft Structure, etc.
     *
     * @param sourceMask
     * @return this builder instance
     */
    public PasteBuilder maskSource(Mask sourceMask) {
        if (sourceMask == null) {
            this.sourceMask = Masks.alwaysTrue();
            return this;
        }
        this.sourceMask = sourceMask;
        return this;
    }

    /**
     * Set whether air blocks in the source are skipped over when pasting.
     *
     * @return this builder instance
     */
    public PasteBuilder ignoreAirBlocks(boolean ignoreAirBlocks) {
        this.ignoreAirBlocks = ignoreAirBlocks;
        return this;
    }

    /**
     * Set whether the copy should include source entities.
     * Note that this is true by default for legacy reasons.
     *
     * @param copyEntities
     * @return this builder instance
     */
    public PasteBuilder copyEntities(boolean copyEntities) {
        this.copyEntities = copyEntities;
        return this;
    }

    /**
     * Set whether the copy should include source biomes (if available).
     *
     * @param copyBiomes
     * @return this builder instance
     */
    public PasteBuilder copyBiomes(boolean copyBiomes) {
        this.copyBiomes = copyBiomes;
        return this;
    }

    /**
     * Build the operation.
     *
     * @return the operation
     */
    public Operation build() {
        BlockTransformExtent extent = new BlockTransformExtent(clipboard, transform);
        ForwardExtentCopy copy = new ForwardExtentCopy(extent, clipboard.getRegion(), clipboard.getOrigin(), targetExtent, to);
        copy.setTransform(transform);
        if (ignoreAirBlocks) {
            copy.setSourceMask(sourceMask == Masks.alwaysTrue() ? new ExistingBlockMask(clipboard)
                    : new MaskIntersection(sourceMask, new ExistingBlockMask(clipboard)));
        } else {
            copy.setSourceMask(sourceMask);
        }
        copy.setCopyingEntities(copyEntities);
        copy.setCopyingBiomes(copyBiomes && clipboard.hasBiomes());
        return copy;
    }

}
