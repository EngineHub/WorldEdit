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
import com.google.common.util.concurrent.SettableFuture;

import java.util.Collections;
import java.util.List;

/**
 * An executor that completes operations immediately when they is offered.
 */
public class ImmediateExecutor implements OperationExecutor {

    @Override
    public void run() { 
    }

    @Override
    public QueuedOperation offer(Operation operation)
            throws RejectedOperationException {
        final SettableFuture<Operation> future = SettableFuture.create();
        Throwable thrown = null;
        
        try {
            SettableExecutionHint hint = new SettableExecutionHint();
            hint.setPreferSingleRun(true);

            OperationHelper.complete(operation, hint);
            future.set(operation);
        } catch (Throwable t) {
            future.setException(t);
            thrown = t;
        }

        final boolean successful = thrown == null;
        
        return new QueuedOperation() {
            @Override
            public ListenableFuture<Operation> getFuture() {
                return future;
            }

            @Override
            public OperationState getState() {
                return successful ? OperationState.COMPLETED : OperationState.FAILED;
            }

            @Override
            public boolean cancel() {
                return false;
            }
        };
    }

    @Override
    public boolean resume() {
        return false;
    }

    @Override
    public int cancelAll() {
        return 0;
    }

    @Override
    public List<QueuedOperation> getQueue() {
        return Collections.emptyList();
    }

}
