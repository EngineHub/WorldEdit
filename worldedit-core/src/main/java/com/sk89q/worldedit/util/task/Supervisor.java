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

import java.util.List;

/**
 * Manages running tasks and informs users of their progress, but does not
 * execute the task.
 */
public interface Supervisor {

    /**
     * Get a list of running or queued tasks.
     *
     * @return a list of tasks
     */
    List<Task<?>> getTasks();

    /**
     * Monitor the given task.
     *
     * @param task the task
     */
    void monitor(Task<?> task);

}
