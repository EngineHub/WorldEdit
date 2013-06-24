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

import com.sk89q.worldedit.WorldEditException;

/**
 * An operation base class that can wrap other operations and call callbacks
 * on success or failure.
 */
public abstract class OperationWrapper implements Operation {
    
    private Operation operation;

    /**
     * Create a new wrapper for the given operation.
     * 
     * @param operation the operation
     */
    public OperationWrapper(Operation operation) {
        this.operation = operation;
    }

    @Override
    public final Operation resume(ExecutionHint opt) throws WorldEditException {
        Operation operation = this.operation;
        try {
            this.operation = operation.resume(opt);
            onResume(operation, this.operation, true);
        } catch (Throwable t) {
            onResume(operation, this.operation, false);
            onFailure(t);
            if (t instanceof WorldEditException) {
                throw (WorldEditException) t;
            } else {
                throw new RuntimeException("Error in wrapped exception", t);
            }
        }

        if (this.operation == null) {
            onSuccess(operation);
            return null;
        } else {
            return this;
        }
    }

    @Override
    public final void cancel() {
        try {
            Operation operation = this.operation;
            if (operation != null) {
                operation.cancel();
            }
        } finally {
            onFailure(new InterruptedException());
        }
    }

    /**
     * Called after {@link #resume(ExecutionHint)} is called.
     * 
     * @param current the operation that was just run
     * @param next the next operation, possibly null
     * @Param success true if this was after a successful call
     */
    protected abstract void onResume(Operation current, Operation next, boolean success);

    /**
     * Called on operation failure.
     * 
     * @param t the exception
     */
    protected abstract void onFailure(Throwable t);

    /**
     * Called on operation success.
     * 
     * @param operation the last operation
     */
    protected abstract void onSuccess(Operation operation);

}
