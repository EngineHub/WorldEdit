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

import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of a {@code Supervisor}.
 */
public class SimpleSupervisor implements Supervisor {

    private final List<Task<?>> monitored = new ArrayList<>();
    private final Object lock = new Object();

    @Override
    public List<Task<?>> getTasks() {
        synchronized (lock) {
            return new ArrayList<>(monitored);
        }
    }

    @Override
    public void monitor(final Task<?> task) {
        checkNotNull(task);

        synchronized (lock) {
            monitored.add(task);
        }

        task.addListener(() -> {
            synchronized (lock) {
                monitored.remove(task);
            }
        }, MoreExecutors.directExecutor());
    }

}
