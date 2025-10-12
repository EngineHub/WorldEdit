/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.iterator.FlatRegion3DIterator;
import com.sk89q.worldedit.regions.iterator.FlatRegionIterator;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a 2D polygonal region.
 */
public class Polygonal2DRegion extends AbstractRegion implements FlatRegion {

    private List<BlockVector2> points;
    private BlockVector2 min;
    private BlockVector2 max;
    private int minY;
    private int maxY;
    private boolean hasY = false;

    /**
     * Construct the region.
     */
    public Polygonal2DRegion() {
        this((World) null);
    }

    /**
     * Construct the region.
     *
     * @param world the world
     */
    public Polygonal2DRegion(World world) {
        this(world, Collections.emptyList(), 0, 0);
        hasY = false;
    }

    /**
     * Construct the region.
     *
     * @param world the world
     * @param points list of points
     * @param minY minimum Y
     * @param maxY maximum Y
     */
    public Polygonal2DRegion(World world, List<BlockVector2> points, int minY, int maxY) {
        super(world);
        this.points = new ArrayList<>(points);
        this.minY = minY;
        this.maxY = maxY;
        hasY = true;
        recalculate();
    }

    /**
     * Make a copy of another region.
     *
     * @param region the other region
     */
    public Polygonal2DRegion(Polygonal2DRegion region) {
        this(region.world, region.points, region.minY, region.maxY);
        hasY = region.hasY;
    }

    /**
     * Get the list of points.
     *
     * @return a list of points
     */
    public List<BlockVector2> getPoints() {
        return Collections.unmodifiableList(points);
    }

    /**
     * Recalculate the bounding box of this polygonal region. This should be
     * called after points have been changed.
     */
    protected void recalculate() {
        if (points.isEmpty()) {
            min = BlockVector2.ZERO;
            minY = 0;
            max = BlockVector2.ZERO;
            maxY = 0;
            return;
        }

        int minX = points.get(0).x();
        int minZ = points.get(0).z();
        int maxX = points.get(0).x();
        int maxZ = points.get(0).z();

        for (BlockVector2 v : points) {
            int x = v.x();
            int z = v.z();
            if (x < minX) {
                minX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }

        int oldMinY = minY;
        int oldMaxY = maxY;
        minY = Math.min(oldMinY, oldMaxY);
        maxY = Math.max(oldMinY, oldMaxY);

        minY = Math.min(Math.max(getWorldMinY(), minY), getWorldMaxY());
        maxY = Math.min(Math.max(getWorldMinY(), maxY), getWorldMaxY());

        min = BlockVector2.at(minX, minZ);
        max = BlockVector2.at(maxX, maxZ);
    }

    /**
     * Add a point to the list.
     *
     * @param position the position
     */
    public void addPoint(BlockVector2 position) {
        points.add(position);
        recalculate();
    }

    /**
     * Add a point to the list.
     *
     * @param position the position
     */
    public void addPoint(BlockVector3 position) {
        points.add(BlockVector2.at(position.x(), position.z()));
        recalculate();
    }

    @Override
    public int getMinimumY() {
        return minY;
    }

    /**
     * Set the minimum Y.
     *
     * @param y the Y
     */
    public void setMinimumY(int y) {
        hasY = true;
        minY = y;
        recalculate();
    }

    @Override
    public int getMaximumY() {
        return maxY;
    }

    /**
     * Set the maximum Y.
     *
     * @param y the Y
     */
    public void setMaximumY(int y) {
        hasY = true;
        maxY = y;
        recalculate();
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return min.toBlockVector3(minY);
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return max.toBlockVector3(maxY);
    }

    @Override
    public long getVolume() {
        if (points.size() <= 2) {
            return 0;
        }

        List<BlockVector2> reverseOrderPoints = new ArrayList<>(points);
        Collections.reverse(reverseOrderPoints);
        long area = Math.max(getAreaClockwise(points), getAreaClockwise(reverseOrderPoints));

        return area * (maxY - minY + 1);
    }

    private long getAreaClockwise(List<BlockVector2> points) {
        int n = points.size();

        int[] previousDirections = new int[n];
        int[] followingDirections = new int[n];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < n; j++) {
                previousDirections[j] = getDirection(points, j);
                if (previousDirections[j] == 0) {
                    previousDirections[j] = previousDirections[(j - 1 + n) % n];
                }

                followingDirections[n - 1 - j] = getDirection(points, n - 1 - j);
                if (followingDirections[n - 1 - j] == 0) {
                    followingDirections[n - 1 - j] = followingDirections[(n - j) % n];
                }
            }
        }

