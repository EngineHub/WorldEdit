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

/**
 * A supervisor manages the execution of tasks and controls the number of
 * simultaneously running tasks by deferring the start of submitted tasks if
 * too many tasks are concurrently running.
 *
 * <p>Whenever a task needs to be started, a supervisor will call the task's
 * {@link Task#submit()} method and add the task to the list of
 * running tasks. After that task has completed, the supervisor may
 * start another task up to fill the completed task's place.</p>
 */
public interface Supervisor {

    /**
     * Submit a task for execution at some point in the future.
     *
     * @param task the task
     * @param <V> the type of return value
     * @return a future
     */
    <V> ListenableFuture<V> submit(Task<V> task);

}
