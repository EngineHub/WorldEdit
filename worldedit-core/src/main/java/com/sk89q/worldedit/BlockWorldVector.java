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

package com.sk89q.worldedit;

/**
 * @deprecated Replace all uses of {@link WorldVector}s with {@link Location}s
 */
@SuppressWarnings("deprecation")
@Deprecated
public class BlockWorldVector extends WorldVector {

    /**
     * Construct an instance from another instance.
     *
     * @param position the position to copy
     */
    public BlockWorldVector(WorldVector position) {
        super(position.getWorld(), position);
    }

    /**
     * Construct an instance from another instance.
     *
     * @param world the world
     * @param position the position to copy
     */
    public BlockWorldVector(LocalWorld world, Vector position) {
        super(world, position);
    }

    /**
     * Construct a new instance.
     *
     * @param world another instance
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public BlockWorldVector(WorldVector world, int x, int y, int z) {
        super(world.getWorld(), x, y, z);
    }

    /**
     * Construct a new instance.
     *
     * @param world another instance
     * @param v the other vector
     */
    public BlockWorldVector(WorldVector world, Vector v) {
        super(world.getWorld(), v.getX(), v.getY(), v.getZ());
    }

    /**
     * Construct a new instance.
     *
     * @param world a world
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public BlockWorldVector(LocalWorld world, int x, int y, int z) {
        super(world, x, y, z);
    }

    /**
     * Construct a new instance.
     *
     * @param world a world
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public BlockWorldVector(LocalWorld world, float x, float y, float z) {
        super(world, x, y, z);
    }

    /**
     * Construct a new instance.
     *
     * @param world a world
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public BlockWorldVector(LocalWorld world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) {
            return false;
        }
        Vector other = (Vector) obj;
        return (int) other.getX() == (int) this.x && (int) other.getY() == (int) this.y
                && (int) other.getZ() == (int) this.z;

    }

    @Override
    public int hashCode() {
        return (Integer.valueOf((int) x).hashCode() << 19) ^
                (Integer.valueOf((int) y).hashCode() << 12) ^
                Integer.valueOf((int) z).hashCode();
    }

}
