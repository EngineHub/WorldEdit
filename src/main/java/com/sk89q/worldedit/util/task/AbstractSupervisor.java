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

package com.sk89q.worldedit.util.task;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.sk89q.worldedit.util.concurrency.SettableFutureCallback;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstraction implementation of a {@code Supervisor} that implements
 * a fixed quota of tasks.
 */
public abstract class AbstractSupervisor implements Supervisor {

    private static final int DEFAULT_CONCURRENT_QUOTA = 5;
    private static final Logger log = Logger.getLogger(FriendlySupervisor.class.getCanonicalName());

    private final Deque<TaskEntry<?>> queue = new ArrayDeque<TaskEntry<?>>();
    private final List<TaskEntry<?>> running = new ArrayList<TaskEntry<?>>();
    private final Object lock = new Object();
    private int concurrentQuota;

    /**
     * Create a new supervisor with a limit of 5 concurrent tasks.
     */
    public AbstractSupervisor() {
        this(DEFAULT_CONCURRENT_QUOTA);
    }

    /**
     * Create a new supervisor with a given limit of concurrent tasks.
     *
     * @param concurrentQuota the maximum number of concurrent tasks
     */
    public AbstractSupervisor(int concurrentQuota) {
        setConcurrentQuota(concurrentQuota);
    }

    /**
     * Get the limit of concurrent tasks.
     *
     * @return the limit
     */
    public int getConcurrentQuota() {
        return concurrentQuota;
    }

    /**
     * Set the limit of concurrent tasks.
     *
     * @param concurrentQuota the limit
     * @throws IllegalArgumentException if {@code concurrentQuota} is less than 0
     */
    public void setConcurrentQuota(int concurrentQuota) {
        checkArgument(concurrentQuota > 0, "concurrentQuota must be >= 0");
        this.concurrentQuota = concurrentQuota;
        advanceQueue();
    }

    /**
     * Submit the given task entry to the queue.
     *
     * @param taskEntry the task entry
     * @param <V> the type returned
     * @return a future
     */
    protected <V> ListenableFuture<V> submit(TaskEntry<V> taskEntry) {
        checkNotNull(taskEntry);
        synchronized (lock) {
            queue.push(taskEntry);
            advanceQueue();
            return taskEntry.getFuture();
        }
    }

    /**
     * Start up another task if needed to fill the concurrent task quota.
     */
    private void advanceQueue() {
        final TaskEntry<?> taskEntry;

        synchronized (lock) {
            int size = running.size();

            if (size >= concurrentQuota) {
                return; // No room left -- abort
            }

            taskEntry = queue.pollLast();

            if (taskEntry == null) {
                return; // Nothing in the queue -- abort
            }

            running.add(taskEntry);
        }

        ListenableFuture<?> future = taskEntry.startTask();

        // Add a listener so we can advance the queue after this
        // task completes
        future.addListener(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    running.remove(taskEntry);
                }

                advanceQueue();
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    /**
     * Contains the task and future.
     *
     * @param <V> the type returned
     */
    protected class TaskEntry<V> {
        private final SettableFuture<V> future = SettableFuture.create();
        private final Task<V> task;

        /**
         * Create a new entry.
         *
         * @param task the task
         */
        protected TaskEntry(Task<V> task) {
            checkNotNull(task);
            this.task = task;
        }

        /**
         * Get the future.
         *
         * @return the future
         */
        private ListenableFuture<V> getFuture() {
            return future;
        }

        /**
         * Start the task and return a future.
         *
         * @return a future
         */
        protected ListenableFuture<V> startTask() {
            if (!future.isCancelled()) {
                try {
                    // Submit the task and get a future that we will redirect
                    // to our own future
                    Futures.addCallback(checkNotNull(task.submit()), new SettableFutureCallback<V>(future));
                } catch (Throwable t) {
                    log.log(Level.WARNING, "Failed to execute the next task", t);
                    future.setException(new InvocationTargetException(t, "Failed to execute the next task"));
                }
            }

            return future;
        }
    }

}
