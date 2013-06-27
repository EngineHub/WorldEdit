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
 * An task that may be split into multiple steps to be run sequentially immediately
 * or at a varying or fixed interval. Operations should attempt to break apart tasks
 * into smaller tasks that can be completed in quicker successions.
 */
public interface Operation {
    
    /**
     * Complete the next step. If this method returns another operation, then this
     * method is to be called for the given operation, or possibly never.
     * If this method returns null, then the operation is complete.
     * 
     * @param opt hints about the execution
     * @return another operation to run that operation again, or null to stop
     * @throws WorldEditException an error
     * @throws InterruptedException on interruption
     */
    Operation resume(ExecutionHint opt) throws WorldEditException, InterruptedException;
    
    /**
     * Abort the current task. After the this method is called, 
     * {@link #resume(ExecutionHint)} should not be called at any point in the future. This
     * method should not be called after successful completion of the operation. This
     * method must be called if the operation is interrupted before completion. When an
     * operation is cancelled, it should stop where it has progressed and should
     * generally not undo previous actions.
     */
    void cancel();

}
