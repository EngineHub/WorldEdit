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
import com.sk89q.worldedit.HeightMap;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.tools.delegates.ToolIterationsDelegate;
import com.sk89q.worldedit.tools.delegates.ToolSizeDelegate;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolIterations;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolSize;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolWithIterations;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolWithSize;

public class SmoothBrush implements Brush,
                                    ToolWithSize,
                                    ToolWithIterations {
    protected ToolIterations iterations = new ToolIterationsDelegate(1);
    protected ToolSize size = new ToolSizeDelegate(true, true, true);
    
    public SmoothBrush() { 
    }
    
    public SmoothBrush(int iterations) {
        this.iterations.set(iterations);
    }
    
    @Deprecated
    public void build(EditSession editSession, Vector pos, Pattern mat, double size)
            throws MaxChangedBlocksException {
        double rad = size;
        Vector min = pos.subtract(rad, rad, rad);
        Vector max = pos.add(rad, rad + 10, rad);
        Region region = new CuboidRegion(min, max);
        HeightMap heightMap = new HeightMap(editSession, region);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        heightMap.applyFilter(filter, iterations.get());
    }

    public ToolIterations iterations() {
        return iterations;
    }

    public ToolSize size() {
        return size;
    }

    public void build(EditSession editSession, Vector pos)
            throws MaxChangedBlocksException {
        double rad = this.size().getX();
        Vector min = pos.subtract(rad, rad, rad);
        Vector max = pos.add(rad, rad + 10, rad);
        Region region = new CuboidRegion(min, max);
        HeightMap heightMap = new HeightMap(editSession, region);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        heightMap.applyFilter(filter, iterations.get());
    }
}
