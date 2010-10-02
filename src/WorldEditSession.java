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

import com.sk89q.worldedit.*;

/**
 *
 * @author sk89q
 */
public class WorldEditSession {
    private int[] pos1 = new int[3];
    private int[] pos2 = new int[3];
    private boolean hasSetPos1 = false;
    private boolean hasSetPos2 = false;

    private void checkPos1() throws IncompleteRegionException {
        if (!hasSetPos1) {
            throw new IncompleteRegionException();
        }
    }

    private void checkPos2() throws IncompleteRegionException {
        if (!hasSetPos2) {
            throw new IncompleteRegionException();
        }
    }

    public int[] getPos1() throws IncompleteRegionException {
        checkPos1();
        return pos1;
    }

    public void setPos1(int x, int y, int z) {
        hasSetPos1 = true;
        pos1 = new int[]{x, y, z};
    }

    public int[] getPos2() throws IncompleteRegionException {
        checkPos2();
        return pos2;
    }

    public void setPos2(int x, int y, int z) {
        hasSetPos2 = true;
        pos2 = new int[]{x, y, z};
    }

    public int getLowerX() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.min(pos1[0], pos2[0]);
    }

    public int getUpperX() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.max(pos1[0], pos2[0]);
    }

    public int getLowerY() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.min(pos1[1], pos2[1]);
    }

    public int getUpperY() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.max(pos1[1], pos2[1]);
    }

    public int getLowerZ() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.min(pos1[2], pos2[2]);
    }

    public int getUpperZ() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.max(pos1[2], pos2[2]);
    }

    public int getSize() throws IncompleteRegionException {
        return (getUpperX() - getLowerX() + 1) *
               (getUpperY() - getLowerY() + 1) *
               (getUpperZ() - getLowerZ() + 1);
    }
}
