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
 * Arranger that delegates to another if enabled, otherwise just passes on the data.
 */
public final class OptionalArranger extends ForwardingArranger {

    public static OptionalArranger wrap(Arranger delegate) {
        return new OptionalArranger(delegate);
    }

    private final Arranger delegate;
    private boolean enabled;

    private OptionalArranger(Arranger delegate) {
        this.delegate = delegate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onWrite(ArrangerContext context, WorldActionBuffer buffer) {
        if (enabled) {
            delegate.onWrite(context, buffer);
        } else {
            super.onWrite(context, buffer);
        }
    }

    @Override
    public void onFlush(ArrangerContext context) {
        if (enabled) {
            delegate.onFlush(context);
        } else {
            super.onFlush(context);
        }
    }

}
