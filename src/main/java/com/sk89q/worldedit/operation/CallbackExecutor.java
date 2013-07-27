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

import static com.sk89q.worldedit.operation.OperationState.*;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Executes operations through interfacing code calling {@link #resume()} repeatedly,
 * possibly every second or even more frequently.
 */
public class CallbackExecutor implements OperationExecutor {

    private static final Logger logger =
            Logger.getLogger(CallbackExecutor.class.getCanonicalName());

    private final Queue<QueuedOperationEntry> queue = 
            new PriorityQueue<QueuedOperationEntry>();
    private ExecutionHint hint = new ImmutableHint(10000, false);
    private int queueSize = Integer.MAX_VALUE;
    private QueuedOperationEntry current;
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
     * Get the execution hint.
     * 
     * @return the execution hint
     */
    public ExecutionHint getHint() {
        return hint;
    }

    /**
     * Set the execution hint.
     * 
     * @param hint the execution hint
     */
    public void setHint(ExecutionHint hint) {
        this.hint = hint;
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
    public QueuedOperation submit(Operation operation)
            throws RejectedOperationException {
        SettableFuture<Operation> future = SettableFuture.create();
        QueuedOperationEntry entry = new CallbackQueuedOperation(operation, 0, future);
        synchronized (this) {
            if (queue.size() >= queueSize) {
                throw new RejectedOperationException();
            }
            queue.add(entry);
        }
        return entry;
    }
    
    @Override
    public boolean resume() {
        QueuedOperationEntry entry;
        
        // No operation, get one from the queue
        synchronized (this) {
            entry = current;

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

        entry.setStateIf(RUNNING, QUEUED);
        
        try {
            Operation newOperation = entry.getOperation().resume(hint);
            if (newOperation != null) {
                // Continue with returned operation
                entry.setOperation(newOperation);
            } else {
                synchronized (this) {
                    current = null;
                }
                
                // We're done
                entry.setStateIf(COMPLETED, RUNNING);
                entry.getFuture().set(entry.getOperation());
            }
        } catch (Throwable t) {
            synchronized (this) {
                current = null;
            }
            
            // Error out
            entry.setStateIf(FAILED, RUNNING);
            entry.getFuture().setException(t);
        }
        
        return true;
    }

    /**
     * Cancel an operation and call its
     * {@link com.sk89q.worldedit.operation.Operation#cancel()}} method.
     *
     * @param target the operation to cancel
     * @return whether the operation was cancelled
     */
    private synchronized boolean performCancel(CallbackQueuedOperation target) {
        queue.remove(target);

        if (target.setStateIf(CANCELLED, QUEUED, RUNNING)) {
            if (target == current) {
                current = null;
            }

            try {
                target.getOperation().cancel();
            } catch (Throwable t) {
                logger.log(Level.WARNING,
                        "Operation threw an exception when cancelled", t);
            }

            target.getFuture().setException(new InterruptedException());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized List<QueuedOperation> cancelAll() {
        List<QueuedOperation> list = new ArrayList<QueuedOperation>();
        QueuedOperation current = this.current;
        
        if (current != null) {
            if (current.cancel()) {
                list.add(current);
            }
        }
        
        for (QueuedOperationEntry entry : queue) {
            if (entry.cancel()) {
                list.add(entry);
            }
        }
        
        return list;
    }

    @Override
    public synchronized List<QueuedOperation> getQueue() {
        List<QueuedOperation> list = new ArrayList<QueuedOperation>();
        
        if (current != null) {
            list.add(current);
        }
        
        for (QueuedOperationEntry entry : queue) {
            list.add(entry);
        }
        
        return list;
    }

    @Override
    public void run() {
        resume();
    }

    private class CallbackQueuedOperation extends QueuedOperationEntry {
        private CallbackQueuedOperation(Operation operation, int priority,
                                    SettableFuture<Operation> future) {
            super(operation, priority, future);
        }

        @Override
        public boolean cancel() {
            return performCancel(this);
        }
    }

}