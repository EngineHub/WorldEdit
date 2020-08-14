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

package com.sk89q.worldedit.util.task;

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.util.task.progress.Progress;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A task that wraps a {@code ListenableFuture}.
 *
 * <p>{@link Task.State#SCHEDULED} is never returned because it is not possible
 * to test whether the future has "started," so {@link Task.State#RUNNING} is
 * returned in its place.</p>
 *
 * <p>Use {@link #create(ListenableFuture, String, Object)} to create a new
 * instance.</p>
 *
 * @param <V> the type returned
 */
public class FutureForwardingTask<V> extends AbstractTask<V> {

    private final ListenableFuture<V> future;

    private FutureForwardingTask(ListenableFuture<V> future, String name, @Nullable Object owner) {
        super(name, owner);
        checkNotNull(future);
        this.future = future;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        future.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public State getState() {
        if (isCancelled()) {
            return State.CANCELLED;
        } else if (isDone()) {
            try {
                get();
                return State.SUCCEEDED;
            } catch (InterruptedException e) {
                return State.CANCELLED;
            } catch (ExecutionException e) {
                return State.FAILED;
            }
        } else {
            return State.RUNNING;
        }
    }

    @Override
    public Progress getProgress() {
        return Progress.indeterminate();
    }

    /**
     * Create a new instance.
     *
     * @param future the future
     * @param name the name of the task
     * @param owner the owner of the task, or {@code null}
     * @param <V> the type returned by the future
     * @return a new instance
     */
    public static <V> FutureForwardingTask<V> create(ListenableFuture<V> future, String name, @Nullable Object owner) {
        return new FutureForwardingTask<>(future, name, owner);
    }

}
