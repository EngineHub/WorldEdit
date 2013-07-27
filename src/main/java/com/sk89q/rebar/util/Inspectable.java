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

package com.sk89q.rebar.util;

/**
 * Indicates an object that can provide information on current progress.
 */
public interface Inspectable {
    
    /**
     * Return the progress of the object as a float between 0 and 1. A negative
     * number indicates that the progress is in an indeterminate state.
     * 
     * @return the progress between 0 and 1, or a negative number for indeterminate
     */
    float getProgress(); 
    
    /**
     * Get a status string describing the current status of the operation.
     * 
     * @return a status string, or null if unavailable
     */
    String getStatus();

}
