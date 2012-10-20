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

package com.sk89q.worldedit.operations;

import com.sk89q.worldedit.WorldEditException;

/**
 * Helper abstract implementation of {@link Operation}.
 */
public abstract class AbstractOperation implements Operation {

    @Override
    public Operation resume(Execution opt) throws WorldEditException {
        return resume();
    }

    /**
     * Complete the next step. If this method returns true, then the method may be
     * called again in the future, or possibly never. If this method returns false,
     * then this method should not be called again.
     * 
     * @return another operation to run that operation again, or null to stop
     * @throws WorldEditException an error
     */
    protected Operation resume() throws WorldEditException {
        return null;
    }

}
