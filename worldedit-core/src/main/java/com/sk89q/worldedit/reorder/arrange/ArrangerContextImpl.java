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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;

class ArrangerContextImpl implements ArrangerContext {

    static WorldActionOutputStream newStream(Iterable<Arranger> arrangers) {
        List<Arranger> arrList = ImmutableList.copyOf(arrangers);
        checkState(arrList.size() > 0, "No Arrangers registered.");
        ListIterator<Arranger> revIter = arrList.listIterator(arrList.size());
        ArrangerContext prev = new FinalArrangerContextImpl();
        while (revIter.hasPrevious()) {
            prev = new ArrangerContextImpl(revIter.previous(), prev);
        }
        return prev;
    }

    private final Reference2ObjectMap<AttributeKey<?>, Object> attrMap
        = new Reference2ObjectOpenHashMap<>();
    private final Arranger next;
    private final ArrangerContext nextContext;

    private ArrangerContextImpl(Arranger next, ArrangerContext nextContext) {
        this.next = next;
        this.nextContext = nextContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T attr(AttributeKey<T> key) {
        return (T) attrMap.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T attrOrInit(AttributeKey<T> key, Supplier<T> init) {
        return (T) attrMap.computeIfAbsent(key, k -> init.get());
    }

    @Override
    public <T> void attr(AttributeKey<T> key, T value) {
        attrMap.put(key, value);
    }

    @Override
    public void write(WorldActionBuffer buffer) {
        next.onWrite(nextContext, buffer);
    }

    @Override
    public void flush() {
        next.onFlush(nextContext);
    }

    private static final class FinalArrangerContextImpl extends ArrangerContextImpl {
        private FinalArrangerContextImpl() {
            super(null, null);
        }
    }

}
