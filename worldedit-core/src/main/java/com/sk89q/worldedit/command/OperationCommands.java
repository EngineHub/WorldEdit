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

package com.sk89q.worldedit.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.component.TaskListBox;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.task.Task;
import com.sk89q.worldedit.util.task.TaskStateComparator;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands for view operations.
 */
public class OperationCommands {

    private final WorldEdit worldEdit;

    public OperationCommands(WorldEdit worldEdit) {
        this.worldEdit = checkNotNull(worldEdit);
    }

    @Command(aliases = {"/running", "/queue"}, desc = "List running tasks")
    public void listRunning(Actor actor) {
        Supervisor supervisor = worldEdit.getSupervisor();
        List<Task<?>> tasks = supervisor.getTasks();
        Collections.sort(tasks, new TaskStateComparator());

        TaskListBox box = new TaskListBox(tasks.size() == 1 ? "1 task" : tasks.size() + " tasks");
        if (!tasks.isEmpty()) {
            for (Task<?> task : tasks) {
                box.appendTask(task);
            }
        } else {
            box.getContents().append("There are currently no active tasks.");
        }
        actor.printRaw(ColorCodeBuilder.asColorCodes(box));
    }

    @Command(aliases = {"/cancelall"}, desc = "Cancel a task")
    @CommandPermissions("worldedit.operation.cancelall")
    public void cancelAllTasks(Actor actor) {
        Supervisor supervisor = worldEdit.getSupervisor();
        List<Task<?>> tasks = supervisor.getTasks();
        if (!tasks.isEmpty()) {
            for (Task<?> task : tasks) {
                task.cancel(false);
            }
            actor.print("All running tasks were cancelled.");
        } else {
            actor.printError("There are no tasks to cancel.");
        }
    }

}
