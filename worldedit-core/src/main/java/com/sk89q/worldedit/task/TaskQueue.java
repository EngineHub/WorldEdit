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

package com.sk89q.worldedit.task;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.operation.Operation;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

public class TaskQueue {

    private final Queue<Callable<Void>> taskQueue = new LinkedList<>();

    public void tick() {
        Callable<Void> task = taskQueue.poll();
        if (task != null) {
            try {
                task.call();
            } catch (Exception e) {
                WorldEdit.logger.error("Oh no", e);
            }
        }
    }

    public void addTask(Callable<Void> task) {
        this.taskQueue.add(task);
    }
}