        long area = 0;

        int minX = points.stream().mapToInt(BlockVector2::x).min().orElse(0);
        int j = n - 1;
        int prevIsNewDirection = isNewDirection(points, j - 1, previousDirections, followingDirections);
        for (int i = 0; i < n; i++) {
            int x1 = points.get(j).x() - minX;
            int z1 = points.get(j).z();
            int x2 = points.get(i).x() - minX;
            int z2 = points.get(i).z();
            int isNewDirectionValue = isNewDirection(points, j, previousDirections, followingDirections);
            area += areaInPoints(x1, z1, x2, z2, isNewDirectionValue, prevIsNewDirection);

            prevIsNewDirection = isNewDirectionValue;
            j = i;
        }

        return area;
    }

    private int getDirection(List<BlockVector2> points, int i) {
        int z1 = points.get(i).z();
        int z2 = points.get((i + 1) % points.size()).z();
        return z1 > z2 ? 1 : z1 == z2 ? 0 : -1;
    }

    private int isNewDirection(List<BlockVector2> points, int i, int[] previousDirections, int[] followingDirections) {
        int n = points.size();

        int x = points.get(i % n).x();
        int z = points.get(i % n).z();
        int x1 = points.get((i + 1) % n).x();
        int x2 = points.get((i - 1 + n) % n).x();
        int z1 = points.get((i + 1) % n).z();
        int z2 = points.get((i - 1 + n) % n).z();
        int previousEdgeOrientation = crossProduct(x1, z1, x, z, x2, z2);

        x = points.get((i + 1) % n).x();
        z = points.get((i + 1) % n).z();
        x1 = points.get(i % n).x();
        x2 = points.get((i + 2) % n).x();
        z1 = points.get(i % n).z();
        z2 = points.get((i + 2) % n).z();
        int nextEdgeOrientation = crossProduct(x1, z1, x, z, x2, z2);

        int direction = getDirection(points, i);
        if (direction == -1) {
            return direction != followingDirections[(i + 1) % n] && nextEdgeOrientation > 0 ? 1 : 0;
        }

        if (direction == 1) {
            return direction != previousDirections[(i - 1 + n) % n] && previousEdgeOrientation < 0 ? 1 : 0;
        }

        boolean prevCondition = previousDirections[(i - 1 + n) % n] == 1 && previousEdgeOrientation >= 0;
        boolean follCondition = followingDirections[(i + 1) % n] == -1 && nextEdgeOrientation <= 0;

        return prevCondition && follCondition ? 2 : prevCondition || follCondition ? 1 : 0;
    }

    private int crossProduct(int x1, int y1, int x, int y, int x2, int y2) {
        return (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
    }

    public long areaInPoints(int x1, int z1, int x2, int z2, int isNewDirectionValue, int prevIsNewDirectionValue) {
        if (z1 == z2) {
            if (isNewDirectionValue == 2 && isNewDirectionValue != prevIsNewDirectionValue) {
                return Math.abs(x1 - x2) - 1;
            }

            return isNewDirectionValue != 0 ? Math.abs(x1 - x2) : 0;
        }

        boolean isNewDirection = isNewDirectionValue == 1;
        boolean isIncreasing = z1 > z2;
        long area = 0;

        int side = Math.min(x1, x2) + (isIncreasing ? 1 : 0);
        int height = isNewDirection ? next(z1 - z2) : z1 - z2;
        area += (long) side * height;

        if ((isIncreasing && (x1 < x2 || isNewDirection)) || (!isIncreasing && (x1 < x2 && !isNewDirection))) {
            area += Math.abs(x1 - x2);
        }

        int z = Math.abs(z1 - z2);
        int x = Math.abs(x1 - x2);
        int squaresInLine = x + z - gcd(x, z);
        if (!isIncreasing) {
            area -= (x * z - squaresInLine) / 2;
            area -= squaresInLine;
        } else {
            area += (x * z - squaresInLine) / 2;
        }

        return area;
    }

    private int next(int value) {
        return value < 0 ? value - 1 : value + 1;
    }

    private int gcd(int n1, int n2) {
        while (n2 != 0) {
            int temp = n2;
            n2 = n1 % n2;
            n1 = temp;
        }

        return n1;
    }

    @Override
    public int getWidth() {
        return max.x() - min.x() + 1;
    }

    @Override
    public int getHeight() {
        return maxY - minY + 1;
    }

    @Override
    public int getLength() {
        return max.z() - min.z() + 1;
    }

    @Override
    public void expand(BlockVector3... changes) throws RegionOperationException {
        for (BlockVector3 change : changes) {
            if (change.x() != 0 || change.z() != 0) {
                throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.polygon2d.error.expand-only-vertical"));
            }
            int changeY = change.y();
            if (changeY > 0) {
                maxY += changeY;
            } else {
                minY += changeY;
            }
        }
        recalculate();
    }

    @Override
    public void contract(BlockVector3... changes) throws RegionOperationException {
        for (BlockVector3 change : changes) {
            if (change.x() != 0 || change.z() != 0) {
                throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.polygon2d.error.contract-only-vertical"));
            }
            int changeY = change.y();
            if (changeY > 0) {
                minY += changeY;
            } else {
                maxY += changeY;
            }
        }
        recalculate();
    }

    @Override
    public void shift(BlockVector3 change) throws RegionOperationException {
        for (int i = 0; i < points.size(); ++i) {
            BlockVector2 point = points.get(i);
            points.set(i, BlockVector2.at(point.x() + change.x(), point.z() + change.z()));
        }

        minY += change.y();
        maxY += change.y();

        recalculate();
    }

    @Override
    public boolean contains(BlockVector3 position) {
        return contains(points, minY, maxY, position);
    }

    /**
     * Checks to see if a point is inside a region.
     *
     * @param points a list of points
     * @param minY the min Y
     * @param maxY the max Y
     * @param pt the position to check
     * @return true if the given polygon contains the given point
     */
    public static boolean contains(List<BlockVector2> points, int minY, int maxY, BlockVector3 pt) {
        if (points.size() < 3) {
            return false;
        }
        int targetX = pt.x(); //wide
        int targetY = pt.y(); //height
        int targetZ = pt.z(); //depth

        if (targetY < minY || targetY > maxY) {
            return false;
        }

        boolean inside = false;
        int npoints = points.size();
        int xNew;
        int zNew;
        int x1;
        int z1;
        int x2;
        int z2;
        long crossproduct;
        int i;

        int xOld = points.get(npoints - 1).x();
        int zOld = points.get(npoints - 1).z();

        for (i = 0; i < npoints; ++i) {
            xNew = points.get(i).x();
            zNew = points.get(i).z();
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
                    if ((z1 <= targetZ) == (targetZ <= z2)) {
                        return true; //on edge
                    }
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
     * @return the number of points
     */
    public int size() {
        return points.size();
    }

    /**
     * Expand the height of the polygon to fit the specified Y.
     *
     * @param y the amount to expand
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

    @Override
    public Iterator<BlockVector3> iterator() {
        return new FlatRegion3DIterator(this);
    }

    @Override
    public Iterable<BlockVector2> asFlatRegion() {
        return () -> new FlatRegionIterator(Polygonal2DRegion.this);
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
        List<BlockVector2> pts = getPoints();
        Iterator<BlockVector2> it = pts.iterator();
        while (it.hasNext()) {
            BlockVector2 current = it.next();
            sb.append("(").append(current.x()).append(", ").append(current.z()).append(")");
            if (it.hasNext()) {
                sb.append(" - ");
            }
        }
        sb.append(" * (").append(minY).append(" - ").append(maxY).append(")");
        return sb.toString();
    }

    @Override
    public Polygonal2DRegion clone() {
        Polygonal2DRegion clone = (Polygonal2DRegion) super.clone();
        clone.points = new ArrayList<>(points);
        return clone;
    }

    @Override
    public List<BlockVector2> polygonize(int maxPoints) {
        if (maxPoints >= 0 && maxPoints < points.size()) {
            throw new IllegalArgumentException("Cannot polygonize a this Polygonal2DRegion into the amount of points given.");
        }

        return points;
    }

}
