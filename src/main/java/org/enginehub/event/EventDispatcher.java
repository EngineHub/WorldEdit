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
 * Handles more than one kind of event.
 * 
 * @see EventExecutor handles one event type only
 */
public interface EventDispatcher {
    
    /**
     * Processes the event accordingly.
     * 
     * <p>Event dispatchers are generally expected to handle exception thrown by
     * {@link EventExecutor}s, but for absolute safety, calling code may want to consider
     * wrapping this call in an exception try/catch block.</p>
     * 
     * @param event the event to handle
     * @return the same event (for convenience)
     */
    Event dispatch(Event event);

}
