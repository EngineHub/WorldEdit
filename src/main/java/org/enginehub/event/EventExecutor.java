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

package org.enginehub.event;

/**
 * Handles a specific type of event.
 * 
 * @param <T> the event the handler is for
 * @see EventDispatcher handles all events
 */
public interface EventExecutor<T extends Event> {
    
    /**
     * Processes the event accordingly.
     * 
     * <p>Calling code should be aware that an event executor may throw an
     * unexpected exception and cause problems upstream.</p>
     * 
     * @param event the event
     * @throws EventException on an event related exception
     */
    void execute(T event) throws EventException;
    
    /**
     * Get the priority of the executor.
     * 
     * <p>The priority of the handler cannot be changed after it has been
     * registered with an {@link EventDispatcher} or an {@link ExecutorList}, as
     * it will not take effect.</p.
     * 
     * @return the priority
     * @see Priority
     */
    int getPriority();
    
    /**
     * Checks if cancelled events are skipped.
     * 
     * @return true if cancelled events should not be given to this executor
     */
    public boolean isIgnoringCancelled();

}
