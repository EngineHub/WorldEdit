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
import com.sk89q.worldedit.util.task.progress.ProgressObservable;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

/**
 * A task is a job that can be scheduled, run, or cancelled. Tasks can report
 * on their own status. Tasks have owners.
 */
public interface Task<V> extends ListenableFuture<V>, ProgressObservable {

    /**
     * Get the unique ID of this task.
     *
     * @return this task's unique ID
     */
    UUID getUniqueId();

    /**
     * Get the name of the task so it can be printed to the user.
     *
     * @return the name of the task
     */
    String getName();

    /**
     * Get the owner of the task.
     *
     * @return an owner object, if one is known or valid, otherwise {@code null}
     */
    @Nullable
    Object getOwner();

    /**
     * Get the state of the task.
     *
     * @return the state of the task
     */
    State getState();

    /**
     * Get the time at which the task was created.
     *
     * @return a date
     */
    Date getCreationDate();

    /**
     * Represents the state of a task.
     */
    enum State {
        /**
         * The task has been scheduled to run but is not running yet.
         */
        SCHEDULED,
        /**
         * The task has been cancelled and may be stopped or will stop.
         */
        CANCELLED,
        /**
         * The task is currently running.
         */
        RUNNING,
        /**
         * The task has failed.
         */
        FAILED,
        /**
         * The task has succeeded.
         */
        SUCCEEDED
    }

}
