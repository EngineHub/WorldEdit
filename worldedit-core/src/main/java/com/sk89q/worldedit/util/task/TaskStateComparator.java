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

import java.util.Comparator;

/**
 * Compares task states according to the order of the {@link Task.State}
 * enumeration.
 */
public class TaskStateComparator implements Comparator<Task<?>> {

    @Override
    public int compare(com.sk89q.worldedit.util.task.Task<?> o1, Task<?> o2) {
        int ordinal1 = o1.getState().ordinal();
        int ordinal2 = o2.getState().ordinal();
        if (ordinal1 < ordinal2) {
            return -1;
        } else if (ordinal1 > ordinal2) {
            return 1;
        } else {
            return 0;
        }
    }

}
