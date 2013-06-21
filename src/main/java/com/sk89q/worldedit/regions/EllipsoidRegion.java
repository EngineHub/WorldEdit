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

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;

/**
 * Represents an ellipsoid region.
 */
public class EllipsoidRegion extends AbstractRegion {
    
    private Vector center;
    private Vector radius;
    
    /**
     * Construct a new instance of this ellipsoid region.
     *
     * @param pos1 position 1
     * @param pos2 position 2
     */
    public EllipsoidRegion(Vector pos1, Vector pos2) {
        this(null, pos1, pos2);
    }

    /**
     * Construct a new instance of this ellipsoid region.
     *
     * @param world the world
     * @param center the center position
     * @param radius the radius
     */
    public EllipsoidRegion(LocalWorld world, Vector center, Vector radius) {
        super(world);
        this.center = center;
        setRadius(radius);
    }

    /**
     * Make a copy of an existing ellipsoid region.
     * 
     * @param region the existing region
     */
    public EllipsoidRegion(EllipsoidRegion region) {
        this(region.world, region.center, region.getRadius());
    }

    @Override
    public Vector getMinimumPoint() {
        return center.subtract(getRadius());
    }
    
    @Override
    public Vector getMaximumPoint() {
        return center.add(getRadius());
    }

    @Override
    public int getArea() {
        return (int) Math.floor((4.0 / 3.0) * Math.PI * radius.getX() * radius.getY() * radius.getZ());
    }

    @Override
    public int getWidth() {
        return (int) (2 * radius.getX());
    }

    @Override
    public int getHeight() {
        return (int) (2 * radius.getY());
    }

    @Override
    public int getLength() {
        return (int) (2 * radius.getZ());
    }

    private Vector calculateDiff(Vector... changes) throws RegionOperationException {
        Vector diff = new Vector().add(changes);

        if ((diff.getBlockX() & 1) + (diff.getBlockY() & 1) + (diff.getBlockZ() & 1) != 0) {
            throw new RegionOperationException(
                    "Ellipsoid changes must be even for each dimensions.");
        }

        return diff.divide(2).floor();
    }

    private Vector calculateChanges(Vector... changes) {
        Vector total = new Vector();
        for (Vector change : changes) {
            total = total.add(change.positive());
        }

        return total.divide(2).floor();
    }

    @Override
    public void expand(Vector... changes) throws RegionOperationException {
        center = center.add(calculateDiff(changes));
        radius = radius.add(calculateChanges(changes));
    }

    @Override
    public void contract(Vector... changes) throws RegionOperationException {
        center = center.subtract(calculateDiff(changes));
        Vector newRadius = radius.subtract(calculateChanges(changes));
        radius = Vector.getMaximum(new Vector(1.5, 1.5, 1.5), newRadius);
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        center = center.add(change);
    }
    
    @Override
    public Vector getCenter() {
        return center;
    }

    public void setCenter(Vector center) {
        this.center = center;
    }

    public Vector getRadius() {
        return radius.subtract(0.5, 0.5, 0.5);
    }

    public void setRadius(Vector radius) {
        this.radius = radius.add(0.5, 0.5, 0.5);
    }

    @Override
    public Set<Vector2D> getChunks() {
        Set<Vector2D> chunks = new HashSet<Vector2D>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    if (contains(new BlockVector(x, y, z))) {
                        chunks.add(new BlockVector2D(x >> ChunkStore.CHUNK_SHIFTS,
                                z >> ChunkStore.CHUNK_SHIFTS));
                    }
                }
            }
        }

        return chunks;
    }

    @Override
    public Set<Vector> getChunkCubes() {
        Set<Vector> chunks = new HashSet<Vector>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    if (contains(new BlockVector(x, y, z))) {
                        chunks.add(new BlockVector(x >> ChunkStore.CHUNK_SHIFTS,
                                y >> ChunkStore.CHUNK_SHIFTS, z >> ChunkStore.CHUNK_SHIFTS));
                    }
                }
            }
        }

        return chunks;
    }

    @Override
    public boolean contains(Vector pt) {
        return pt.subtract(center).divide(radius).lengthSq() <= 1;
    }

    @Override
    public String toString() {
        return center + " - " + getRadius();
    }

    public void extendRadius(Vector minRadius) {
        setRadius(Vector.getMaximum(minRadius, getRadius()));
    }

    @Override
    public EllipsoidRegion clone() {
        return (EllipsoidRegion) super.clone();
    }
}
