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

import javax.annotation.Nullable;

/**
 * A {@code DelegateOperation} requests from the subclass an operation
 * to execute; if an operation is returned, it will be completed. At which
 * point, another operation will be requested from the subclass and
 * the process will continue until the subclass does not return a new
 * operation to execute.
 *
 * <p>Cancellation has no effect. Implementations should check whether
 * the operation has been cancelled in {@link #getNextOperation(RunContext)}
 * and return {@code null} as necessary.</p>
 */
public abstract class DelegateOperation extends AbstractOperation {

    private Operation delegate;

    /**
     * Get the current delegated operation.
     *
     * @return the current delegated operation, which may be null
     */
    @Nullable
    protected Operation getCurrentOperation() {
        return delegate;
    }

    @Override
    public Result resume(RunContext run) throws Exception {
        Operation delegate = this.delegate;

        if (delegate != null) {
            if (delegate.resume(run) == Result.STOP) {
                delegate = null;
            }
        }

        if (delegate == null) {
            delegate = getNextOperation(run);
        }

        this.delegate = delegate;

        return delegate != null ? Result.CONTINUE : Result.STOP;
    }

    /**
     * Get the next operation to run.
     *
     * @param run the run context
     * @return an operation to execute or {@code null} to abort
     * @throws Exception thrown on any error
     */
    @Nullable
    protected abstract Operation getNextOperation(RunContext run) throws Exception;

    /**
     * Return whether the operation should continue.
     *
     * @param run the run context
     * @return true to continue execution
     */
    protected boolean shouldResume(RunContext run) {
        return !run.isCancelled();
    }

}
