// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import java.util.Set;

import com.sk89q.worldedit.snapshots.SnapshotRepository;

/**
 * Represents WorldEdit's configuration.
 * 
 * @author sk89q
 */
public abstract class LocalConfiguration {
    protected static final int[] defaultDisallowedBlocks = new int[] {
                6, 7, 14, 15, 16, 21, 22, 23, 24, 25, 26, 27, 28, 29, 39, 31,
                32, 33, 34, 36, 37, 38, 39, 40, 46, 50, 51, 56, 59, 69, 73, 74,
                75, 76, 77, 81, 83
            };

    public boolean profile = false;
    public Set<Integer> disallowedBlocks = null;
    public int defaultChangeLimit = -1;
    public int maxChangeLimit = -1;
    public String shellSaveType = null;
    public SnapshotRepository snapshotRepo = null;
    public int maxRadius = -1;
    public int maxSuperPickaxeSize = 5;
    public boolean logComands = false;
    public boolean registerHelp = true;
    public int wandItem = 271;
    public boolean superPickaxeDrop = true;
    public boolean superPickaxeManyDrop = true;
    public boolean noDoubleSlash = false;
    public boolean useInventory = false;
    public boolean useInventoryOverride = false;
    
    /**
     * Loads the configuration.
     */
    public abstract void load();
}
