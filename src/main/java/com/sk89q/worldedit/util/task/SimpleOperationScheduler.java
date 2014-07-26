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

package com.sk89q.worldedit.util.task;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.util.scheduler.TickScheduler;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An operation scheduler that can inform owners that are {@link Actor}s
 * on the status of their operations.
 *
 * <p>This operation will handle errors that occur on any operation.</p>
 */
public class SimpleOperationScheduler extends AbstractOperationScheduler {

    private final SimpleSupervisor supervisor;
    private final Queue<OperationTask> scheduled = new ArrayDeque<OperationTask>();
    private final List<OperationTask> running = new ArrayList<OperationTask>();
    private final Object lock = new Object();
    private int poolSize = 2;

    /**
     * Create a new operation scheduler.
     *
     * @param supervisor the supervisor to submit tasks to
     */
    public SimpleOperationScheduler(SimpleSupervisor supervisor) {
        checkNotNull(supervisor);
        this.supervisor = supervisor;
    }

    /**
     * Get the number of operations to run at a time.
     *
     * @return a number of operations
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Set the number of operations to run at a time.
     *
     * <p>If the pool size is decreased, currently running tasks will continue
     * to be run. If the pool size is increased, then more tasks will be
     * promoted to running.</p>
     *
     * @param poolSize a number of operations
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        promoteScheduled();
    }

    @Override
    public ListenableFuture<Operation> submit(Operation operation, World world, @Nullable String name, @Nullable Object owner) {
        checkNotNull(operation);
        checkNotNull(world);
        TickScheduler scheduler = checkNotNull(world.getScheduler());

        OperationTask task = new OperationTask(operation, scheduler, name, owner);
        synchronized (lock) {
            scheduled.offer(task);
            supervisor.monitor(task);
        }
        promoteScheduled();

        // Catch errors
        Futures.addCallback(task, new ErrorHandlerCallback(owner instanceof Actor ? (Actor) owner : null));

        return task;
    }

    private void promoteScheduled() {
        boolean mayNeedPromotion = true;

        while (mayNeedPromotion) {
            final OperationTask entry;

            synchronized (lock) {
                int runningCount = running.size();

                if (runningCount >= poolSize) {
                    return; // No room left -- abort
                }

                entry = scheduled.poll();

                if (entry == null) {
                    return; // Nothing in the queue -- abort
                }

                mayNeedPromotion = !scheduled.isEmpty();
            }

            entry.submitToScheduler();
        }
    }

}
