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

package com.sk89q.worldedit.changelog;

import com.sk89q.worldedit.foundation.Extent;

/**
 * A change, possibly a part of {@link ChangeLog}, that can be applied and then reversed.
 * 
 * <p>Implementations of this interface may be immutable. Before storing a copy of
 * one of these objects, call {@link #clone()} first and store that object.</p>
 */
public interface ReversibleChange extends Cloneable {

    /**
     * Revert the change, after this change object had been created, or
     * {@link #apply(Extent)} had been previously called.
     * 
     * @param extent the extent to make the change to
     */
    void revert(Extent extent);
    
    /**
     * Re-apply the change, after {@link #revert(Extent)} had been previously called.
     * 
     * @param extent the extent to make the change to
     */
    void apply(Extent extent);
    
    /**
     * Make a clone of this object.
     * 
     * @return the cloned version
     */
    ReversibleChange clone();

}
