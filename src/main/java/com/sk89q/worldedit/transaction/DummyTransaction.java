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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.foundation.Extent;
import com.sk89q.worldedit.operation.Operation;

/**
 * A transaction that just passes calls to the underlying {@link Extent}.
 */
public class DummyTransaction extends AbstractTransaction {
    
    private final Extent extent;

    /**
     * Create a new instance with the given extent.
     * 
     * @param extent the extent
     */
    public DummyTransaction(Extent extent) {
        this.extent = extent;
    }

    @Override
    public Operation getFlushOperation() {
        return null;
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) {
        return extent.setBlock(location, block);
    }
    

}
