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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code FutureCallback} that will copy its success and failure states
 * to a {@code SettableFuture}.
 *
 * @param <V> the type of the future
 */
public class SettableFutureCallback<V> implements FutureCallback<V> {

    private final SettableFuture<V> future;

    /**
     * Create a new instance.
     *
     * @param future the future to copy the states to
     */
    public SettableFutureCallback(SettableFuture<V> future) {
        checkNotNull(future);
        this.future = future;
    }

    @Override
    public void onSuccess(@Nullable V result) {
        future.set(result);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onFailure(Throwable t) {
        future.setException(t);
    }

}
