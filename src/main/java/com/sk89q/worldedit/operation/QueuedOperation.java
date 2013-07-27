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

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.rebar.util.Describable;

/**
 * Stores the state of an operation that has been added to an {@link OperationExecutor}.
 */
public interface QueuedOperation extends Describable {
    
    /**
     * Get the current operation that is queued.
     * 
     * <p>This may not be the same as the original operation.</p>
     * 
     * @return the operation, or null if there is no more operation to execute
     */
    Operation getOperation();

    /**
     * Get the future.
     *
     * @return the future
     */
    ListenableFuture<Operation> getFuture();

    /**
     * Get the state of this operation.
     *
     * @return the operation
     */
    OperationState getState();

    /**
     * Cancel the operation referenced by this object.
     *
     * <p>This method may be called several times or after the operation has completed
     * or has been cancelled to no effect.</p>
     *
     * <p>A call to this method is merely a strong suggestion to interrupt the
     * operation, but the operation may still complete to the end if it does not
     * have proper interruption checks.</p>
     *
     * @return true if the operation was in a cancellable state
     */
    boolean cancel();

}
