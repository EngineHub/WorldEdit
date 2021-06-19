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
import java.util.function.Function;
import javax.annotation.Nullable;

class MapLifecycled<T, U> implements Lifecycled<U> {
    private final LifecycledCallbackHandler<U> events = new LifecycledCallbackHandler<>(this);
    private final Lifecycled<T> upstream;
    private final Function<T, U> mapper;
    @Nullable
    private U cache;
    private boolean computable;

    MapLifecycled(Lifecycled<T> upstream, Function<T, U> mapper) {
        this.upstream = upstream;
        this.mapper = mapper;
        upstream.events().onInvalidated(this, (this$, __) -> {
            boolean fire = this$.computable;
            this$.cache = null;
            this$.computable = false;
            if (fire) {
                this$.events.fireInvalidated();
            }
        });
        upstream.events().onNewValue(this,  (this$, __) -> {
            boolean fire = !this$.computable;
            this$.computable = true;
            if (fire) {
                this$.events.fireOnNewValue();
            }
        });
    }

    private void compute() {
        T value = upstream.value().orElseThrow(() ->
            new AssertionError("Upstream lost value without calling onInvalidated event")
        );
        this.cache = Objects.requireNonNull(mapper.apply(value), "Mapper cannot produce null");
    }

    @Override
    public Optional<U> value() {
        if (!computable) {
            return Optional.empty();
        }
        if (cache == null) {
            compute();
        }
        return Optional.of(cache);
    }

    @Override
    public boolean isValid() {
        return computable;
    }

    @Override
    public Events<U> events() {
        return events;
    }
}
