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

package com.sk89q.worldedit.session;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.Transform;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds the clipboard and the current transform on the clipboard.
 */
public class ClipboardHolder {

    private final Clipboard clipboard;
    private Transform transform = new Identity();

    /**
     * Create a new instance with the given clipboard.
     *
     * @param clipboard the clipboard
     */
    public ClipboardHolder(Clipboard clipboard) {
        checkNotNull(clipboard);
        this.clipboard = clipboard;
    }

    /**
     * Get the clipboard.
     *
     * <p>
     * If there is a transformation applied, the returned clipboard will
     * not contain its effect.
     * </p>
     *
     * @return the clipboard
     */
    public Clipboard getClipboard() {
        return clipboard;
    }

    /**
     * Set the transform.
     *
     * @param transform the transform
     */
    public void setTransform(Transform transform) {
        checkNotNull(transform);
        this.transform = transform;
    }

    /**
     * Get the transform.
     *
     * @return the transform
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Create a builder for an operation to paste this clipboard.
     *
     * @return a builder
     */
    public PasteBuilder createPaste(Extent targetExtent) {
        return new PasteBuilder(this, targetExtent);
    }

}
