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


/**
 * Executes {@link Operation}s that is given to it.
 * 
 * <p>Implementations may have a bounded queue or no queue at all.</p>
 */
public interface OperationExecutor extends Runnable {

    /**
     * Add an operation to the queue.
     * 
     * @param operation the operation
     * @return a future
     * @throws RejectedOperationException thrown if there is no room
     */
    ListenableFuture<Operation> offer(Operation operation)
            throws RejectedOperationException;

    /**
     * Execute a step in the current operation.
     * 
     * <p>This method will block until the operation returns.</p>
     * 
     * @return true if an operation was executed
     */
    boolean resume();

}
