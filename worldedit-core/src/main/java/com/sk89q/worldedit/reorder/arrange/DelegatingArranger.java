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

package com.sk89q.worldedit.reorder.arrange;

import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;

/**
 * Delegates to another arranger, which may be changed on the fly.
 */
public final class DelegatingArranger implements Arranger {

    private volatile Arranger delegate;

    public DelegatingArranger(Arranger delegate) {
        this.delegate = delegate;
    }

    public Arranger getDelegate() {
        return delegate;
    }

    public void setDelegate(Arranger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onWrite(ArrangerContext context, WorldActionBuffer buffer) {
        delegate.onWrite(context, buffer);
    }

    @Override
    public void onFlush(ArrangerContext context) {
        delegate.onFlush(context);
    }
}
