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
 * @author Albert
 */
public class RegionClipboard {
    private int[][][] data;
    private Point<Integer> min;
    private Point<Integer> max;
    private Point<Integer> origin;

    /**
     * Constructs the region instance. The minimum and maximum points must be
     * the respective minimum and maximum numbers!
     * 
     * @param min
     * @param max
     * @param origin
     */
    public RegionClipboard(Point<Integer> min, Point<Integer> max, Point<Integer> origin) {
        this.min = min;
        this.max = max;
        this.origin = origin;
        data = new int[(max.getX()) - min.getX() + 1]
            [max.getY() - min.getY() + 1]
            [max.getZ() - min.getZ() + 1];
    }

    /**
     * Copy to the clipboard.
     *
     * @param editSession
     */
    public void copy(EditSession editSession) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    data[x - min.getX()][y - min.getY()][z - min.getZ()] =
                        editSession.getBlock(x, y, z);
                }
            }
        }
    }

    /**
     * Paste from the clipboard.
     *
     * @param editSession
     * @param origin Position to paste it from
     * @param noAir True to not paste air
     */
    public void paste(EditSession editSession, Point<Integer> newOrigin, boolean noAir) {
        int xs = max.getX() - min.getX();
        int ys = max.getY() - min.getY();
        int zs = max.getZ() - min.getZ();
        int offsetX = min.getX() - origin.getX() + newOrigin.getX();
        int offsetY = min.getY() - origin.getY() + newOrigin.getY();
        int offsetZ = min.getZ() - origin.getZ() + newOrigin.getZ();
        
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y <= ys; y++) {
                for (int z = 0; z <= zs; z++) {
                    if (noAir && data[x][y][z] == 0) { continue; }
                    
                    editSession.setBlock(x + offsetX, y + offsetY, z + offsetZ,
                                         data[x][y][z]);
                }
            }
        }
    }
}
