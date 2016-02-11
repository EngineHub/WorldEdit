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

import com.sk89q.worldedit.WorldEditException;

import java.util.Collection;

public class SinglePassOperationQueue extends OperationQueue {
    /**
     * Create a new queue containing no operations.
     */
    public SinglePassOperationQueue() {
    }

    /**
     * Create a new queue with operations from the given collection.
     *
     * @param operations a collection of operations
     */
    public SinglePassOperationQueue(Collection<Operation> operations) {
        super(operations);
    }

    /**
     * Create a new queue with operations from the given array.
     *
     * @param operation an array of operations
     */
    public SinglePassOperationQueue(Operation... operation) {
        super(operation);
    }

    @Override
    protected Operation runOperation(Operation operation, RunContext context) throws WorldEditException {
        return operation.resume(new RunContext());
    }
}
