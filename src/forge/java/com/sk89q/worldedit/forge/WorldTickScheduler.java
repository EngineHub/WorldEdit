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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.util.scheduler.AbstractTickScheduler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A small task scheduler implementation providing only synchronous tasks.
 */
class WorldTickScheduler extends AbstractTickScheduler implements ITickHandler {

    private static final Logger log = Logger.getLogger(WorldTickScheduler.class.getCanonicalName());
    private final PriorityQueue<Task> pending = new PriorityQueue<Task>(10,
            new Comparator<Task>() {
                @Override
                public int compare(Task task1, Task task2) {
                    long diff = task1.getNextRun() - task2.getNextRun();
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
    @GuardedBy("buffer")
    private final List<Task> buffer = new ArrayList<Task>();
    private int currentTick = 0;

    @Override
    protected void submit(Runnable runnable, long delay) {
        synchronized (buffer) {
            buffer.add(new Task(runnable, delay));
        }
    }

    @Override
    public void tickStart(EnumSet type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet type, Object... tickData) {
        currentTick++;
        synchronized (buffer) {
            pending.addAll(buffer);
            buffer.clear();
        }

        while (!pending.isEmpty() && pending.peek().getNextRun() <= currentTick) {
            Task task = pending.poll();
            try {
                task.run();
            } catch (Throwable t) {
                log.log(Level.WARNING, "An exception was thrown while running a scheduled tick task", t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnumSet ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "WorldEdit Scheduler";
    }

    /**
     * Wraps a task to be run.
     */
    protected class Task implements Runnable {
        private final Runnable task;
        private long nextRun;

        protected Task(Runnable runnable, long delay) {
            this.task = runnable;
            this.nextRun = currentTick + delay;
        }

        public long getNextRun() {
            return nextRun;
        }

        @Override
        public void run() {
            task.run();
        }
    }

}
