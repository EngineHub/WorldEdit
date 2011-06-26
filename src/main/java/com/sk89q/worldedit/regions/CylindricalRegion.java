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
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author Nichts
 */
public class CylindricalRegion implements Region {
    /**
     * Store the center point.
     */
    private Vector center;
    /**
     * Store the height (in blocks)
     */
    private int height;
    /**
     * Store the radius (in blocks)
     */
    private int radius;
    
    /**
     * Indicates weather or not the cylinder has changed
     * since the containing blocks were last calculated
     */
    private boolean changed;
    
    /**
     * number of blocks in this cylinder
     */
    private int area;
    
    /**
    * Array of sets containing all blocks of the bottom
    * cylinder
    */
    private ArrayList<HashSet<BlockVector>> base;

    /**
     * Construct a new instance of this cylindrical region.
     * 
     * @param center
     * @param outer
     */
    public CylindricalRegion(Vector center, Vector outer) {
        this.changed = true;
        this.center = new Vector(center);
        this.height = outer.getBlockY() - this.center.getBlockY();
        if(height < 0) {
            this.center.add(0, height, 0);
            height = -height;
        }
        this.radius = (int)Math.round(Math.sqrt(Math.pow(outer.getBlockX() - this.center.getBlockX(), 2) +
                                Math.pow(outer.getBlockZ() - this.center.getBlockZ(), 2)));
    }

    /**
     * Construct a new instance of this cylindrical region.
     * 
     * @param center
     * @param height
     * @param radius
     */
    public CylindricalRegion(Vector center, int radius, int height) {
        this.changed = true;
        if(height < 0) {
            this.center = new Vector(center.getX(), center.getY() + height, center.getZ());
        } else {
            this.center = center;
        }
        this.height = Math.abs(height);
        this.radius = Math.abs(radius);
    }
    
    /**
     * Get the center point of the cylinder.
     *
     * @return min point
     */
    public Vector getMinimumPoint() {
        return new Vector(center);
    }

    /**
     * Get the upper point of the cylinder.
     *
     * @return max point
     */
    public Vector getMaximumPoint() {
        return new Vector(center.getX(), center.getY() + height, center.getZ());
    }

    /**
     * Get the number of blocks in the region.
     * 
     * @return number of blocks
     */
    public int getArea() {
        if(changed) {
            calculateBase();
        }
        return area;
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth() {
        return 2*radius+1;
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight() {
       return height + 1;
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength() {
        return getWidth();
    }

    /**
     * Expands the cylinder in a direction.
     *
     * @param change
     */
    public void expand(Vector change) {
        changed = true;
        height += Math.abs(change.getBlockY());
        if(change.getX() < 0) {
            this.center.add(0, change.getX(), 0);
        }
        radius += Math.abs(Math.round(Math.pow(change.getBlockX(), 2) + Math.pow(change.getBlockZ(), 2)));
    }

    /**
     * Contracts the cylinder in a direction.
     *
     * @param change
     */
    public void contract(Vector change) {
        changed = true;
        height -= Math.abs(change.getBlockY());
        if(change.getX() > 0) {
            this.center.add(0, change.getX(), 0);
        }
        if(height < 0) {
            this.center.add(0, height, 0);
            height = -height;
        }
        radius -= Math.abs(Math.round(Math.pow(change.getBlockX(), 2) + Math.pow(change.getBlockZ(), 2)));
    }

    /**
     * Get center.
     * 
     * @param center
     */
    public Vector getCenter() {
        return new Vector(center); //would be very bad if someone would modify center so we just return a copy
    }

    /**
     * Set center.
     * 
     * @param center
     */
    public void setCenter(Vector center) {
        changed = true;
        this.center = center;
    }

    /**
     * Get a list of chunks that this region is within.
     * 
     * @return
     */
    public Set<Vector2D> getChunks() {
        Set<Vector2D> chunks = new HashSet<Vector2D>();
        Iterator<BlockVector> iter= baseIterator();
        while(iter.hasNext()) {
            chunks.add(ChunkStore.toChunk(iter.next()));
        }
        return chunks;
    }

    /**
     * Returns true based on whether the region contains the point,
     *
     * @param pt
     */
    public boolean contains(Vector pt) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        
        if ((y < center.getBlockY()) || (y > center.getBlockY() + height)) {
            return false;
        }

        Iterator<BlockVector> iter = fastBaseIterator();
        
        BlockVector2D v = new BlockVector2D(x, z);
        
        while(iter.hasNext()) {
            BlockVector next = iter.next();
            if(v.equals(new BlockVector2D(next.getBlockX(), next.getBlockY()))) {
                return true;
            }
        }
        
        return false;
    }

    private void calculateBase() {
        int error = 2-2*this.radius;
        int x = -this.radius;
        int z = 0;
        int count = 0;
        int radius = this.radius;
        int cx = this.center.getBlockX();
        int cy = this.center.getBlockY();
        int cz = this.center.getBlockZ();
        base = new ArrayList<HashSet<BlockVector>>(this.radius * 2 + 1);
        for(int i = 0; i <= this.radius * 2; i++) {
            base.add(new HashSet<BlockVector>());
        }
        do
        {
            for(int i = x; i <= -x; i++) {
                if(base.get(this.radius + z).add(new BlockVector(cx + i, cy, cz + z))) {
                    count++;
                }
                if(base.get(this.radius - z).add(new BlockVector(cx + i, cy, cz - z))) {
                    count++;
                }
            }
            radius = error;
            if(radius > x) {
                x++;
                error += x*2+1;
            }
            if(radius <= z) {
                z++;
                error += z*2+1;
            }
        } while (x <= 0);
        area = count * (height + 1);
        this.changed = false;
    }
    
    public Iterator<BlockVector> baseIterator() {
        if (this.changed) {
            calculateBase();
        }
        return new Iterator<BlockVector>() {
            Iterator<HashSet<BlockVector>> zIterator = base.iterator();
            Iterator<BlockVector> xIterator = zIterator.next().iterator(); //zIterator should never be empty
            public boolean hasNext() {
                return xIterator.hasNext() || zIterator.hasNext();
            }

            public BlockVector next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                if(xIterator.hasNext()) {
                    return new BlockVector(xIterator.next());
                }
                xIterator = zIterator.next().iterator();
                return new BlockVector(xIterator.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    private Iterator<BlockVector> fastBaseIterator() {
        if (this.changed) {
            calculateBase();
        }
        return new Iterator<BlockVector>() {
            Iterator<HashSet<BlockVector>> yIterator = base.iterator();
            Iterator<BlockVector> xIterator = yIterator.next().iterator(); //yIterator should never be empty
            public boolean hasNext() {
                return xIterator.hasNext() || yIterator.hasNext();
            }

            public BlockVector next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                if(xIterator.hasNext()) {
                    return xIterator.next();
                }
                xIterator = yIterator.next().iterator();
                return xIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    /**
     * Get the iterator.
     * 
     * @return iterator of points inside the region
     */
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            private Iterator<BlockVector> baseIterator = baseIterator();
            private int yoffset = 0;
            
            public boolean hasNext() {
                return !(yoffset == height && !baseIterator.hasNext());
            }
            
            public BlockVector next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                if (!baseIterator.hasNext()) {
                    yoffset++;
                    baseIterator = fastBaseIterator();
                }
                BlockVector next = baseIterator.next();
                return new BlockVector(next.getX(), next.getY() + yoffset, next.getZ());
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
