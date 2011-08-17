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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.tools.delegates.ToolFlagsDelegate;
import com.sk89q.worldedit.tools.delegates.ToolPatternDelegate;
import com.sk89q.worldedit.tools.delegates.ToolSizeDelegate;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolFlags;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolPattern;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolSize;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolWithFlags;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolWithPattern;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolWithSize;
import com.sk89q.worldedit.tools.enums.ToolFlag;

public class CylinderBrush implements Brush,
                                      ToolWithSize,
                                      ToolWithPattern,
                                      ToolWithFlags {
    protected ToolFlags flags = new ToolFlagsDelegate(new ToolFlag[]{ToolFlag.HOLLOW});
    protected ToolSize size = new ToolSizeDelegate(true, false, true);
    protected ToolPattern pattern = new ToolPatternDelegate();
    
    public CylinderBrush() {
    }
    
    public CylinderBrush(int height) {
        this.size.setY(height);
    }
    
    @Deprecated
    public void build(EditSession editSession, Vector pos, Pattern mat, double size)
            throws MaxChangedBlocksException {
        editSession.makeCylinder(pos, mat, size, (int)this.size.getY());
    }
    
    public void build(EditSession editSession, Vector pos)
            throws MaxChangedBlocksException {
        editSession.makeCylinder(pos, pattern.get(), size.getX(), (int)size.getY(), flags.contains(ToolFlag.HOLLOW));
    }

    public ToolPattern pattern() {
        return pattern;
    }

    public ToolFlags flags() {
        return flags;
    }

    public ToolSize size() {
        return size;
    }
}
