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

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.regions.FlatRegion;

/**
 * Helper class to visit columns in a region.
 * <p>
 * In the future, this class will try to break up column visits in batches
 * according to options set in {@link ExecutionHint}.
 */
public abstract class ColumnVisitor implements Operation {

    private final FlatRegion region;
    
    /**
     * Create a column visitor.
     * 
     * @param region area to apply changes to
     */
    public ColumnVisitor(FlatRegion region) {
        this.region = region;
    }

    @Override
    public Operation resume(ExecutionHint opt) throws WorldEditException {
        Iterator<BlockVector> points = region.columnIterator();
        
        while (points.hasNext()) {
            visitColumn(opt, points.next());
        }

        return null;
    }

    @Override
    public void cancel() {
        // Nothing to clean up
    }
    
    /**
     * Called by {@link #resume(ExecutionHint)} on each column visited in the region.
     * 
     * @param opt execution hints
     * @param columnPt point of column, the highest position in each column
     * @throws WorldEditException an error
     */
    public abstract void visitColumn(ExecutionHint opt, Vector columnPt) throws WorldEditException;

}