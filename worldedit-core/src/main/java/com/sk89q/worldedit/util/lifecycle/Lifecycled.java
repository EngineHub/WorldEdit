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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an object with a simple valid/invalid lifecycle.
 *
 * <p>
 * A lifecycled object will start with no value, then trigger
 * {@link Events#onNewValue(Object, BiConsumer)} callbacks when it gets one, and
 * {@link Events#onInvalidated(Object, BiConsumer)} callbacks when it loses it. A full
 * invalidated->new value cycle is called a "reload".
 * </p>
 *
 * <p>
 * Downstream lifecycled objects can be derived using functional methods, and share some
 * common rules. They will apply the operation sometime before the result is needed, either
 * eagerly or lazily. They will re-do the operation after the upstream {@link Lifecycled} is
 * reloaded.
 * </p>
 *
 * <p>
 * Unless specified, {@link Lifecycled} objects are <em>not</em> thread-safe. However, the
 * {@link Events} objects are, and callbacks may be added from any thread.
 * </p>
 *
 * @param <T> the value type
 */
public interface Lifecycled<T> {

    interface Events<T> {
        /**
         * Add a callback for when this lifecycled is given a new value. Will be called immediately
         * if this lifecycled is currently valid.
         *
         * <p>
         * The callback should not reference the owner, it must only access it via the parameter.
         * This ensures that the owner will be GC-able, otherwise it may be stuck in a reference
         * loop.
         * </p>
         *
         * @param owner when the owner is GC'd, the callback is removed
         * @param callback the callback, will be passed the lifecycled object
         */
        <O> void onNewValue(O owner, BiConsumer<O, ? super Lifecycled<T>> callback);

        /**
         * Add a callback for when this lifecycled is invalidated. Will be called immediately if
         * this lifecycled is currently invalid.
         *
         * <p>
         * The callback should not reference the owner, it must only access it via the parameter.
         * This ensures that the owner will be GC-able, otherwise it may be stuck in a reference
         * loop.
         * </p>
         *
         * @param owner when the owner is GC'd, the callback is removed
         * @param callback the callback, will be passed the lifecycled object
         */
        <O> void onInvalidated(O owner, BiConsumer<O, ? super Lifecycled<T>> callback);
    }

    /**
     * Get the value or {@link Optional#empty()}.
     *
     * @return the value
     */
    Optional<T> value();

    /**
     * Get the value or throw.
     *
     * @return the value
     * @throws IllegalStateException if there is no value
     */
    default T valueOrThrow() throws IllegalStateException {
        return value().orElseThrow(() -> new IllegalStateException("Currently invalid"));
    }

    /**
     * Check for validity, usually without triggering computation.
     *
     * @return if this lifecycled's {@link #value()} is valid
     */
    default boolean isValid() {
        return value().isPresent();
    }

    /**
     * Get the event manager for this lifecycled object.
     *
     * @return the event manager
     */
    Events<T> events();

    /**
     * Map the value.
     *
     * @param mapper the mapper function
     * @param <U> the new type
     * @return the downstream lifecycled
     */
    default <U> Lifecycled<U> map(Function<T, U> mapper) {
        return new MapLifecycled<>(this, mapper);
    }

    /**
     * Filter the value. In other words, create a new lifecycled object where validity is ANDed
     * with the result of calling the filter function.
     *
     * @param filterer the filter function
     * @return the downstream lifecycled
     */
    default Lifecycled<T> filter(Predicate<T> filterer) {
        SimpleLifecycled<T> downstream = SimpleLifecycled.invalid();
        events().onInvalidated(downstream, (d, lifecycled) -> d.invalidate());
        events().onNewValue(downstream, (d, lifecycled) -> {
            T value = lifecycled.valueOrThrow();
            if (filterer.test(value)) {
                d.newValue(value);
            }
        });
        return downstream;
    }

    default <U> Lifecycled<U> flatMap(Function<T, Lifecycled<U>> mapper) {
        return new FlatMapLifecycled<>(this, mapper);
    }
}
