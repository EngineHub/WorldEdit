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

package org.enginehub.common;

import org.enginehub.auth.Principal;
import org.enginehub.util.StringIdentifiable;

/**
 * An entity that can perform tasks.
 *
 * <p>Examples of actors include:</p>
 *
 * <ul>
 *     <li>A player in the game</li>
 *     <li>Someone using the server console</li>
 *     <li>Someone using a remote console</li>
 *     <li>A user using a GUI</li>
 * </ul>
 *
 * @param <T> the underlying object
 */
public interface Actor<T> extends Principal, StringIdentifiable {

    /**
     * Get the name of the actor.
     *
     * <p>If the actor does not naturally a name, a generic name can be returned.
     * Names are only for display, and two actors can have the same name.</p>
     *
     * @return the actor's name
     */
    String getName();

    /**
     * Get the underlying object that the implementation abstracts.
     *
     * <p>In code that is meant to be abstracted (i.e. WorldEdit), this should never
     * be called.</p>
     *
     * @return the handle
     */
    T getHandle();

}
