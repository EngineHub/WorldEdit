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

import java.util.PriorityQueue;
import java.util.Queue;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Executes operations through interfacing code calling {@link #resume()} repeatedly,
 * possibly every second or even more frequently.
 */
public class CallbackExecutor implements OperationExecutor {

    private final Queue<QueuedOperation> queue = new PriorityQueue<QueuedOperation>();
    private int queueSize = Integer.MAX_VALUE;
    private QueuedOperation current;
    private int interval = 1;
    private int cycle = 0;

    /**
     * GSet the maximum number of operations that can reside in the queue.
     * 
     * @return the maximum queue size
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Set the maximum number of operations that can reside in the queue.
     * 
     * @param queueSize the maximum queue size
     */
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * Get the interval of calls to {@link #resume()} that actually do something.
     * 
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Set the interval of calls to {@link #resume()} that actually do something.
     * 
     * @param interval the interval
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public ListenableFuture<Operation> offer(Operation operation) 
            throws RejectedOperationException {
        SettableFuture<Operation> future = SettableFuture.create();
        QueuedOperation entry = new QueuedOperation(operation, 0, future);
        synchronized (this) {
            if (queue.size() >= queueSize) {
                throw new RejectedOperationException();
            }
            queue.add(entry);
        }
        return future;
    }
    
    @Override
    public boolean resume() {
        QueuedOperation entry = current;
        
        // No operation, get one from the queue
        synchronized (this) {
            if (cycle++ != 0) {
                if (cycle >= interval) {
                    cycle = 0;
                }
                return false;
            }
            
            if (entry == null) {
                entry = current = queue.poll();
            }
        }
        
        if (entry == null) {
            return false; // Nothing to do
        }
        
        try {
            SettableExecutionHint hint = new SettableExecutionHint();
            hint.setBlockCount(10000);
            
            Operation newOperation = entry.getOperation().resume(hint);
            if (newOperation != null) {
                // Continue with returned operation
                entry.setOperation(newOperation);
            } else {
                current = null;
                
                // We're done
                entry.getFuture().set(entry.getOperation());
            }
        } catch (Throwable t) {
            current = null;
            
            // Error out
            entry.getFuture().setException(t);
        }
        
        return true;
    }

    @Override
    public void run() {
        resume();
    }

}