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

package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.task.Task;
import com.sk89q.worldedit.util.task.progress.Progress;

/**
 * Displays a list of tasks.
 *
 * @see Task the type of task
 */
public class TaskListBox extends MessageBox {

    private boolean first = true;

    /**
     * Create a new box.
     *
     * @param title the title
     */
    public TaskListBox(String title) {
        super(title);
    }

    public TaskListBox appendTask(Task<?> task) {
        if (!first) {
            getContents().newLine();
        }

        Object owner = task.getOwner();
        if (owner instanceof Actor) {
            Actor actor = (Actor) owner;
            getContents().createFragment(Style.YELLOW).append("[").append(actor.getName()).append("] ");
        } else {
            getContents().createFragment(Style.GRAY).append("[unowned] ");
        }

        getContents().createFragment(getStateColor(task.getState())).append(task.getState().name());
        getContents().append(": ");
        getContents().createFragment(Style.WHITE).append(task.getName());

        Progress progress = task.getProgress();
        if (!progress.isIndeterminate()) {
            getContents().createFragment(Style.GRAY).append(" (").append(Math.round(progress.getProgress() * 100)).append("%)");
        }

        first = false;
        return this;
    }

    private static Style getStateColor(Task.State state) {
        switch (state) {
            case CANCELLED:
                return Style.CYAN;
            case FAILED:
                return Style.RED;
            case RUNNING:
                return Style.GREEN;
            case SCHEDULED:
                return Style.GRAY;
            case SUCCEEDED:
                return Style.GREEN_DARK;
            default:
                return Style.WHITE;
        }
    }

}
