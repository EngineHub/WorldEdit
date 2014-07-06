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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Operation helper methods.
 */
public final class Operations {

    public static final TrueRunContext TRUE_RUN_CONTEXT = new TrueRunContext();

    private Operations() {
    }

    /**
     * Complete a given operation synchronously until it completes.
     *
     * @param op operation to execute
     * @throws WorldEditException WorldEdit exception
     * @deprecated No longer used in WorldEdit
     */
    public static void complete(Operation op) throws WorldEditException {
        while (op != null) {
            op = op.resume(TRUE_RUN_CONTEXT);
        }
    }

    /**
     * Complete a given operation synchronously until it completes. Catch all
     * errors that is not {@link MaxChangedBlocksException} for legacy reasons.
     *
     * @param op operation to execute
     * @throws MaxChangedBlocksException thrown when too many blocks have been changed
     * @deprecated No longer used in WorldEdit
     */
    @Deprecated
    public static void completeLegacy(Operation op) throws MaxChangedBlocksException {
        while (op != null) {
            try {
                op = op.resume(TRUE_RUN_CONTEXT);
            } catch (MaxChangedBlocksException e) {
                throw e;
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Complete a given operation synchronously until it completes. Re-throw all
     * {@link com.sk89q.worldedit.WorldEditException} exceptions as
     * {@link java.lang.RuntimeException}s.
     *
     * @param op operation to execute
     */
    public static void completeBlindly(Operation op) {
        while (op != null) {
            try {
                op = op.resume(TRUE_RUN_CONTEXT);
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static Platform getSchedulingPlatform() {
        return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.SCHEDULING);
    }

    private static int taskId = -1;

    /**
     * Completes the given Operation slowly, with a best-effort attempt made
     * to avoid lagging the server.
     *
     * @param op an Operation
     * @return a Future that will complete with the final Operation that was executed
     */
    public static OperationFuture completeSlowly(EditSession session, Operation op) {
        if (taskId == -1) {
            // Start the worker task
            taskId = getSchedulingPlatform().schedule(0, 1, new SlowCompletionWorker());

            if (taskId == -1) {
                // Platform does not support scheduling
                // Just do the operation now and return a completed Future
                OperationFuture future = new OperationFuture(session, op);
                try {
                    Operation innerOp = op;
                    while (innerOp != null) {
                        innerOp = innerOp.resume(new TrueRunContext());
                        future.replaceOperation(innerOp);
                    }
                    future.complete();
                } catch (WorldEditException ex) {
                    future.throwing(ex);
                }
                // session.flushQueue();
                return future;
            }
        }

        session.setInLongOperation(true);
        // TODO try running for ~5ms here
        return SlowCompletionWorker.queueOperation(session, op);
    }

    public static void completeFutureNow(OperationFuture future) throws WorldEditException {
        SlowCompletionWorker.cancel(future);
        Operation op = future.getOperation();
        future.setStarted();
        while (op != null) {
            try {
                op = op.resume(TRUE_RUN_CONTEXT);
                future.replaceOperation(op);
            } catch (WorldEditException e) {
                future.throwing(e);
                throw e;
            }
        }
        future.complete();
    }

    public static List<OperationFuture> getQueueSnapshot() {
        return new ArrayList<OperationFuture>(SlowCompletionWorker.queue);
    }

    protected static class SlowCompletionWorker implements Runnable {
        /**
         * A deck of the OperationFutures waiting to be processed.
         *
         * New Operations are added at the bottom ("last") of the deck, and
         * operations are processed from the top. However, when an Operation
         * doesn't fully complete within the allotted 30 milliseconds, it is
         * placed back on the top of the deck to be processed again next tick.
         */
        private static final Deque<OperationFuture> queue = new ArrayDeque<OperationFuture>();
        private int cancelCounter = 0;

        protected static OperationFuture queueOperation(EditSession session, Operation op) {
            OperationFuture future = new OperationFuture(session, op);
            queue.addLast(future);
            return future;
        }

        protected static boolean cancel(OperationFuture future) {
            future.getOriginalOperation().cancel();
            return queue.remove(future);
        }

        @Override
        public void run() {
            RunContext run = new TimedRunContext(30, TimeUnit.MILLISECONDS);

            // If no operations are active for 5 seconds, cancel the job
            if (queue.size() == 0) {
                cancelCounter++;
                if (cancelCounter >= 100) {
                    if (getSchedulingPlatform().cancelScheduled(taskId)) {
                        taskId = -1;
                    }
                }
                return;
            }

            OperationFuture future;
            while (run.shouldContinue() && (future = queue.pollFirst()) != null) {
                if (future.isDone()) {
                    // cancelled
                    continue;
                }

                future.setStarted();

                // Process future
                Operation operation = future.getOperation();
                while (run.shouldContinue() && operation != null) {
                    try {
                        operation = operation.resume(run);
                        future.replaceOperation(operation);
                    } catch (WorldEditException e) {
                        future.throwing(e);
                        break;
                    }
                }

                future.getEditSession().flushQueue();

                if (future.isDone()) {
                    // Operation threw an exception
                    continue;
                } else if (operation == null) {
                    // Operation completed
                    future.complete();
                    future.getEditSession().setInLongOperation(false);
                    continue;
                } else {
                    // Run timed out - NOT done!
                    // Put it back in the queue.
                    queue.addFirst(future);
                    future.delayed();
                    WorldEdit.logger.info("Performing long operation...");
                    return;
                }
            }
        }
    }
}
