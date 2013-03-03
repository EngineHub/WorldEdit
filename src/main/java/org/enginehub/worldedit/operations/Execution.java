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

package org.enginehub.worldedit.operations;

/**
 * Provides hints about the current execution cycle. Operations should utilize this
 * information, but they may not.
 */
public class Execution {
    
    private boolean preferSingleRun = false;

    /**
     * Returns whether a single run is preferred.
     * 
     * @return true if the operation should complete as much as possible in one execution
     */
    public boolean preferSingleRun() {
        return preferSingleRun;
    }

    /**
     * Set whether a single run is preferred.
     * 
     * @param preferSingleRun true if the operation should complete ASAP
     */
    public void setPreferSingleRun(boolean preferSingleRun) {
        this.preferSingleRun = preferSingleRun;
    }

}
