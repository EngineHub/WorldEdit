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

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEditException;

/**
 * Wraps another operation and allows for tracking the owner of an operation.
 */
public class OwnedOperation implements Operation {

    private final LocalPlayer owner;
    private final long creationTime = System.currentTimeMillis();
    private final Operation originalOperation;
    private Operation operation;

    /**
     * Create a new operation with the given owner and operation.
     *
     * @param owner the owner
     * @param operation the operation
     */
    public OwnedOperation(LocalPlayer owner, Operation operation) {
        this.owner = owner;
        this.operation = operation;
        this.originalOperation = operation;
    }

    @Override
    public Operation resume(ExecutionHint opt) throws WorldEditException {
        operation = operation.resume(opt);
        if (operation == null) {
            return null;
        }
        return this;
    }

    @Override
    public void cancel() {
        operation.cancel();
    }

    /**
     * Get the operation that this object was created with.
     * 
     * @return the operation
     */
    public Operation getOperation() {
        return originalOperation;
    }

    /**
     * Get the owner of this operation.
     * 
     * @return the owner
     */
    public LocalPlayer getOwner() {
        return owner;
    }

    /**
     * Get the time when this operation was created.
     * 
     * @return the time in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }

}
