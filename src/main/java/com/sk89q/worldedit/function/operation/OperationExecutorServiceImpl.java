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

package com.sk89q.worldedit.function.operation;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class OperationExecutorServiceImpl extends AbstractExecutorService implements OperationExecutorService {
    private boolean shutdown = false;
    private final Deque<OpTask> operations = new ArrayDeque<OpTask>();
    private final Deque<Runnable> runnables = new ArrayDeque<Runnable>();

    OperationExecutorServiceImpl() {
        // worker setup is done in Operations
    }

    @Override
    public OperationFuture submit(Operation operation, EditSession editSession) {
        if (shutdown) {
            throw new RejectedExecutionException("Service is shut down");
        }
        OpTask task = new OpTask(operation, editSession);
        operations.offer(task);
        return task.future;
    }

    @Override
    public void execute(Runnable runnable) {
        if (shutdown) {
            throw new RejectedExecutionException("Service is shut down");
        }
        runnables.add(runnable);
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        ArrayList<Runnable> ret = new ArrayList<Runnable>();
        for (OpTask task : operations) {
            ret.add(new OpWrapper(task));
        }
        ret.addAll(runnables);
        return ret;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && !operations.isEmpty() && !runnables.isEmpty();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        RunContext run = new TimedRunContext(l, timeUnit);
        while (run.shouldContinue() && !operations.isEmpty()) {
            OpTask task = operations.poll();
            if (!work(task, run)) {
                operations.addFirst(task);
            }
        }
        while (run.shouldContinue() && !runnables.isEmpty()) {
            Runnable runnable = runnables.poll();
            work(runnable);
        }
        return isTerminated();
    }

    // Implementation methods

    void heartbeat(RunContext runContext) {
        while (runContext.shouldContinue() && !operations.isEmpty()) {
            OpTask task = operations.poll();
            if (work(task, runContext)) {
                operations.addFirst(task);
            }
        }
        while (runContext.shouldContinue() && !runnables.isEmpty()) {
            Runnable runnable = runnables.poll();
            work(runnable);
        }
    }

    private void work(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ignored) {
            // dfc
        }
    }

    // returns true if not done
    private boolean work(OpTask work, RunContext run) {
        if (work.future.isCancelled()) {
            return false;
        }
        work.started = true;

        while (run.shouldContinue() && work.current != null) {
            try {
                Operation operation = work.current.resume(run);
                work.replaceOperation(operation);
            } catch (WorldEditException e) {
                work.future.throwing(e);
                break;
            }
        }
        work.session.flushQueue();
        return !work.future.isDone() && !work.future.isCancelled();
    }

    /**
     * Wraps an Operation in a Runnable, providing a run() method that will run to completion.
     */
    private static class OpWrapper implements Runnable {
        final OpTask work;

        public OpWrapper(OpTask work) {
            this.work = work;
        }

        @Override
        public void run() {
            Operation op = work.current;
            while (op != null) {
                try {
                    op = op.resume(Operations.TRUE_RUN_CONTEXT);
                    work.replaceOperation(op);
                } catch (WorldEditException e) {
                    work.future.throwing(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static class OpTask {
        final OpFuture future;
        final EditSession session;
        boolean started = false;
        Operation current;

        public OpTask(Operation original, EditSession session) {
            this.future = new OpFuture(original);
            this.current = original;
            this.session = session;
        }

        // helper method
        void replaceOperation(Operation result) {
            current = result;
            if (result == null) {
                future.finish();
            }
        }

        private class OpFuture extends AbstractFuture<Operation> implements OperationFuture {
            private final Operation original;
            private final long startTime = System.currentTimeMillis();

            private OpFuture(Operation original) {
                this.original = original;
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return !(started && !mayInterruptIfRunning) && super.cancel(mayInterruptIfRunning);

            }

            @Override
            public Operation getOriginalOperation() {
                return original;
            }

            @Override
            public long getStartTime() {
                return startTime;
            }

            private void finish() {
                this.set(original);
            }

            private void throwing(Throwable thrown) {
                this.setException(thrown);
            }
        }
    }

    // ListeningExcecutorService required methods

    @Override
    protected final <T> ListenableFutureTask<T> newTaskFor(Runnable runnable, T value) {
        return ListenableFutureTask.create(runnable, value);
    }

    @Override
    protected final <T> ListenableFutureTask<T> newTaskFor(Callable<T> callable) {
        return ListenableFutureTask.create(callable);
    }

    @Override public ListenableFuture<?> submit(Runnable task) {
        return (ListenableFuture<?>) super.submit(task);
    }

    @Override public <T> ListenableFuture<T> submit(Runnable task, @Nullable T result) {
        return (ListenableFuture<T>) super.submit(task, result);
    }

    @Override public <T> ListenableFuture<T> submit(Callable<T> task) {
        return (ListenableFuture<T>) super.submit(task);
    }
}
