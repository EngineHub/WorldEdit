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

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.concurrent.RejectedExecutionException;

/**
 * An operation scheduler accepts operations and schedules them to be executed
 * in either one go or spread over several ticks. Some schedulers
 * may choose to notify the owners of operations on the status of their tasks.
 */
public interface OperationScheduler {

    /**
     * Submit an operation for execution with no given name and no given owner.
     *
     * @param operation the operation
     * @param world the world for which the operation should be scheduled under
     * @return a future that will complete on total completion of the operation
     * @throws RejectedExecutionException thrown if the operation is rejected
     */
    public ListenableFuture<Operation> submit(Operation operation, World world);

    /**
     * Submit an operation for execution.
     *
     * @param operation the operation
     * @param world the world for which the operation should be scheduled under
     * @param name the name of the task (to be printed to users)
     * @param owner the optionally specified owner of the operation
     * @return a future that will complete on total completion of the operation
     * @throws RejectedExecutionException thrown if the operation is rejected
     */
    public ListenableFuture<Operation> submit(Operation operation, World world, @Nullable String name, @Nullable Object owner);

}
