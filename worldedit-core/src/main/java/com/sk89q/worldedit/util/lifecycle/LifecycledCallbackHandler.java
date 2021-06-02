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

package com.sk89q.worldedit.util.lifecycle;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * Convenience class for implementing the callbacks of {@link Lifecycled}.
 */
public class LifecycledCallbackHandler<T> implements Lifecycled.Events<T> {
    private final Lifecycled<T> lifecycled;
    private final Lock lock = new ReentrantLock();
    private final Map<Object, BiConsumer<?, ? super Lifecycled<T>>> onInvalidatedCallbacks =
        new WeakHashMap<>();
    private final Map<Object, BiConsumer<?, ? super Lifecycled<T>>> onNewValueCallbacks =
        new WeakHashMap<>();

    public LifecycledCallbackHandler(Lifecycled<T> lifecycled) {
        this.lifecycled = lifecycled;
    }

    @Override
    public <O> void onInvalidated(O owner, BiConsumer<O, ? super Lifecycled<T>> callback) {
        lock.lock();
        try {
            onInvalidatedCallbacks.put(owner, callback);
            if (!lifecycled.isValid()) {
                callback.accept(owner, lifecycled);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <O> void onNewValue(O owner, BiConsumer<O, ? super Lifecycled<T>> callback) {
        lock.lock();
        try {
            onNewValueCallbacks.put(owner, callback);
            if (lifecycled.isValid()) {
                callback.accept(owner, lifecycled);
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Fire {@link #onInvalidated(Object, BiConsumer)} callbacks.
     */
    public void fireInvalidated() {
        lock.lock();
        try {
            for (Map.Entry<Object, BiConsumer<?, ? super Lifecycled<T>>> callback : onInvalidatedCallbacks.entrySet()) {
                Object owner = callback.getKey();
                if (owner == null) {
                    // GC'd, continue
                    continue;
                }
                @SuppressWarnings("unchecked")
                BiConsumer<Object, ? super Lifecycled<T>> cast =
                    (BiConsumer<Object, ? super Lifecycled<T>>) callback.getValue();
                cast.accept(owner, lifecycled);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Fire {@link #onNewValue(Object, BiConsumer)} callbacks, the {@link Lifecycled#value()} must
     * be available.
     */
    public void fireOnNewValue() {
        lock.lock();
        try {
            for (Map.Entry<Object, BiConsumer<?, ? super Lifecycled<T>>> callback : onNewValueCallbacks.entrySet()) {
                Object owner = callback.getKey();
                if (owner == null) {
                    // GC'd, continue
                    continue;
                }
                @SuppressWarnings("unchecked")
                BiConsumer<Object, ? super Lifecycled<T>> cast =
                    (BiConsumer<Object, ? super Lifecycled<T>>) callback.getValue();
                cast.accept(owner, lifecycled);
            }
        } finally {
            lock.unlock();
        }
    }
}
