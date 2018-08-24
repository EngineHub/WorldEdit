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

package com.sk89q.worldedit.function.task;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.function.operation.RunContext;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class TaskQueue {
    private final Deque<Task> queue = new ArrayDeque<>();
    private Task current;
    private boolean paused;

    /**
     * Create a new queue containing no operations.
     */
    public TaskQueue() {
    }

    /**
     * Create a new queue with tasks from the given collection.
     *
     * @param tasks a collection of tasks
     */
    public TaskQueue(Collection<Task> tasks) {
        checkNotNull(tasks);
        for (Task task : tasks) {
            offer(task);
        }
    }

    /**
     * Create a new queue with tasks from the given array.
     *
     * @param tasks an array of tasks
     */
    public TaskQueue(Task... tasks) {
        checkNotNull(tasks);
        for (Task task : tasks) {
            offer(task);
        }
    }

    /**
     * Add a new task to the queue.
     *
     * @param task the task
     */
    public void offer(Task task) {
        checkNotNull(task);
        queue.offer(task);
    }

    public Task resume(RunContext run) {
        if (paused) {
            return current;
        }
        if (current == null && !queue.isEmpty()) {
            current = queue.poll();
        }

        if (current != null) {
            if (current.resumeTask(run)) {
                current = queue.poll();
            }
        }

        return current;
    }

    /**
     * Set the paused state of this task queue.
     *
     * @param paused If it should pause
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Gets whether this task queue is paused.
     *
     * @return If it's paused
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Cancel the current task in the queue.
     */
    public void cancel() {
        if (current != null) {
            current.cancel();
            current = null;
        }
    }

    /**
     * Cancel all tasks in the queue.
     */
    public void cancelAll() {
        for (Task task : queue) {
            task.cancel();
        }
        queue.clear();
    }
}
