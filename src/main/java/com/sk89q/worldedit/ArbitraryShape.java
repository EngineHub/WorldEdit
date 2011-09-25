// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit;

import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

public abstract class ArbitraryShape {
    private Region extent;

    public ArbitraryShape(Region extent) {
        this.extent = extent;
    }

    protected Region getExtent() {
        return extent;
    }

    protected abstract boolean isInside(double x, double y, double z);

    public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
        int affected = 0;

        for (BlockVector position : getExtent()) {
            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            if (!isInside(x, y, z))
                continue;

            if (hollow) {
                boolean draw = false;
                do {
                    if (!isInside(x+1, y, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x-1, y, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y+1, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y-1, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y, z+1)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y, z-1)) {
                        draw = true;
                        break;
                    }
                } while (false);

                if (!draw) {
                    continue;
                }
            }

            if (editSession.setBlock(position, pattern)) {
                ++affected;
            }
        }

        return affected;
    }
}
