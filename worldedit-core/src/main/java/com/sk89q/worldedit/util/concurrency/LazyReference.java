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

package com.sk89q.worldedit.util.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Thread-safe lazy reference.
 */
public class LazyReference<T> {

    public static <T> LazyReference<T> from(Supplier<T> valueComputation) {
        return new LazyReference<>(valueComputation);
    }

    // Memory saving technique: hold the computation info in the same reference field that we'll
    // put the value into, so the memory possibly retained by those parts is GC'able as soon as
    // it's no longer needed.

    private static final class RefInfo<T> {
        private final Lock lock = new ReentrantLock();
        private final Supplier<T> valueComputation;

        private RefInfo(Supplier<T> valueComputation) {
            this.valueComputation = valueComputation;
        }
    }

    private Object value;

    private LazyReference(Supplier<T> valueComputation) {
        this.value = new RefInfo<>(valueComputation);
    }

    // casts are safe, value is either RefInfo or T
    @SuppressWarnings("unchecked")
    public T getValue() {
        Object v = value;
        if (!(v instanceof RefInfo)) {
            return (T) v;
        }
        RefInfo<T> refInfo = (RefInfo<T>) v;
        refInfo.lock.lock();
        try {
            v = value;
            if (!(v instanceof RefInfo)) {
                return (T) v;
            }
            value = v = refInfo.valueComputation.get();
            return (T) v;
        } finally {
            refInfo.lock.unlock();
        }
    }

}
