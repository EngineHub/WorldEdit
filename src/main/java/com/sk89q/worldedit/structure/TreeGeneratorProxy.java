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
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.structure;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.TreeGenerator;

/**
 * Acts an proxy between the older {@link TreeGenerator} enum-based tree generator and
 * {@link Structure}s.
 */
public class TreeGeneratorProxy implements Structure {
    
    private TreeGenerator type;
    
    /**
     * Construct a new {@link TreeGenerator} proxy.
     * 
     * @param type tree type
     */
    public TreeGeneratorProxy(TreeGenerator type) {
        this.type = type;
    }

    @Override
    public boolean generate(EditSession context, Vector position) throws WorldEditException {
        return type.generate(context, position);
    }

}
