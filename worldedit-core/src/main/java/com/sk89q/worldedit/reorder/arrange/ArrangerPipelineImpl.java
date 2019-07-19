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

import com.sk89q.worldedit.reorder.buffer.PlacementBuffer;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.base.Preconditions.checkState;

class ArrangerPipelineImpl implements ArrangerPipeline {

    // Make a stream that any interaction with will kill the parent stream.
    private static final PlacementOutputStream UNUSABLE_PLACEMENT_OUTPUT_STREAM =
        (PlacementOutputStream) Proxy.newProxyInstance(ArrangerPipelineImpl.class.getClassLoader(),
            new Class[] { PlacementOutputStream.class }, (proxy, method, args) -> {
                throw new IllegalStateException("Last Arranger interacted with output stream.");
            });

    private final List<Arranger> arrangers = new CopyOnWriteArrayList<>();

    @Override
    public List<Arranger> arrangers() {
        return arrangers;
    }

    @Override
    public PlacementOutputStream openStream() {
        checkState(arrangers.size() > 0, "No Arrangers registered.");
        ListIterator<Arranger> revIter = arrangers.listIterator(arrangers.size());
        PlacementOutputStream prev = UNUSABLE_PLACEMENT_OUTPUT_STREAM;
        while (revIter.hasPrevious()) {
            prev = new PlacementOutputStreamImpl(revIter.previous(), prev);
        }
        return prev;
    }

    private static final class PlacementOutputStreamImpl implements PlacementOutputStream {
        private final Arranger next;
        private final PlacementOutputStream nextContext;

        private PlacementOutputStreamImpl(Arranger next, PlacementOutputStream nextContext) {
            this.next = next;
            this.nextContext = nextContext;
        }

        @Override
        public void write(PlacementBuffer buffer) {
            next.onWrite(nextContext, buffer);
        }

        @Override
        public void flush() {
            next.onFlush(nextContext);
        }
    }

}
