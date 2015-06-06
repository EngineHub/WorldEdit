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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.operation.Operation.Result;
import com.sk89q.worldedit.function.util.AffectedCounter;
import com.sk89q.worldedit.util.task.progress.Progress;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Operation helper methods.
 */
public final class Operations {

    private Operations() {
    }

    /**
     * Complete a given operation synchronously until it completes.
     *
     * @param op operation to execute
     * @throws WorldEditException WorldEdit exception
     */
    public static void complete(Operation op) throws Exception {
        Result result;
        do {
            result = op.resume(ImmediateRunContext.getInstance());
        } while (result == Result.CONTINUE);
    }

    /**
     * Complete a given operation synchronously until it completes. Catch all
     * errors that is not {@link MaxChangedBlocksException} for legacy reasons.
     *
     * @param op operation to execute
     * @throws MaxChangedBlocksException thrown when too many blocks have been changed
     */
    public static void completeLegacy(Operation op) throws MaxChangedBlocksException {
        try {
            complete(op);
        } catch (MaxChangedBlocksException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        try {
            complete(op);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrap the given operation so it cannot be cancelled.
     *
     * <p>This modifies the operation so it always thinks
     * {@link RunContext#isCancelled()} is {@code false}.</p>
     *
     * @param op the operation
     * @return the wrapped operation
     */
    public static Operation ignoreCancellation(final Operation op) {
        return new Operation() {
            @Override
            public Result resume(RunContext run) throws Exception {
                return op.resume(preventCancel(run));
            }

            @Override
            public Progress getProgress() {
                return op.getProgress();
            }
        };
    }

    /**
     * Wrap a {@code RunContext} so that it always returns false for
     * {@link RunContext#isCancelled()}.
     *
     * @param run the run context to wrap
     * @return a new run context
     */
    private static RunContext preventCancel(final RunContext run) {
        return new RunContext() {
            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean shouldContinue() {
                return run.shouldContinue();
            }
        };
    }

    /**
     * Add a message on successful completion of the given future stating
     * a number of blocks changed using the counter provided
     * by {@code counter}.
     *
     * @param future the future
     * @param counter the counter
     * @param actor the actor
     */
    public static void addBlockChangeMessage(ListenableFuture<?> future, final AffectedCounter counter, final Actor actor) {
        checkNotNull(future);
        checkNotNull(counter);
        checkNotNull(actor);

        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(@Nullable Object result) {
                int changed = counter.getAffected();
                if (changed == 1) {
                    actor.print("1 block was changed.");
                } else {
                    actor.print(changed + " blocks were changed.");
                }
            }

            @Override
            public void onFailure(@Nullable Throwable t) {
            }
        });
    }

}
