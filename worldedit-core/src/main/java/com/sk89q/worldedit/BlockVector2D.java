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
 * Extension of {@code Vector2D} that that compares with other instances
 * using integer components.
 */
public class BlockVector2D extends Vector2D {

    public static final BlockVector2D ZERO = new BlockVector2D(0, 0);
    public static final BlockVector2D UNIT_X = new BlockVector2D(1, 0);
    public static final BlockVector2D UNIT_Z = new BlockVector2D(0, 1);
    public static final BlockVector2D ONE = new BlockVector2D(1, 1);

    /**
     * Construct an instance from another instance.
     *
     * @param position the position to copy
     */
    public BlockVector2D(Vector2D position) {
        super(position);
    }

    /**
     * Construct a new instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public BlockVector2D(int x, int z) {
        super(x, z);
    }

    /**
     * Construct a new instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public BlockVector2D(float x, float z) {
        super(x, z);
    }

    /**
     * Construct a new instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public BlockVector2D(double x, double z) {
        super(x, z);
    }

    @Override
    public int hashCode() {
        return ((int) x << 16) ^ (int) z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        }

        Vector2D other = (Vector2D) obj;
        return (int) other.x == (int) this.x && (int) other.z == (int) this.z;

    }

    @Override
    public BlockVector2D toBlockVector2D() {
        return this;
    }

}
