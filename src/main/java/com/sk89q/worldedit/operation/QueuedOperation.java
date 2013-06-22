// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.operation;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Used by {@link OperationExecutor} to queue an operation.
 */
class QueuedOperation implements Comparable<QueuedOperation> {

    private Operation operation;
    private final int priority;
    private final SettableFuture<Operation> future;
    
    /**
     * Create a new entry.
     * 
     * @param operation the operation
     * @param priority the priority, where 0 is nominal and higher is more priority
     * @param future the future
     */
    public QueuedOperation(Operation operation, int priority, 
            SettableFuture<Operation> future) {
        this.operation = operation;
        this.priority = priority;
        this.future = future;
    }

    /**
     * Set the operation.
     * 
     * @param operation the operation
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /**
     * Get the operation.
     * 
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }
    
    /**
     * Get the priority of the operation.
     * 
     * @return the priority, where 0 is nominal, and higher values are higher priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Get the future.
     * 
     * @return the future
     */
    public SettableFuture<Operation> getFuture() {
        return future;
    }

    @Override
    public int compareTo(QueuedOperation o) {
        return (priority == o.priority ? 0 : (priority > o.priority ? -1 : 1));
    }

}
