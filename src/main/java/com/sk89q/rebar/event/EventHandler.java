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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience annotation to mark an event handler.
 * 
 * <p>This must be placed on only public methods.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    
    /**
     * The priority of the event.
     * 
     * <p>Priority affects the order in which events are handled.</p>
     * 
     * @return the priority, default {@link Priority#DEFAULT}
     */
    int priority() default Priority.DEFAULT;
    
    /**
     * Convenient attribute to have this event handler skipped if the event has
     * been cancelled.
     * 
     * @return true if cancelled
     */
    boolean ignoreCancelled() default false;

}
