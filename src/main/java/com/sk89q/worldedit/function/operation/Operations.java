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

    private static int taskId = -1;
    private static OperationExecutorServiceImpl executorService;

    public static OperationExecutorService getExecutor() {
        if (executorService == null) {
            executorService = new OperationExecutorServiceImpl();
            Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.SCHEDULING);
            taskId = platform.schedule(0, WorldEdit.getInstance().getConfiguration().slowExecutorPeriod,
                    new SlowCompletionWorker());
        }
        return executorService;
    }

    /**
     * Completes the given Operation slowly, with a best-effort attempt made
     * to avoid lagging the server.
     *
     * @param session The EditSession that the Operation is changing
     * @param op an Operation
     * @return a Future that will complete with the final Operation that was executed
     */
    /*
    Note that the parameter 'op' is a CountingOperation instead of an Operation.
    This is because the Operation randomly gets cast to a CountingOperation in
    the result listeners. If you have a non-Counting operation that needs to be
    done over multiple ticks, go ahead and change the parameter.

    If nobody comes up with a non-Counting Operation that needs to be done
    slowly, then change the return type of OperationFuture to CountingOperation.
     */
    public static OperationFuture completeSlowly(EditSession session, CountingOperation op) throws WorldEditException {
        OperationExecutorService service = getExecutor();

        OperationFuture future = service.submit(op, session);

        if (taskId == -1) {
            // Platform does not support scheduling - run the task now
            executorService.heartbeat(TRUE_RUN_CONTEXT);
        } else {
            session.setInLongOperation(true);
        }

        return future;
    }

    private static class SlowCompletionWorker implements Runnable {
        @Override
        public void run() {
            RunContext run = new TimedRunContext(WorldEdit.getInstance().getConfiguration().slowExecutorMillisPer, TimeUnit.MILLISECONDS);
            executorService.heartbeat(run);
        }
    }
}
