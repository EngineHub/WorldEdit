package com.sk89q.worldedit.forge;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * A small task scheduler implementation providing only synchronous tasks.
 * Loosely based on the Bukkit CraftScheduler.
 */
public class ForgeMiniScheduler implements ITickHandler {
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
