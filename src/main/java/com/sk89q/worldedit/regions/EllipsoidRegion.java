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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.storage.ChunkStore;
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
     * Stores the radii plus 0.5 on each axis.
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

    @Deprecated
    public EllipsoidRegion(LocalWorld world, Vector center, Vector radius) {
        this((World) world, center, radius);
    }

    /**
     * Construct a new instance of this ellipsoid region.
     *
     * @param world
     * @param center
     * @param radius
     */
    public EllipsoidRegion(World world, Vector center, Vector radius) {
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

    /**
     * Expand the region.
     *
     * @param changes array/arguments with multiple related changes
     * @throws RegionOperationException
     */
    public void expand(Vector... changes) throws RegionOperationException {
        center = center.add(calculateDiff(changes));
        radius = radius.add(calculateChanges(changes));
    }

    /**
     * Contract the region.
     *
     * @param changes array/arguments with multiple related changes
     * @throws RegionOperationException
     */
    public void contract(Vector... changes) throws RegionOperationException {
        center = center.subtract(calculateDiff(changes));
        Vector newRadius = radius.subtract(calculateChanges(changes));
        radius = Vector.getMaximum(new Vector(1.5, 1.5, 1.5), newRadius);
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
    @Override
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
     * @param radius
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
        final Set<Vector2D> chunks = new HashSet<Vector2D>();

        final Vector min = getMinimumPoint();
        final Vector max = getMaximumPoint();
        final int centerY = getCenter().getBlockY();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                if (!contains(new BlockVector(x, centerY, z))) {
                    continue;
                }

                chunks.add(new BlockVector2D(
                    x >> ChunkStore.CHUNK_SHIFTS,
                    z >> ChunkStore.CHUNK_SHIFTS
                ));
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

    public EllipsoidRegion clone() {
        return (EllipsoidRegion) super.clone();
    }
}
