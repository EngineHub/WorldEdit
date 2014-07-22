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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.extension.platform.Actor;

import javax.annotation.Nullable;
import java.util.Timer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A supervisor that keeps track of a task's owner and informs him or her
 * on the status of his or her task.
 */
public class FriendlySupervisor extends AbstractSupervisor {

    private static final Timer timer = new Timer();
    private int queueInformDelay = 1000;

    /**
     * Create a new supervisor with a limit of 5 concurrent tasks.
     */
    public FriendlySupervisor() {
    }

    /**
     * Create a new supervisor with a given limit of concurrent tasks.
     *
     * @param concurrentQuota the maximum number of concurrent tasks
     */
    public FriendlySupervisor(int concurrentQuota) {
        super(concurrentQuota);
    }

    /**
     * Get the delay before the owner of a task is informed that the task
     * has started.
     *
     * <p>If the task finishes before the delay is reached, then no message
     * is printed.</p>
     *
     * @return the delay in milliseconds
     */
    public int getQueueInformDelay() {
        return queueInformDelay;
    }

    /**
     * Set the delay before the owner of a task is informed that the task
     * has started.
     *
     * <p>If the task finishes before the delay is reached, then no message
     * is printed.</p>
     *
     * @param queueInformDelay the delay in milliseconds
     * @throws IllegalArgumentException if {@code queueInformDelay} is less than 0
     */
    public void setQueueInformDelay(int queueInformDelay) {
        checkArgument(queueInformDelay >= 0, "queueInformDelay must be >= 0");
        this.queueInformDelay = queueInformDelay;
    }

    @Override
    public <V> ListenableFuture<V> submit(Task<V> task) {
        return submit(new FriendlyTaskEntry<V>(task, null));
    }

    /**
     * Submit a task with the given actor as its owner.
     *
     * <p>The owner may receive updates on the status of the submitted task.</p>
     *
     * @param task the task
     * @param owner the owner, which may be null if there is no owner
     * @param <V> the type returned
     * @return a future
     */
    public <V> ListenableFuture<V> submit(Task<V> task, @Nullable Actor owner) {
        return submit(new FriendlyTaskEntry<V>(task, owner));
    }

    private class FriendlyTaskEntry<V> extends TaskEntry<V> {
        @Nullable
        private final Actor owner;
        private final ActorMessageTimerTask informer;

        private FriendlyTaskEntry(Task<V> task, @Nullable Actor owner) {
            super(task);
            this.owner = owner;
            informer = new ActorMessageTimerTask(owner, "Your task has been queued...");
            timer.schedule(informer, queueInformDelay);
        }

        @Override
        protected ListenableFuture<V> startTask() {
            ListenableFuture<V> future = super.startTask();
            future.addListener(new Runnable() {
                @Override
                public void run() {
                    informer.cancel();
                }
            }, MoreExecutors.sameThreadExecutor());
            return future;
        }
    }

}