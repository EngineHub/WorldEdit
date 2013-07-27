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

package com.sk89q.rebar.event;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.rebar.util.Ownable;
import com.sk89q.rebar.util.Owner;

/**
 * Executes a list of events contained within.
 * 
 * @param <T> the event that this list is for
 */
public class ExecutorList<T extends Event> implements Set<EventExecutor<T>>, EventExecutor<T> {
    
    private static final Logger logger = Logger.getLogger(ExecutorList.class.getCanonicalName());
    
    private final ExecutorComparator comparator = new ExecutorComparator();
    private final SortedSet<EventExecutor<T>> internalSet = new TreeSet<EventExecutor<T>>(comparator);
    // Note: TreeSets have log(n) time for add/remove/contains
    // When we execute events, we loop over the array below
    // Nulls are blocked by the comparator
    
    @SuppressWarnings("unchecked") // Can't new EventExecutor<T>[0] apparently
    private EventExecutor<T>[] loopedArray = new EventExecutor[0];

    @Override
    public int getPriority() {
        return Priority.DEFAULT;
    }

    /**
     * Executes an event against this list of event handlers.
     * 
     * <p>Errors are currently only logged to the logger under this class' name.</p>
     */
    @Override
    public void execute(T event) {
        boolean isCancellable = event instanceof Cancellable;
        
        for (int i = 0; i < loopedArray.length; i++) {
            try {
                EventExecutor<T> executor = loopedArray[i];
                if (executor.isIgnoringCancelled() && 
                        (isCancellable && ((Cancellable) event).isCancelled())) {
                    continue; // Ignore cancelled events
                }
                
                loopedArray[i].execute(event);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Failed to execute event handler", t);
            }
        }
    }
    
    /**
     * Updates the internal array.
     * 
     * @return always true
     */
    @SuppressWarnings("unchecked")
    private synchronized boolean update() {
        EventExecutor<T>[] loopedArray = new EventExecutor[internalSet.size()];
        internalSet.toArray(loopedArray);
        this.loopedArray = loopedArray;
        return true;
    }

    @Override
    public synchronized boolean add(EventExecutor<T> e) {
        // Nulls blocked by comparator
        return internalSet.add(e) && update();
    }

    @Override
    public synchronized boolean addAll(Collection<? extends EventExecutor<T>> c) {
        return internalSet.addAll(c) && update();
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return internalSet.retainAll(c) && update();
    }

    @Override
    public synchronized boolean remove(Object o) {
        return internalSet.remove(o) && update();
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return internalSet.removeAll(c) && update();
    }

    /**
     * Removed all executors (implementing {@link Ownable}) that are owned by the
     * given owner.
     * 
     * @param owner the owner to test again
     * @return true if the list was changed
     */
    public synchronized boolean removeOwned(Owner owner) {
        boolean changed = false;
        
        Iterator<EventExecutor<T>> it = internalSet.iterator();
        while (it.hasNext()) {
            EventExecutor<?> executor = it.next();
            if (executor instanceof Ownable) {
                if (((Ownable) executor).getOwner().equals(owner)) {
                    it.remove();
                    changed = true;
                }
            }
        }
        
        if (changed) {
            update();
        }
        
        return changed;
    }

    @Override
    public synchronized boolean contains(Object o) {
        return internalSet.contains(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return internalSet.containsAll(c);
    }

    @Override
    public synchronized void clear() {
        internalSet.clear();
        update();
    }

    @Override
    public synchronized Object[] toArray() {
        return internalSet.toArray();
    }

    @Override
    public synchronized <E> E[] toArray(E[] a) {
        return internalSet.toArray(a);
    }

    @Override
    public int size() {
        return loopedArray.length;
    }

    @Override
    public boolean isEmpty() {
        return loopedArray.length == 0;
    }

    @Override
    public Iterator<EventExecutor<T>> iterator() {
        final Iterator<EventExecutor<T>> it = internalSet.iterator();
        
        return new Iterator<EventExecutor<T>>() {
            @Override
            public synchronized boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public synchronized EventExecutor<T> next() {
                return it.next();
            }

            @Override
            public synchronized void remove() {
                it.remove();
                update();
            }
        };
    }

    @Override
    public boolean isIgnoringCancelled() {
        return false;
    }

}
