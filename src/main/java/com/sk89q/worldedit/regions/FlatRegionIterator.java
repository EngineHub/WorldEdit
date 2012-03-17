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

package com.sk89q.worldedit.regions;

import java.util.Iterator;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class FlatRegionIterator implements Iterator<Vector2D>  {

    private Region region;
    private int y;
    private int minX;
    private int nextX;
    private int nextZ;
    private int maxX;
    private int maxZ;

    public FlatRegionIterator(Region region) {
        this.region = region;

        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        this.y = min.getBlockY();

        this.minX = min.getBlockX();

        this.nextX = minX;
        this.nextZ = min.getBlockZ();

        this.maxX = max.getBlockX();
        this.maxZ = max.getBlockZ();

        forward();
    }

    @Override
    public boolean hasNext() {
        return nextX != Integer.MIN_VALUE;
    }

    private void forward() {
        while (hasNext() && !region.contains(new Vector(nextX, y, nextZ))) {
            forwardOne();
        }
    }

    @Override
    public Vector2D next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }

        Vector2D answer = new Vector2D(nextX, nextZ);

        forwardOne();
        forward();

        return answer;
    }

    private void forwardOne() {
        if (++nextX <= maxX) {
            return;
        }
        nextX = minX;

        if (++nextZ <= maxZ) {
            return;
        }
        nextX = Integer.MIN_VALUE;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
