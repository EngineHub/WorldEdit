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

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author sk89q
 */
public class CuboidRegion implements Region {
    /**
     * Store the first point.
     */
    private Vector pos1;
    /**
     * Store the second point.
     */
    private Vector pos2;

    /**
     * Construct a new instance of this cuboid region.
     * 
     * @param pos1
     * @param pos2
     */
    public CuboidRegion(Vector pos1, Vector pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    public Vector getMinimumPoint() {
        return new Vector(Math.min(pos1.getX(), pos2.getX()),
                         Math.min(pos1.getY(), pos2.getY()),
                         Math.min(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    public Vector getMaximumPoint() {
        return new Vector(Math.max(pos1.getX(), pos2.getX()),
                         Math.max(pos1.getY(), pos2.getY()),
                         Math.max(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Get the number of blocks in the region.
     * 
     * @return number of blocks
     */
    public int getArea() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int)((max.getX() - min.getX() + 1) *
                     (max.getY() - min.getY() + 1) *
                     (max.getZ() - min.getZ() + 1));
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int)(max.getX() - min.getX() + 1);
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int)(max.getY() - min.getY() + 1);
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int)(max.getZ() - min.getZ() + 1);
    }

    /**
     * Expands the cuboid in a direction.
     *
     * @param change
     */
    public void expand(Vector change) {
        if (change.getX() > 0) {
            if (Math.max(pos1.getX(), pos2.getX()) == pos1.getX()) {
                pos1 = pos1.add(new Vector(change.getX(), 0, 0));
            } else {
                pos2 = pos2.add(new Vector(change.getX(), 0, 0));
            }
        } else {
            if (Math.min(pos1.getX(), pos2.getX()) == pos1.getX()) {
                pos1 = pos1.add(new Vector(change.getX(), 0, 0));
            } else {
                pos2 = pos2.add(new Vector(change.getX(), 0, 0));
            }
        }

        if (change.getY() > 0) {
            if (Math.max(pos1.getY(), pos2.getY()) == pos1.getY()) {
                pos1 = pos1.add(new Vector(0, change.getY(), 0));
            } else {
                pos2 = pos2.add(new Vector(0, change.getY(), 0));
            }
        } else {
            if (Math.min(pos1.getY(), pos2.getY()) == pos1.getY()) {
                pos1 = pos1.add(new Vector(0, change.getY(), 0));
            } else {
                pos2 = pos2.add(new Vector(0, change.getY(), 0));
            }
        }

        if (change.getZ() > 0) {
            if (Math.max(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                pos1 = pos1.add(new Vector(0, 0, change.getZ()));
            } else {
                pos2 = pos2.add(new Vector(0, 0, change.getZ()));
            }
        } else {
            if (Math.min(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                pos1 = pos1.add(new Vector(0, 0, change.getZ()));
            } else {
                pos2 = pos2.add(new Vector(0, 0, change.getZ()));
            }
        }

        pos1 = pos1.clampY(0, 127);
        pos2 = pos2.clampY(0, 127);
    }

    /**
     * Contracts the cuboid in a direction.
     *
     * @param change
     */
    public void contract(Vector change) {
        if (change.getX() < 0) {
            if (Math.max(pos1.getX(), pos2.getX()) == pos1.getX()) {
                pos1 = pos1.add(new Vector(change.getX(), 0, 0));
            } else {
                pos2 = pos2.add(new Vector(change.getX(), 0, 0));
            }
        } else {
            if (Math.min(pos1.getX(), pos2.getX()) == pos1.getX()) {
                pos1 = pos1.add(new Vector(change.getX(), 0, 0));
            } else {
                pos2 = pos2.add(new Vector(change.getX(), 0, 0));
            }
        }

        if (change.getY() < 0) {
            if (Math.max(pos1.getY(), pos2.getY()) == pos1.getY()) {
                pos1 = pos1.add(new Vector(0, change.getY(), 0));
            } else {
                pos2 = pos2.add(new Vector(0, change.getY(), 0));
            }
        } else {
            if (Math.min(pos1.getY(), pos2.getY()) == pos1.getY()) {
                pos1 = pos1.add(new Vector(0, change.getY(), 0));
            } else {
                pos2 = pos2.add(new Vector(0, change.getY(), 0));
            }
        }

        if (change.getZ() < 0) {
            if (Math.max(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                pos1 = pos1.add(new Vector(0, 0, change.getZ()));
            } else {
                pos2 = pos2.add(new Vector(0, 0, change.getZ()));
            }
        } else {
            if (Math.min(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                pos1 = pos1.add(new Vector(0, 0, change.getZ()));
            } else {
                pos2 = pos2.add(new Vector(0, 0, change.getZ()));
            }
        }

        pos1 = pos1.clampY(0, 127);
        pos2 = pos2.clampY(0, 127);
    }

    /**
     * Get position 1.
     * 
     * @return position 1
     */
    public Vector getPos1() {
        return pos1;
    }

    /**
     * Set position 1.
     * 
     * @param pos1
     */
    public void setPos1(Vector pos1) {
        this.pos1 = pos1;
    }

    /**
     * Get position 2.
     * 
     * @return position 2
     */
    public Vector getPos2() {
        return pos2;
    }

    /**
     * Set position 2.
     *
     * @param pos2
     */
    public void setPos2(Vector pos2) {
        this.pos2 = pos2;
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
        double x = pt.getX();
        double y = pt.getY();
        double z = pt.getZ();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();
        
        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }

    /**
     * Get the iterator.
     * 
     * @return iterator of points inside the region
     */
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            private Vector min = getMinimumPoint();
            private Vector max = getMaximumPoint();
            private int nextX = min.getBlockX();
            private int nextY = min.getBlockY();
            private int nextZ = min.getBlockZ();
            
            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }
            
            public BlockVector next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                BlockVector answer = new BlockVector(nextX, nextY, nextZ);
                if (++nextX > max.getBlockX()) {
                    nextX = min.getBlockX();
                    if (++nextY > max.getBlockY()) {
                        nextY = min.getBlockY();
                        if (++nextZ > max.getBlockZ()) {
                            nextX = Integer.MIN_VALUE;
                        }
                    }
                }
                return answer;
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    /**
     * Returns string representation in the format
     * "(minX, minY, minZ) - (maxX, maxY, maxZ)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return getMinimumPoint() + " - " + getMaximumPoint();
    }
}
