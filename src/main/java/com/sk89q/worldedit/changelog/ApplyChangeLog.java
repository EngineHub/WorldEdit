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

import java.util.Iterator;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.foundation.Extent;
import com.sk89q.worldedit.operation.ChangeCountable;
import com.sk89q.worldedit.operation.ExecutionHint;
import com.sk89q.worldedit.operation.ExecutionWatch;
import com.sk89q.worldedit.operation.Operation;

/**
 * Revert or re-apply a {@link ChangeLog}.
 */
public class ApplyChangeLog implements Operation, ChangeCountable {
    
    private final Extent extent;
    private final ChangeLog changeLog;
    private final Iterator<ReversibleChange> it;
    private final boolean revert;
    private int affected = 0;

    /**
     * Create a new instance.
     * 
     * @param extent the extent
     * @param changeSet the change set
     * @param revert true to revert, false to re-apply
     * @see ApplyChangeLog#createRedo(Extent, ChangeLog)
     * @see ApplyChangeLog#createUndo(Extent, ChangeLog)
     */
    private ApplyChangeLog(Extent extent, ChangeLog changeSet, boolean revert) {
        this.extent = extent;
        this.changeLog = changeSet;
        this.it = revert ? changeSet.descendingIterator() : changeSet.iterator();
        this.revert = revert;
    }

    /**
     * Get the extent.
     * 
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Get the change log.
     * 
     * @return the change log
     */
    public ChangeLog getChangeLog() {
        return changeLog;
    }

    @Override
    public Operation resume(ExecutionHint opt) throws WorldEditException, InterruptedException {
        ExecutionWatch watch = opt.createWatch();
        
        while (it.hasNext() && watch.shouldContinue()) {
            ReversibleChange change = it.next();
            if (revert) {
                change.revert(extent);
            } else {
                change.apply(extent);
            }
            affected++;
        }

        return it.hasNext() ? this : null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public int getChangeCount() {
        return affected;
    }

    /**
     * Create an undo operation.
     * 
     * @param extent the extent
     * @param changeLog the change set
     * @return an operation to run
     */
    public static ApplyChangeLog createUndo(Extent extent, ChangeLog changeLog) {
        return new ApplyChangeLog(extent, changeLog, true);
    }
    
    /**
     * Create a redo operation.
     * 
     * @param extent the extent
     * @param changeLog the change set
     * @return an operation to run
     */
    public static ApplyChangeLog createRedo(Extent extent, ChangeLog changeLog) {
        return new ApplyChangeLog(extent, changeLog, false);
    }

}
