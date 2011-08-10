// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.UnsupportedFlagException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.tools.enums.ToolFlag;

@Deprecated
public class HollowSphereBrush extends SphereBrush {
    public HollowSphereBrush() {
        try {
            this.flags.add(ToolFlag.HOLLOW);
        } catch (UnsupportedFlagException e) {
            
        }
    }
    
    @Override
    @Deprecated
    public void build(EditSession editSession, Vector pos, Pattern mat, double size)
            throws MaxChangedBlocksException {
        boolean hollow = flags.contains(ToolFlag.HOLLOW);
        try {
            this.flags.add(ToolFlag.HOLLOW);
        } catch (UnsupportedFlagException e) {
            
        }
        super.build(editSession, pos, mat, size);
        if(!hollow) {
            this.flags.remove(ToolFlag.HOLLOW);
        }
    }
}
