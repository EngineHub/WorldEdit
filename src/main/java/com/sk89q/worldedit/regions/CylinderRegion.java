/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.iterator.FlatRegion3DIterator;
import com.sk89q.worldedit.regions.iterator.FlatRegionIterator;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a cylindrical region.
 *
 * @author yetanotherx
 */
public class CylinderRegion extends AbstractRegion implements FlatRegion {
    private Vector2D center;
    private Vector2D radius;
    private int minY;
    private int maxY;
    private boolean hasY = false;

    /**
     * Construct the region
     */
    public CylinderRegion() {
        this((World) null);
    }

    @Deprecated
    public CylinderRegion(LocalWorld world) {
        this((World) world);
    }
    /**
     * Construct the region.
     *
     * @param world
     */
    public CylinderRegion(World world) {
        this(world, new Vector(), new Vector2D(), 0, 0);
        hasY = false;
    }

    @Deprecated
    public CylinderRegion(LocalWorld world, Vector center, Vector2D radius, int minY, int maxY) {
        this((World) world, center, radius, minY, maxY);
    }

    /**
     * Construct the region.
     *
     * @param world
     * @param center
     * @param radius
     * @param minY
     * @param maxY
     */
    public CylinderRegion(World world, Vector center, Vector2D radius, int minY, int maxY) {
        super(world);
        setCenter(center.toVector2D());
        setRadius(radius);
        this.minY = minY;
        this.maxY = maxY;
        hasY = true;
    }

    public CylinderRegion(CylinderRegion region) {
        this(region.world, region.getCenter(), region.getRadius(), region.minY, region.maxY);
        hasY = region.hasY;
    }

    /**
     * Returns the main center point of the cylinder
     *
     * @return
     */
    @Override
    public Vector getCenter() {
        return center.toVector((maxY + minY) / 2);
    }

    /**
     * Sets the main center point of the region
     *
     * @deprecated replaced by {@link #setCenter(Vector2D)}
     */
    @Deprecated
    public void setCenter(Vector center) {
        setCenter(center.toVector2D());
    }

    /**
     * Sets the main center point of the region
     *
     * @param center
     */
    public void setCenter(Vector2D center) {
        this.center = center;
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
        return center.subtract(getRadius()).toVector(minY);
    }

    /**
     * Get the upper point of a region.
     *
     * @return max. point
     */
    public Vector getMaximumPoint() {
        return center.add(getRadius()).toVector(maxY);
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

    private Vector2D calculateDiff2D(Vector... changes) throws RegionOperationException {
        Vector2D diff = new Vector2D();
        for (Vector change : changes) {
            diff = diff.add(change.toVector2D());
        }

        if ((diff.getBlockX() & 1) + (diff.getBlockZ() & 1) != 0) {
            throw new RegionOperationException("Cylinders changes must be even for each horizontal dimensions.");
        }

        return diff.divide(2).floor();
    }

    private Vector2D calculateChanges2D(Vector... changes) {
        Vector2D total = new Vector2D();
        for (Vector change : changes) {
            total = total.add(change.toVector2D().positive());
        }

        return total.divide(2).floor();
    }

    /**
     * Expand the region.
     * Expand the region.
     *
     * @param changes array/arguments with multiple related changes
     * @throws RegionOperationException
     */
    public void expand(Vector... changes) throws RegionOperationException {
        center = center.add(calculateDiff2D(changes));
        radius = radius.add(calculateChanges2D(changes));
        for (Vector change : changes) {
            int changeY = change.getBlockY();
            if (changeY > 0) {
                maxY += changeY;
            } else {
                minY += changeY;
            }
        }
    }

    /**
     * Contract the region.
     *
     * @param changes array/arguments with multiple related changes
     * @throws RegionOperationException
     */
    public void contract(Vector... changes) throws RegionOperationException {
        center = center.subtract(calculateDiff2D(changes));
        Vector2D newRadius = radius.subtract(calculateChanges2D(changes));
        radius = Vector2D.getMaximum(new Vector2D(1.5, 1.5), newRadius);
        for (Vector change : changes) {
            int height = maxY - minY;
            int changeY = change.getBlockY();
            if (changeY > 0) {
                minY += Math.min(height, changeY);
            } else {
                maxY += Math.max(-height, changeY);
            }
        }
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        center = center.add(change.toVector2D());

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

        return pt.toVector2D().subtract(center).divide(radius).lengthSq() <= 1;
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

    @Override
    public Iterator<BlockVector> iterator() {
        return new FlatRegion3DIterator(this);
    }

    @Override
    public Iterable<Vector2D> asFlatRegion() {
        return new Iterable<Vector2D>() {
            @Override
            public Iterator<Vector2D> iterator() {
                return new FlatRegionIterator(CylinderRegion.this);
            }
        };
    }

    /**
     * Returns string representation in the format
     * "(centerX, centerZ) - (radiusX, radiusZ) - (minY, maxY)"
     *
     * @return string
     */
    @Override
    public String toString() {
        return center + " - " + radius + "(" + minY + ", " + maxY + ")";
    }

    public CylinderRegion clone() {
        return (CylinderRegion) super.clone();
    }

    @Override
    public List<BlockVector2D> polygonize(int maxPoints) {
        final Vector2D radius = getRadius();
        int nPoints = (int) Math.ceil(Math.PI*radius.length());

        // These strange semantics for maxPoints are copied from the selectSecondary method.
        if (maxPoints >= 0 && nPoints >= maxPoints) {
            nPoints = maxPoints - 1;
        }

        final List<BlockVector2D> points = new ArrayList<BlockVector2D>(nPoints);
        for (int i = 0; i < nPoints; ++i) {
            double angle = i * (2.0 * Math.PI) / nPoints;
            final Vector2D pos = new Vector2D(Math.cos(angle), Math.sin(angle));
            final BlockVector2D blockVector2D = pos.multiply(radius).add(center).toBlockVector2D();
            points.add(blockVector2D);
        }

        return points;
    }
}
