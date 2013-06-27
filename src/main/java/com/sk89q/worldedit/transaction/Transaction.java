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

package com.sk89q.worldedit.transaction;

import com.sk89q.worldedit.foundation.Extent;
import com.sk89q.worldedit.foundation.MutableExtent;
import com.sk89q.worldedit.operation.Operation;

/**
 * A transaction is a {@link MutableExtent} that can override block placement
 * behavior to work better on an implementation.
 * 
 * <p>The simplest type of transaction merely passes block changes onto an underlying
 * {@link Extent}, but a more advanced one may re-order block placements so that
 * "attached" blocks such as torches are placed only after the block that it sits on
 * has been placed. Another type of transaction may set blocks using one pass and
 * then apply physics and lighting on a second pass.</p>
 */
public interface Transaction extends MutableExtent {
    
    /**
     * Get whether physics should be applied.
     * 
     * @return true if physics should be applied
     */
    boolean getApplyPhysics();
    
    /**
     * Set whether physics should be applied.
     * 
     * @param applyPhysics true if physics should be applied.
     */
    void setApplyPhysics(boolean applyPhysics);
    
    /**
     * Get whether lighting should be re-calculated.
     * 
     * @return true if lighting should be re-calculated
     */
    boolean getCalculateLighting();
    
    /**
     * Set whether lighting should be-recalculated.
     * 
     * @param calculateLighting true if lighting should be re-calculated
     */
    void setCalculateLighting(boolean calculateLighting);
    
    /**
     * Get an operation to run at the very end in order to flush changes and
     * perform any cleanup.
     * 
     * @return an operation, or null if nothing needs to be done
     */
    Operation getFlushOperation();

}
