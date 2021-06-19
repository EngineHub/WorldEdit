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
import javax.annotation.Nullable;

/**
 * A {@link Lifecycled} that can be directly called to {@linkplain #invalidate() invalidate} it or
 * set a {@linkplain #newValue(Object) new value}.
 */
public final class SimpleLifecycled<T> implements Lifecycled<T> {
    public static <T> SimpleLifecycled<T> valid(T value) {
        return new SimpleLifecycled<>(Objects.requireNonNull(value));
    }

    public static <T> SimpleLifecycled<T> invalid() {
        return new SimpleLifecycled<>(null);
    }

    private final LifecycledCallbackHandler<T> events = new LifecycledCallbackHandler<>(this);
    @Nullable
    private T value;

    private SimpleLifecycled(@Nullable T value) {
        this.value = value;
    }

    /**
     * Set the value of this lifecycled and fire the new value event.
     *
     * @param value the value
     */
    public void newValue(T value) {
        // Ensure lifecycle constraints are upheld.
        invalidate();
        this.value = Objects.requireNonNull(value);
        events.fireOnNewValue();
    }

    /**
     * Remove the value of this lifecycled and fire the invalidated event.
     */
    public void invalidate() {
        boolean fire = this.value != null;
        this.value = null;
        if (fire) {
            events.fireInvalidated();
        }
    }

    @Override
    public Optional<T> value() {
        return Optional.ofNullable(value);
    }

    @Override
    public Events<T> events() {
        return events;
    }
}
