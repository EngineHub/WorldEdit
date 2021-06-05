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

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

class FlatMapLifecycled<T, U> implements Lifecycled<U> {
    private final LifecycledCallbackHandler<U> events = new LifecycledCallbackHandler<>(this);
    private Lifecycled<U> mapped;
    private Token<FlatMapLifecycled<T, U>> mappedToken;
    @Nullable
    private U value;

    FlatMapLifecycled(Lifecycled<T> upstream, Function<T, Lifecycled<U>> mapper) {
        upstream.events().onInvalidated(this, (this$, up) -> {
            boolean fire = this$.value != null;
            this$.value = null;
            // drop `mapped` hooks if needed
            this$.mappedToken = null;
            this$.mapped = null;
            if (fire) {
                this$.events.fireInvalidated();
            }
        });
        upstream.events().onNewValue(this, (this$, up) -> {
            this$.mapped = mapper.apply(up.valueOrThrow());
            this$.mappedToken = new Token<>(this$);
            mapped.events().onInvalidated(this$.mappedToken, (token, mapped$) -> {
                boolean fire = token.inner.value != null;
                token.inner.value = null;
                // note we do not drop the token here, onNewValue may be called again
                if (fire) {
                    this$.events.fireInvalidated();
                }
            });
            mapped.events().onNewValue(this$.mappedToken, (token, mapped$) -> {
                U newValue = mapped$.valueOrThrow();
                boolean fire = token.inner.value != newValue;
                token.inner.value = newValue;
                if (fire) {
                    this$.events.fireOnNewValue();
                }
            });
        });
    }

    @Override
    public Optional<U> value() {
        return Optional.ofNullable(value);
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public Events<U> events() {
        return events;
    }
}
