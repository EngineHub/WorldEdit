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

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A {@link Lifecycled} that never invalidates.
 */
public final class ConstantLifecycled<T> implements Lifecycled<T> {
    private final T value;

    public ConstantLifecycled(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Optional<T> value() {
        return Optional.of(value);
    }

    @Override
    public Events<T> events() {
        // Simple implementation, we just need to call onNewValue
        return new Events<T>() {
            @Override
            public <O> void onNewValue(O owner, BiConsumer<O, ? super Lifecycled<T>> callback) {
                callback.accept(owner, ConstantLifecycled.this);
            }

            @Override
            public <O> void onInvalidated(O owner, BiConsumer<O, ? super Lifecycled<T>> callback) {
            }
        };
    }
}
