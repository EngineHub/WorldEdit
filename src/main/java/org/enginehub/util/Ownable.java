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

package org.enginehub.util;

/**
 * An object that can be owned by another object.
 * 
 * <p>For example, a command might be "owned" by a portion of a program. If all
 * commands owned by that portion need to be unregistered, this class allows tracing
 * back the owner for quick de-registration.</p.
 * 
 * <p>Owners should be compared with {@link #equals(Object)}.</p>
 */
public interface Ownable {
    
    /**
     * Get the owner.
     * 
     * @return the owner object, possibly null
     */
    Owner getOwner();

    /**
     * Set the owner.
     * 
     * @param owner the new owner, or null
     */
     void setOwner(Owner owner);

}
