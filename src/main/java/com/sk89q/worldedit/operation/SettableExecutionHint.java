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

/**
 * A settable implementation of {@link ExecutionHint}.
 */
public class SettableExecutionHint implements ExecutionHint {

    private boolean preferSingleRun = false;
    private int blockCount = Integer.MAX_VALUE;

    @Override
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

    @Override
    public int getBlockCount() {
        return blockCount;
    }

    /**
     * Set the recommended number of blocks to change.
     * 
     * @param blockCount the count
     */
    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

}
