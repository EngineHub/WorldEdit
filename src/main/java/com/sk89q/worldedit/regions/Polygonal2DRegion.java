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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

/**
 * Represents a 2D polygonal region.
 *
 * @author sk89q
 */
public class Polygonal2DRegion extends AbstractRegion implements FlatRegion {
    private List<BlockVector2D> points;
    private Vector2D min;
    private Vector2D max;
    private int minY;
    private int maxY;
    private boolean hasY = false;

    /**
     * Construct the region
     */
    public Polygonal2DRegion() {
        this((LocalWorld) null);
    }

    /**
     * Construct the region.
     *
     * @param world
     */
    public Polygonal2DRegion(LocalWorld world) {
        this(world, Collections.<BlockVector2D>emptyList(), 0, 0);
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
    public Polygonal2DRegion(LocalWorld world, List<BlockVector2D> points, int minY, int maxY) {
        super(world);
        this.points = new ArrayList<BlockVector2D>(points);
        this.minY = minY;
        this.maxY = maxY;
        hasY = true;
        recalculate();
    }

    public Polygonal2DRegion(Polygonal2DRegion region) {
        this(region.world, region.points, region.minY, region.maxY);
        hasY = region.hasY;
    }

    /**
     * Get the list of points.
     *
     * @return
     */
    public List<BlockVector2D> getPoints() {
        return Collections.unmodifiableList(points);
    }

    /**
     * Recalculate the bounding box of this polygonal region. This should be
     * called after points have been changed.
     */
    protected void recalculate() {
        if (points.size() == 0) {
            min = new Vector2D(0, 0);
            minY = 0;
            max = new Vector2D(0, 0);
            maxY = 0;
            return;
        }

        int minX = points.get(0).getBlockX();
        int minZ = points.get(0).getBlockZ();
        int maxX = points.get(0).getBlockX();
        int maxZ = points.get(0).getBlockZ();

        for (BlockVector2D v : points) {
            int x = v.getBlockX();
            int z = v.getBlockZ();
            if (x < minX) minX = x;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (z > maxZ) maxZ = z;
        }

        int oldMinY = minY;
        int oldMaxY = maxY;
        minY = Math.min(oldMinY, oldMaxY);
        maxY = Math.max(oldMinY, oldMaxY);

        minY = Math.min(Math.max(0, minY), world == null ? 255 : world.getMaxY());
        maxY = Math.min(Math.max(0, maxY), world == null ? 255 : world.getMaxY());

        min = new Vector2D(minX, minZ);
        max = new Vector2D(maxX, maxZ);
    }

    /**
     * Add a point to the list.
     *
     * @param pt
     */
    public void addPoint(Vector2D pt) {
        points.add(pt.toBlockVector2D());
        recalculate();
    }

    /**
     * Add a point to the list.
     *
     * @param pt
     */
    public void addPoint(BlockVector2D pt) {
        points.add(pt);
        recalculate();
    }

    /**
     * Add a point to the list.
     *
     * @param pt
     */
    public void addPoint(Vector pt) {
        points.add(new BlockVector2D(pt.getBlockX(), pt.getBlockZ()));
        recalculate();
    }

    /**
     * Get the minimum Y.
     *
     * @return min y
     */
    public int getMinimumY() {
        return minY;
    }

    @Deprecated
    public int getMininumY() {
        return minY;
    }

    /**
     * Set the minimum Y.
     *
     * @param y
     */
    public void setMinimumY(int y) {
        hasY = true;
        minY = y;
        recalculate();
    }

    /**
     * Get the maximum Y.
     *
     * @return max y
     */
    public int getMaximumY() {
        return maxY;
    }

    /**
     * Set the maximum Y.
     *
     * @param y
     */
    public void setMaximumY(int y) {
        hasY = true;
        maxY = y;
        recalculate();
    }

    /**
     * Get the lower point of a region.
     *
     * @return min. point
     */
    public Vector getMinimumPoint() {
        return min.toVector(minY);
    }

    /**
     * Get the upper point of a region.
     *
     * @return max. point
     */
    public Vector getMaximumPoint() {
        return max.toVector(maxY);
    }

    /**
     * Get the number of blocks in the region.
     *
     * @return number of blocks
     */
    public int getArea() {
        double area = 0;
        int i, j = points.size() - 1;

        for (i = 0; i < points.size(); ++i) {
            area += (points.get(j).getBlockX() + points.get(i).getBlockX())
                    * (points.get(j).getBlockZ() - points.get(i).getBlockZ());
            j = i;
        }

        return (int) Math.floor(Math.abs(area * 0.5)
                * (maxY - minY + 1));
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth() {
        return max.getBlockX() - min.getBlockX() + 1;
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
        return max.getBlockZ() - min.getBlockZ() + 1;
    }

    /**
     * Expand the region.
     *
     * @param changes
     */
    public void expand(Vector... changes) throws RegionOperationException {
        for (Vector change : changes) {
            if (change.getBlockX() != 0 || change.getBlockZ() != 0) {
                throw new RegionOperationException("Polygons can only be expanded vertically.");
            }
        }

        for (Vector change : changes) {
            int changeY = change.getBlockY();
            if (changeY > 0) {
                maxY += changeY;
            } else {
                minY += changeY;
            }
        }
        recalculate();
    }

    /**
     * Contract the region.
     *
     * @param changes
     */
    public void contract(Vector... changes) throws RegionOperationException {
        for (Vector change : changes) {
            if (change.getBlockX() != 0 || change.getBlockZ() != 0) {
                throw new RegionOperationException("Polygons can only be contracted vertically.");
            }
        }

        for (Vector change : changes) {
            int changeY = change.getBlockY();
            if (changeY > 0) {
                minY += changeY;
            } else {
                maxY += changeY;
            }
        }
        recalculate();
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        final double changeX = change.getX();
        final double changeY = change.getY();
        final double changeZ = change.getZ();

        for (int i = 0; i < points.size(); ++i) {
            BlockVector2D point = points.get(i);
            points.set(i, new BlockVector2D(point.getX() + changeX, point.getZ() + changeZ));
        }

        minY += changeY;
        maxY += changeY;

        recalculate();
    }

    /**
     * Checks to see if a point is inside this region.
     */
    public boolean contains(Vector pt) {
        return contains(points, minY, maxY, pt);
    }

    /**
     * Checks to see if a point is inside a region.
     *
     * @param points
     * @param minY
     * @param maxY
     * @param pt
     * @return
     */
    public static boolean contains(List<BlockVector2D> points, int minY,
            int maxY, Vector pt) {
        if (points.size() < 3) {
            return false;
        }
        int targetX = pt.getBlockX(); //wide
        int targetY = pt.getBlockY(); //height
        int targetZ = pt.getBlockZ(); //depth

        if (targetY < minY || targetY > maxY) {
            return false;
        }

        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        int i;

        xOld = points.get(npoints - 1).getBlockX();
        zOld = points.get(npoints - 1).getBlockZ();

        for (i = 0; i < npoints; ++i) {
            xNew = points.get(i).getBlockX();
            zNew = points.get(i).getBlockZ();
            //Check for corner
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (x1 <= targetX && targetX <= x2) {
                crossproduct = ((long) targetZ - (long) z1) * (long) (x2 - x1)
                        - ((long) z2 - (long) z1) * (long) (targetX - x1);
                if (crossproduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2)) return true; //on edge
                } else if (crossproduct < 0 && (x1 != targetX)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }

    /**
     * Return the number of points.
     *
     * @return
     */
    public int size() {
        return points.size();
    }

    /**
     * Expand the height of the polygon to fit the specified Y.
     *
     * @param y
     * @return true if the area was expanded
     */
    public boolean expandY(int y) {
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
     * Get the iterator.
     *
     * @return iterator of points inside the region
     */
    @Override
    public Iterator<BlockVector> iterator() {
        return new FlatRegion3DIterator(this);
    }

    @Override
    public Iterable<Vector2D> asFlatRegion() {
        return new Iterable<Vector2D>() {
            @Override
            public Iterator<Vector2D> iterator() {
                return new FlatRegionIterator(Polygonal2DRegion.this);
            }
        };
    }

    /**
     * Returns string representation in the format
     * "(x1, z1) - ... - (xN, zN) * (minY - maxY)"
     *
     * @return string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<BlockVector2D> pts = getPoints();
        Iterator<BlockVector2D> it = pts.iterator();
        while (it.hasNext()) {
            BlockVector2D current = it.next();
            sb.append("(" + current.getBlockX() + ", " + current.getBlockZ() + ")");
            if (it.hasNext()) sb.append(" - ");
        }
        sb.append(" * (" + minY + " - " + maxY + ")");
        return sb.toString();
    }

    public Polygonal2DRegion clone() {
        Polygonal2DRegion clone = (Polygonal2DRegion) super.clone();
        clone.points = new ArrayList<BlockVector2D>(points);
        return clone; 
    }

    @Override
    public List<BlockVector2D> polygonize(int maxPoints) {
        if (maxPoints >= 0 && maxPoints < points.size()) {
            throw new IllegalArgumentException("Cannot polygonize a this Polygonal2DRegion into the amount of points given.");
        }

        return points;
    }
}
