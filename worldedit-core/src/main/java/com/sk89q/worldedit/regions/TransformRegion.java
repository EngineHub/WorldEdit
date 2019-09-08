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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Transforms another region according to a provided vector {@code Transform}.
 *
 * @see Transform
 */
public class TransformRegion extends AbstractRegion {

    private final Region region;
    private Transform transform = new Identity();

    /**
     * Create a new instance.
     *
     * @param region the region
     * @param transform the transform
     */
    public TransformRegion(Region region, Transform transform) {
        this(null, region, transform);
    }

    /**
     * Create a new instance.
     *
     * @param world the world, which may be null
     * @param region the region
     * @param transform the transform
     */
    public TransformRegion(@Nullable World world, Region region, Transform transform) {
        super(world);
        checkNotNull(region);
        checkNotNull(transform);
        this.region = region;
        this.transform = transform;
    }

    /**
     * Get the untransformed, base region.
     *
     * @return the base region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Get the transform that is applied.
     *
     * @return the transform
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Set the transform that is applied.
     *
     * @param transform the transform
     */
    public void setTransform(Transform transform) {
        checkNotNull(transform);
        this.transform = transform;
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return transform.apply(region.getMinimumPoint().toVector3()).toBlockPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return transform.apply(region.getMaximumPoint().toVector3()).toBlockPoint();
    }

    @Override
    public Vector3 getCenter() {
        return transform.apply(region.getCenter());
    }

    @Override
    public int getArea() {
        return region.getArea(); // Cannot transform this
    }

    @Override
    public int getWidth() {
        return getMaximumPoint().subtract(getMinimumPoint()).getBlockX() + 1;
    }

    @Override
    public int getHeight() {
        return getMaximumPoint().subtract(getMinimumPoint()).getBlockY() + 1;
    }

    @Override
    public int getLength() {
        return getMaximumPoint().subtract(getMinimumPoint()).getBlockZ() + 1;
    }

    @Override
    public void expand(BlockVector3... changes) throws RegionOperationException {
        throw new RegionOperationException("Can't expand a TransformedRegion");
    }

    @Override
    public void contract(BlockVector3... changes) throws RegionOperationException {
        throw new RegionOperationException("Can't contract a TransformedRegion");
    }

    @Override
    public void shift(BlockVector3 change) throws RegionOperationException {
        throw new RegionOperationException("Can't change a TransformedRegion");
    }

    @Override
    public boolean contains(BlockVector3 position) {
        return region.contains(transform.inverse().apply(position.toVector3()).toBlockPoint());
    }

    @Override
    public List<BlockVector2> polygonize(int maxPoints) {
        List<BlockVector2> origPoints = region.polygonize(maxPoints);
        List<BlockVector2> transformedPoints = new ArrayList<>();
        for (BlockVector2 vector : origPoints) {
            transformedPoints.add(transform.apply(vector.toVector3(0)).toVector2().toBlockPoint());
        }
        return transformedPoints;
    }

    @Override
    public Iterator<BlockVector3> iterator() {
        final Iterator<BlockVector3> it = region.iterator();

        return new Iterator<BlockVector3>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public BlockVector3 next() {
                BlockVector3 next = it.next();
                if (next != null) {
                    return transform.apply(next.toVector3()).toBlockPoint();
                } else {
                    return null;
                }
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }
}
