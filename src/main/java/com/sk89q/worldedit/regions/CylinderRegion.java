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

import java.util.HashSet;
import java.util.Set;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;

/**
 * Represents a cylindrical region.
 *
 * @author yetanotherx
 */
public class CylinderRegion extends AbstractRegion {
    private Vector center;
    private Vector2D center2D;
    private Vector2D radius;
    private int minY;
    private int maxY;
    private boolean hasY = false;

    /**
     * Construct the region
     */
    public CylinderRegion() {
        this((LocalWorld) null);
    }

    /**
     * Construct the region.
     * 
     * @param world
     */
    public CylinderRegion(LocalWorld world) {
        this(world, new Vector(), new Vector2D(), 0, 0);
        hasY = false;
    }

    /**
     * Construct the region.
     * 
     * @param world
     * @param points
     * @param minY
     * @param maxY
     */
    public CylinderRegion(LocalWorld world, Vector center, Vector2D radius, int minY, int maxY) {
        super(world);
        this.center = center;
        setRadius(radius);
        this.minY = minY;
        this.maxY = maxY;
        hasY = true;
    }

    public CylinderRegion(CylinderRegion region) {
        this(region.world, region.center, region.getRadius(), region.minY, region.maxY);
        hasY = region.hasY;
    }

    /**
     * Returns the main center point of the cylinder
     * 
     * @return 
     */
    public Vector getCenter() {
        return center;
    }

    /**
     * Sets the main center point of the region
     * 
     */
    public void setCenter(Vector center) {
        this.center = center;
        this.center2D = center.toVector2D();
    }

    /**
     * Returns the radius of the cylinder
     * 
     * @return 
     */
    public Vector2D getRadius() {
        return radius.subtract(0.5, 0.5);
    }

    /**
     * Sets the radius of the cylinder
     * 
     * @param radius 
     */
    public void setRadius(Vector2D radius) {
        this.radius = radius.add(0.5, 0.5);
    }

    /**
     * Extends the radius to be at least the given radius
     * 
     * @param minRadius
     */
    public void extendRadius(Vector2D minRadius) {
        setRadius(Vector2D.getMaximum(minRadius, getRadius()));
    }

    /**
     * Set the minimum Y.
     * 
     * @param y
     */
    public void setMinimumY(int y) {
        hasY = true;
        minY = y;
    }

    /**
     * Se the maximum Y.
     * 
     * @param y
     */
    public void setMaximumY(int y) {
        hasY = true;
        maxY = y;
    }

    /**
     * Get the lower point of a region.
     * 
     * @return min. point
     */
    public Vector getMinimumPoint() {
        return center2D.subtract(getRadius()).toVector(minY);
    }

    /**
     * Get the upper point of a region.
     * 
     * @return max. point
     */
    public Vector getMaximumPoint() {
        return center2D.add(getRadius()).toVector(maxY);
    }

    /**
     * Gets the maximum Y value
     * @return 
     */
    public int getMaximumY() {
        return maxY;
    }

    /**
     * Gets the minimum Y value
     * @return 
     */
    public int getMinimumY() {
        return minY;
    }

    /**
     * Get the number of blocks in the region.
     * 
     * @return number of blocks
     */
    public int getArea() {
        return (int) Math.floor(radius.getX() * radius.getZ() * Math.PI * getHeight());
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
        return maxY - minY + 1;
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
     * Expand the region.
     *
     * @param change
     */
    public void expand(Vector change) throws RegionOperationException {
        if (change.getBlockX() != 0 || change.getBlockZ() != 0) {
            throw new RegionOperationException("Cylinders can only be expanded vertically.");
        }

        int changeY = change.getBlockY();
        if (changeY > 0) {
            maxY += changeY;
        } else {
            minY += changeY;
        }
    }

    /**
     * Contract the region.
     *
     * @param change
     */
    public void contract(Vector change) throws RegionOperationException {
        if (change.getBlockX() != 0 || change.getBlockZ() != 0) {
            throw new RegionOperationException("Cylinders can only be expanded vertically.");
        }

        int changeY = change.getBlockY();
        if (changeY > 0) {
            minY += changeY;
        } else {
            maxY += changeY;
        }
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        setCenter(getCenter().add(change));

        int changeY = change.getBlockY();
        maxY += changeY;
        minY += changeY;
    }

    /**
     * Checks to see if a point is inside this region.
     */
    public boolean contains(Vector pt) {
        final int blockY = pt.getBlockY();
        if (blockY < minY || blockY > maxY) {
            return false;
        }

        return pt.toVector2D().subtract(center2D).divide(radius).lengthSq() <= 1;
    }

    /**
     * Get a list of chunks.
     * 
     * @return
     */
    public Set<Vector2D> getChunks() {
        Set<Vector2D> chunks = new HashSet<Vector2D>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                Vector pt = new Vector(x, minY, z);
                if (contains(pt)) {
                    chunks.add(ChunkStore.toChunk(pt));
                }
            }
        }

        return chunks;
    }

    /**
     * Sets the height of the cylinder to fit the specified Y.
     * 
     * @param y 
     * @return true if the area was expanded
     */
    public boolean setY(int y) {
        if (!hasY) {
            minY = y;
            maxY = y;
            hasY = true;
            return true;
        } else if (y < minY) {
            minY = y;
            return true;
        } else if (y > maxY) {
            maxY = y;
            return true;
        }

        return false;
    }

    /**
     * Returns string representation in the format
     * "(centerX, centerY, centerZ) - (radiusX, radiusZ)"
     * 
     * @return string
     */
    @Override
    public String toString() {
        return center + " - " + radius;
    }
}
