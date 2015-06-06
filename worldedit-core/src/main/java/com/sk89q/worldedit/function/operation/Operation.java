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

import com.sk89q.worldedit.util.task.OperationScheduler;
import com.sk89q.worldedit.util.task.progress.ProgressObservable;

/**
 * The {@code Operation} interface is to be implemented by classes whose
 * instances are to be executed repeatedly in one or more threads sequentially.
 *
 * <p>The purpose of operations is to allow the thread-safe execution of tasks
 * in thread-unsafe environments where locks cannot be employed, possibly due
 * to legacy limitations.</p>
 *
 * <p>Scheduling an operation for execution is made possible by the
 * {@link OperationScheduler} class. In addition, the utility
 * {@link Operations} class provides related methods for working with
 * operations, including a function to repeatedly execute an operation
 * until completion (without returning control until completion).</p>
 */
public interface Operation extends ProgressObservable {

    /**
     * Returns whether this operation is opportunistic.
     *
     * <p>An opportunistic operation may have tasks to perform frequently
     * through the lifetime of another operation, but can be abandoned
     * at any time without causing problems. Opportunistic tasks should
     * always return {@code false} for {@link #resume(RunContext)} so
     * that unaware implementations do not inevitably execute an opportunistic
     * task for an infinite duration.</p>
     *
     * @return true if opportunistic
     */
    boolean isOpportunistic();

    /**
     * Execute the next pass of the operation.
     *
     * <p>Implementations should inspect the provided {@link RunContext}
     * to determine when it should return control to the calling code as well
     * as whether it should abort execution completely.</p>
     *
     * <p>This method should never be called from different threads
     * simultaneously.</p>
     *
     * @param run describes information about the current run
     * @return whether the operation should be resumed
     * @throws Exception any exception may be thrown
     */
    Result resume(RunContext run) throws Exception;

    /**
     * The return value of the {@link #resume(RunContext)} method to determine
     * whether execution should continue.
     */
    public static enum Result {

        /**
         * Resume the operation again. There is no actual gaurantee that
         * the scheduler will resume the operation.
         */
        CONTINUE,

        /**
         * Don't resume the operation again.
         */
        STOP
    }

}
