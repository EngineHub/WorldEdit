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

package com.sk89q.worldedit.structure;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;

/**
 * A structure can be generated from a given point. Example structures are trees,
 * mushrooms, pyramids, a town, and things of that nature.
 */
public interface Structure {

    /**
     * Generate the structure at the given point. The point generally refers to the
     * starting point of the structure and will often refer to the block directly above
     * the ground. However, the structure may generate blocks and entities below
     * the given point.
     * 
     * @param context the context to place objects in
     * @param position position to start the structure
     * @return true if the structure could be generated
     * @throws WorldEditException on error
     */
    boolean generate(EditSession context, Vector position) throws WorldEditException;
    
}
