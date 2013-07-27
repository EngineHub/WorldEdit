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
    private Throwable thrown;
    private boolean completed = false;

    /**
     * Create a new wrapper for the given operation.
     * 
     * @param operation the operation
     */
    public OperationWrapper(Operation operation) {
        this.operation = operation;
    }

    @Override
    public final Operation resume(ExecutionHint opt) throws WorldEditException,
            InterruptedException {
        Operation operation = this.operation;
        
        if (!completed) { // Run the wrapped operation
            try {
                this.operation = operation.resume(opt);
                onResume(operation, this.operation, true);
                if (this.operation == null) {
                    onSuccess(operation);
                } else {
                    return this;
                }
            } catch (Throwable t) {
                onResume(operation, this.operation, false);
                onFailure(t);
                thrown = t;
            }
            
            completed = true;
            this.operation = nextOperation(thrown == null);
            return this;
        } else {
            this.operation = operation.resume(opt);
            
            if (this.operation == null) {
                if (thrown != null) {
                    if (thrown instanceof WorldEditException) {
                        throw (WorldEditException) thrown;
                    } else {
                        throw new RuntimeException("Error in wrapped operation", thrown);
                    }
                } else {
                    return null;
                }
            } else {
                return this;
            }
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
    
    /**
     * Return the next operation to run.
     * 
     * <p>This method is called after the wrapped operation has completed (either on 
     * success or on failure), after either {@link #onFailure(Throwable)} or
     * {@link #onSuccess(Operation)} has been called, to get a new operation to continue
     * with. When the returned operation completes, this method will be called again to
     * get a new operation, and this will continue until this method returns null.
     * Operations returned by this method will not trigger calls to
     * {@link #onResume(Operation, Operation, boolean)} or the other callbacks. If the
     * original operation fails, then after the operations returned by this method
     * completes, the original exception will be re-thrown. This means that operations
     * run by this method are guaranteed to be run, as long as one of them does not
     * throw an exception before the later operations can complete. Any exceptions thrown
     * by operations returned by this method will mask exceptions thrown
     * by the wrapped exception.</p>
     * 
     * @param success true if the wrapped successful had completed without failure
     * @return an operation, or null to complete
     */
    protected abstract Operation nextOperation(boolean success);

}
