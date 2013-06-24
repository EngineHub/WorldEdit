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

import com.sk89q.worldedit.LocalPlayer;

/**
 * Holds metadata for tracking the owner of an operation.
 */
public class PlayerIssuedOperation {

    private final String label;
    private final LocalPlayer owner;
    private final long creationTime = System.currentTimeMillis();

    /**
     * Create a new operation with the given owner and operation.
     *
     * @param label the label describing this operation
     * @param owner the owner
     */
    public PlayerIssuedOperation(String label, LocalPlayer owner) {
        this.label = label;
        this.owner = owner;
    }

    /**
     * Get the label describing this operation.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Get the owner of this operation.
     * 
     * @return the owner
     */
    public LocalPlayer getOwner() {
        return owner;
    }

    /**
     * Get the time when this operation was created.
     * 
     * @return the time in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }

}
