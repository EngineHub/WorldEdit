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

package com.sk89q.worldedit.util.scheduler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract implementation that handles the creation of the future
 * and its cancellation. Implementations only need to handle the actual
 * scheduling of a runnable.
 */
public abstract class AbstractTickScheduler implements TickScheduler {

    /**
     * Submit the given task for execution.
     *
     * @param runnable the task
     * @param delay the delay in ticks
     */
    protected abstract void submit(Runnable runnable, long delay);

    @Override
    public final ListenableFuture<?> schedule(Runnable runnable, long delay) {
        checkNotNull(runnable);
        checkArgument(delay >= 0, "delay must be >= 0");
        SettableFuture<Runnable> future = SettableFuture.create();
        submit(new WrappedRunnable(runnable, future), delay);
        return future;
    }

    @Override
    public final <V> ListenableFuture<V> schedule(Callable<V> task, long delay) {
        checkNotNull(task);
        checkArgument(delay >= 0, "delay must be >= 0");
        SettableFuture<V> future = SettableFuture.create();
        submit(new WrappedCallable<V>(task, future), delay);
        return future;
    }

    /**
     * Wraps a runnable to handle the future.
     */
    private static class WrappedRunnable implements Runnable {
        private final Runnable runnable;
        private final SettableFuture<Runnable> future;

        private WrappedRunnable(Runnable runnable, SettableFuture<Runnable> future) {
            this.runnable = runnable;
            this.future = future;
        }

        @Override
        public void run() {
            if (!future.isCancelled()) {
                try {
                    runnable.run();
                    future.set(runnable);
                } catch (Throwable throwable) {
                    future.setException(throwable);
                }
            }
        }
    }

    /**
     * Wraps a callable to handle the future.
     */
    private static class WrappedCallable<V> implements Runnable {
        private final Callable<V> callable;
        private final SettableFuture<V> future;

        private WrappedCallable(Callable<V> callable, SettableFuture<V> future) {
            this.callable = callable;
            this.future = future;
        }

        @Override
        public void run() {
            if (!future.isCancelled()) {
                try {
                    future.set(callable.call());
                } catch (Throwable throwable) {
                    future.setException(throwable);
                }
            }
        }
    }

}
