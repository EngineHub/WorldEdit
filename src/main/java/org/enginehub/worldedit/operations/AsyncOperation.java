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
 * This interface indicates that the operation may run in an asynchronous fashion in a
 * thread other than a main one. {@link #nextAsync()} can be called in order
 * to determine whether the next execution of {@link #resume(Execution)} should occur
 * asynchronously. There is no requirement that calling code perform the next
 * execution cycle asynchronously, however.
 */
public interface AsyncOperation extends Operation {

    /**
     * Indicates whether the next call to {@link #resume(Execution)} should be called.
     * asynchronously if possible and feasible.
     * 
     * @return true to execute asynchronously the next cycle
     */
    boolean nextAsync();
    
}
