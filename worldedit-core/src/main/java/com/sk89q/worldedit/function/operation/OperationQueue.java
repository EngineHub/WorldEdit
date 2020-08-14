/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.operation;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Executes multiple queues in order.
 */
public class OperationQueue implements Operation {

    private final List<Operation> operations = Lists.newArrayList();
    private final Deque<Operation> queue = new ArrayDeque<>();
    private Operation current;

    /**
     * Create a new queue containing no operations.
     */
    public OperationQueue() {
    }

    /**
     * Create a new queue with operations from the given collection.
     *
     * @param operations a collection of operations
     */
    public OperationQueue(Collection<Operation> operations) {
        checkNotNull(operations);
        for (Operation operation : operations) {
            offer(operation);
        }
        this.operations.addAll(operations);
    }

    /**
     * Create a new queue with operations from the given array.
     *
     * @param operation an array of operations
     */
    public OperationQueue(Operation... operation) {
        checkNotNull(operation);
        for (Operation o : operation) {
            offer(o);
        }
    }

    /**
     * Add a new operation to the queue.
     *
     * @param operation the operation
     */
    public void offer(Operation operation) {
        checkNotNull(operation);
        queue.offer(operation);
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        if (current == null && !queue.isEmpty()) {
            current = queue.poll();
        }

        if (current != null) {
            current = current.resume(run);

            if (current == null) {
                current = queue.poll();
            }
        }

        return current != null ? this : null;
    }

    @Override
    public void cancel() {
        for (Operation operation : queue) {
            operation.cancel();
        }
        queue.clear();
    }

    @Override
    public Iterable<Component> getStatusMessages() {
        return Iterables.concat(operations.stream().map(Operation::getStatusMessages).collect(Collectors.toList()));
    }

}
