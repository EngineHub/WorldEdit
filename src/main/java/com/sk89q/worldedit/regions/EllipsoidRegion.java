// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author TomyLobo
 */
public class EllipsoidRegion extends AbstractRegion {
    /**
     * Stores the center.
     */
    private Vector center;
    /**
     * Stores the radiuses plus 0.5 on each axis.
     */
    private Vector radius;
    /**
     * Construct a new instance of this ellipsoid region.
     *
     * @param pos1
     * @param pos2
     */
    public EllipsoidRegion(Vector pos1, Vector pos2) {
        this(null, pos1, pos2);
    }
    
    /**
     * Construct a new instance of this ellipsoid region.
     * 
     * @param world
     * @param center
     * @param radius
     */
    public EllipsoidRegion(LocalWorld world, Vector center, Vector radius) {
        super(world);
        this.center = center;
        setRadius(radius);
    }

    public EllipsoidRegion(EllipsoidRegion ellipsoidRegion) {
        this(ellipsoidRegion.world, ellipsoidRegion.center, ellipsoidRegion.getRadius());
    }

    /**
     * Get the lower point of the ellipsoid.
     *
     * @return min point
     */
    public Vector getMinimumPoint() {
        return center.subtract(getRadius());
    }

    /**
     * Get the upper point of the ellipsoid.
     *
     * @return max point
     */
    public Vector getMaximumPoint() {
        return center.add(getRadius());
    }

    /**
     * Get the number of blocks in the region.
     *
     * @return number of blocks
     */
    public int getArea() {
        return (int) Math.floor((4.0 / 3.0) * Math.PI * radius.getX() * radius.getY() * radius.getZ());
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth() {
        return (int) (2 * radius.getX());
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight() {
        return (int) (2 * radius.getY());
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength() {
        return (int) (2 * radius.getZ());
    }

    /**
     * Expands the ellipsoid in a direction.
     *
     * @param change
     */
    public void expand(Vector change) {
    }

    /**
     * Contracts the ellipsoid in a direction.
     *
     * @param change
     */
    public void contract(Vector change) {
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        center = center.add(change);
    }

    /**
     * Get the center.
     *
     * @return center
     */
    public Vector getCenter() {
        return center;
    }

    /**
     * Set the center.
     *
     * @param center
     */
    public void setCenter(Vector center) {
        this.center = center;
    }

    /**
     * Get the radiuses.
     *
     * @return radiuses
     */
    public Vector getRadius() {
        return radius.subtract(0.5, 0.5, 0.5);
    }

    /**
     * Set radiuses.
     *
     * @param radiuses
     */
    public void setRadius(Vector radius) {
        this.radius = radius.add(0.5, 0.5, 0.5);
    }

    /**
     * Get a list of chunks that this region is within.
     *
     * @return
     */
    public Set<Vector2D> getChunks() {
        Set<Vector2D> chunks = new HashSet<Vector2D>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    Vector pt = new Vector(x, y, z);
                    chunks.add(ChunkStore.toChunk(pt));
                }
            }
        }

        return chunks;
    }

    /**
     * Returns true based on whether the region contains the point,
     *
     * @param pt
     */
    public boolean contains(Vector pt) {
        return pt.subtract(center).divide(radius).lengthSq() <= 1;
    }

    /**
     * Returns string representation in the format
     * "(centerX, centerY, centerZ) - (radiusX, radiusY, radiusZ)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return center + " - " + getRadius();
    }

    public void extendRadius(Vector minRadius) {
        setRadius(Vector.getMaximum(minRadius, getRadius()));
    }
}
