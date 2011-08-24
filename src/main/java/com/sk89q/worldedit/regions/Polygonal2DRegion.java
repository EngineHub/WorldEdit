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

package com.sk89q.worldedit.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;

/**
 * Represents a 2D polygonal region.
 *
 * @author sk89q
 */
public class Polygonal2DRegion implements Region {
    protected List<BlockVector2D> points;
    protected BlockVector min;
    protected BlockVector max;
    protected int minY;
    protected int maxY;
    protected boolean hasY = false;
    
    /**
     * Construct the region.
     */
    public Polygonal2DRegion() {
        points = new ArrayList<BlockVector2D>();
        minY = 0;
        maxY = 0;
        hasY = false;
        recalculate();
    }
    
    /**
     * Construct the region.
     * 
     * @param points
     * @param minY
     * @param maxY
     */
    public Polygonal2DRegion(List<BlockVector2D> points, int minY, int maxY) {
        this.points = points;
        this.minY = minY;
        this.maxY = maxY;
        hasY = true;
        recalculate();
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
            min = new BlockVector(0, 0, 0);
            max = new BlockVector(0, 0, 0);
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

        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
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
     * Se the maximum Y.
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
        return min;
    }

    /**
     * Get the upper point of a region.
     * 
     * @return max. point
     */
    public Vector getMaximumPoint() {
        return max;
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

        return (int)Math.floor(Math.abs(area * 0.5) 
                * (maxY - minY + 1));
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth() {
        return max.getBlockX() - min.getBlockX();
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight() {
        return max.getBlockY() - min.getBlockY();
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength() {
        return max.getBlockZ() - min.getBlockZ();
    }

    /**
     * Expand the region.
     *
     * @param change
     */
    public void expand(Vector change) throws RegionOperationException {
        if (change.getBlockX() != 0 || change.getBlockZ() != 0) {
            throw new RegionOperationException("Polygons can only be expanded vertically.");
        }
        
        int changeY = change.getBlockY();
        if (changeY > 0) {
            maxY += changeY;
        } else {
            minY += changeY;
        }
        recalculate();
    }

    /**
     * Contract the region.
     *
     * @param change
     */
    public void contract(Vector change) throws RegionOperationException {
        if (change.getBlockX() != 0 || change.getBlockZ() != 0) {
            throw new RegionOperationException("Polygons can only be contracted vertically.");
        }
        
        int changeY = change.getBlockY();
        if (changeY > 0) {
            minY += changeY;
        } else {
            maxY += changeY;
        }
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
     * Get a list of chunks.
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
                    if (contains(pt)) { // Not the best
                        chunks.add(ChunkStore.toChunk(pt));
                    }
                }
            }
        }

        return chunks;
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
    public Iterator<BlockVector> iterator() {
        return new Polygonal2DRegionIterator(this);
        
        /*
        Incomplete iterator. Where's my yield?!
        
        ArrayList<BlockVector> items = new ArrayList<BlockVector>();
        
        int nodes, pixelZ, i, j, swap;
        int n = points.size();
        int[] nodeX = new int[n];

        int minZ = getMinimumPoint().getBlockZ();
        int maxZ = getMaximumPoint().getBlockZ();
        
        for (pixelZ = minZ; pixelZ < maxZ; ++pixelZ) {
            // Build a list of nodes
            nodes = 0;
            j = n - 1;
            for (i = 0; i < n; ++i) {
                if (points.get(i).getBlockZ() < (double) pixelZ
                        && points.get(j).getBlockZ() >= (double) pixelZ
                        || points.get(j).getBlockZ() < (double) pixelZ
                        && points.get(i).getBlockZ() >= (double) pixelZ) {
                    nodeX[nodes++] = (int) (points.get(i).getBlockX()
                            + (pixelZ - points.get(i).getBlockZ())
                            / (points.get(j).getBlockZ() - points.get(i)
                                    .getBlockZ())
                            * (points.get(j).getBlockX() - points.get(i)
                                    .getBlockX()));
                }
                j = i;
            }

            // Sort the nodes, via a simple bubble sort
            i = 0;
            while (i < nodes - 1) {
                if (nodeX[i] > nodeX[i + 1]) {
                    swap = nodeX[i];
                    nodeX[i] = nodeX[i + 1];
                    nodeX[i + 1] = swap;
                    if (i > 0)
                        --i;
                } else {
                    ++i;
                }
            }

            // Fill the pixels between node pairs
            for (i = 0; i < nodes; i += 2) {
                for (j = nodeX[i]; j < nodeX[i + 1]; ++j) {
                    for (int y = minY; y >= maxY; ++y) {
                        items.add(new BlockVector(j, y, pixelZ));
                    }
                }
            }
        }
        
        return items.iterator();*/
    }

    /**
     * Returns string representation in the format
     * "(x1, z1) - ... - (xN, zN) * (minY - maxY)"
     * 
     * @return string
     */
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

    /**
     * A terrible polygonal region iterator.
     */
    public class Polygonal2DRegionIterator implements Iterator<BlockVector> {
        protected List<BlockVector2D> points = new ArrayList<BlockVector2D>();
        protected int minX;
        protected int minY;
        protected int minZ;
        protected int maxX;
        protected int maxY;
        protected int maxZ;
        protected int n;
        protected int i;
        protected int curX;
        protected int curZ;
        protected int curY;
        protected BlockVector next;
        
        public Polygonal2DRegionIterator(Polygonal2DRegion region) {
            points = new ArrayList<BlockVector2D>(region.points);
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();
            minX = min.getBlockX();
            minY = min.getBlockY();
            minZ = min.getBlockZ();
            maxX = max.getBlockX();
            maxY = max.getBlockY();
            maxZ = max.getBlockZ();
            n = (maxX - minX + 1) * (maxZ - minZ + 1);
            i = 0;
            curX = 0;
            curZ = 0;
            curY = minY;
            next = null;
            findNext();
        }
        
        private void findNext() {
            if (i >= n) {
                next = null;
                return;
            }
            
            if (next != null && curY <= maxY) {
                ++curY;
                next = new BlockVector(curX, curY, curZ);
                if (curY > maxY) {
                    ++i;
                    curY = minY;
                } else {
                    return;
                }
            } 
            
            while (i < n) {
                curZ = i / (maxX - minX + 1) + minZ;
                curX = (i % (maxX - minX + 1)) + minX;
                BlockVector pt = new BlockVector(curX, minY, curZ);
                if (contains(points, minY, maxY, pt)) {
                    next = pt;
                    return;
                }
                ++i;
            }
            
            next = null;
        }

        public boolean hasNext() {
            return next != null;
        }

        public BlockVector next() {
            BlockVector next = this.next;
            findNext();
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
