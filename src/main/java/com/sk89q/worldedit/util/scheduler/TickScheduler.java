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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Schedules tasks to run later in a given number of ticks.
 */
public interface TickScheduler {

    /**
     * Schedule a runnable to be executed in the given number of ticks.
     *
     * @param runnable the runnable to execute
     * @param delay the delay in ticks
     * @return a future representing the scheduled task
     */
    Future<?> schedule(Runnable runnable, long delay);

    /**
     * Schedule a task to be executed in the given number of ticks.
     *
     * @param task the task to execute
     * @param delay the delay in ticks
     * @return a future representing the scheduled task
     */
    <V> Future<V> schedule(Callable<V> task, long delay);

}
