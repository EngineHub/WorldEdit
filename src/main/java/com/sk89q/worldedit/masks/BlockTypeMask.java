// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.masks;

import java.util.HashSet;
import java.util.Set;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;

/**
 * A filter that matches blocks based on block types.
 *
 * @author sk89q
 */
public class BlockTypeMask implements Mask {
    protected Set<Integer> types;

    public BlockTypeMask() {
        types = new HashSet<Integer>();
    }

    public BlockTypeMask(Set<Integer> types) {
        this.types = types;
    }

    public BlockTypeMask(int type) {
        this();
        add(type);
    }

    public void add(int type) {
        types.add(type);
    }

    public boolean matches(EditSession editSession, Vector pos) {
        return types.contains(editSession.getBlockType(pos));
    }

}
