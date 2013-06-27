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

import java.util.Collection;
import java.util.Iterator;

/**
 * A collection of {@link ReversibleChange}s, which can be used as an undo or redo log.
 */
public interface ChangeLog extends Collection<ReversibleChange> {
    
    /**
     * Get an iterator over the block changes that returns values in reverse.
     * 
     * <p>This can be utilized to perform an "undo" operation.</p>
     * 
     * @return a reverse iterator
     */
    Iterator<ReversibleChange> descendingIterator();

}
