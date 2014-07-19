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

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A small task scheduler implementation providing only synchronous tasks.
 * Loosely based on the Bukkit CraftScheduler.
 */
class ForgeMiniScheduler implements ITickHandler {

    private final AtomicInteger idAssigner = new AtomicInteger(30);
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
    private final List<Task> temp = new ArrayList<Task>();
    private Task currentlyRunning;
    private int currentTick = 0;

    private static final int PERIOD_RUNONCE = -1;
    private static final int PERIOD_CANCEL = -2;

    public int schedule(long delay, long period, Runnable run) {
        if (delay < 0) {
            delay = 0;
        }
        if (period <= 0) {
            period = PERIOD_RUNONCE;
        }

        Task task = new Task(run, nextId(), period);

        // Calling schedule from a task
        return addToTemp(delay, task).getTaskId();
    }

    public boolean cancel(int taskId) {
        // Calling cancel from the running task
        if (currentlyRunning.getTaskId() == taskId) {
            currentlyRunning.setPeriod(PERIOD_CANCEL);
            return true;
        } else {
            Iterator<Task> iter = pending.iterator();
            Task temp;
            while (iter.hasNext()) {
                temp = iter.next();
                if (temp.getTaskId() == taskId) {
                    iter.remove();
                    temp.cancel0();
                    return true;
                }
            }
            return false;
        }
    }

    public void heartbeat() {
        currentTick++;
        moveToPending();
        while (isReady()) {
            Task task = pending.poll();
            if (task.getPeriod() == PERIOD_CANCEL) {
                continue;
            }

            currentlyRunning = task;
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("WorldEdit scheduled task generated an exception");
                t.printStackTrace();
            }

            if (task.getPeriod() > 0) {
                addToTemp(task.getPeriod(), task);
            }
        }
    }

    private boolean isReady() {
        return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
    }

    private int nextId() {
        return idAssigner.getAndIncrement();
    }

    private Task addToTemp(long delay, Task task) {
        task.setNextRun(currentTick + delay);
        temp.add(task);
        return task;
    }

    private void moveToPending() {
        pending.addAll(temp);
        temp.clear();
    }

    // ITickHandler methods

    @Override
    public void tickStart(EnumSet type, Object... tickData) {
        heartbeat();
    }

    @Override
    public void tickEnd(EnumSet type, Object... tickData) {
    }

    @Override
    public EnumSet ticks() {
        return EnumSet.of(TickType.CLIENT, TickType.SERVER);
    }

    @Override
    public String getLabel() {
        return "WorldEdit Scheduler";
    }

    private class Task implements Runnable {
        private final Runnable task;
        private final int id;
        private long period;
        private long nextRun;
        private Task next;

        Task(Runnable runnable, int id, long period) {
            this.task = runnable;
            this.id = id;
            this.period = period;
        }

        public int getTaskId() {
            return id;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        public long getNextRun() {
            return nextRun;
        }

        public void setNextRun(long nextRun) {
            this.nextRun = nextRun;
        }

        @Override
        public void run() {
            task.run();
        }

        public Task getNext() {
            return next;
        }

        public void setNext(Task task) {
            this.next = task;
        }

        public void cancel0() {
            setPeriod(PERIOD_CANCEL);
        }
    }

}
