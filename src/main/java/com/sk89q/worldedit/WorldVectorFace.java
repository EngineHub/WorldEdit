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
 * A WorldVector that emphasizes one side of the block
 */
public class WorldVectorFace extends WorldVector {
    /**
     * Represents the side.
     */
    private VectorFace face;

    /**
     * Construct the Vector object.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param face
     */
    public WorldVectorFace(LocalWorld world, double x, double y, double z, VectorFace face) {
        super(world, x, y, z);
        this.face = face;
    }

    /**
     * Construct the Vector object.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param face
     */
    public WorldVectorFace(LocalWorld world, int x, int y, int z, VectorFace face) {
        super(world, x, y, z);
        this.face = face;
    }

    /**
     * Construct the Vector object.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param face
     */
    public WorldVectorFace(LocalWorld world, float x, float y, float z, VectorFace face) {
        super(world, x, y, z);
        this.face = face;
    }

    /**
     * Construct the Vector object.
     *
     * @param world
     * @param pt
     * @param face
     */
    public WorldVectorFace(LocalWorld world, Vector pt, VectorFace face) {
        super(world, pt);
        this.face = face;
    }

    /**
     * Construct the Vector object.
     * 
     * @param world
     * @param face
     */
    public WorldVectorFace(LocalWorld world, VectorFace face) {
        super(world);
        this.face = face;
    }

    /**
     * Get the face.
     *
     * @return
     */
    public VectorFace getFace() {
        return face;
    }

    /**
     * Get the WorldVector adjacent to this WorldVectorFace.
     *
     * @return
     */
    public WorldVector getFaceVector() {
        return new WorldVector(getWorld(),
                               getBlockX() - face.getModX(),
                               getBlockY() - face.getModY(),
                               getBlockZ() - face.getModZ());
    }

    /**
     * Get a WorldVectorFace by comparing two vectors. Note that they need not be
     * adjacent, as only the directions, not distance, will be taken into account.
     *
     * @param world the world in which the resulting vector should lie
     * @param vector the original vector
     * @param face the direction in which the face should lie
     * @return
     */
    public static WorldVectorFace getWorldVectorFace(LocalWorld world, Vector vector, Vector face) {
        if (vector == null || face == null) return null;
        // check which direction the face is from the vector
        final int x1 = vector.getBlockX();
        final int y1 = vector.getBlockY();
        final int z1 = vector.getBlockZ();
        int modX = x1 - face.getBlockX();
        int modY = y1 - face.getBlockY();
        int modZ = z1 - face.getBlockZ();
        if (modX > 0) modX = 1;
        else if (modX < 0) modX = -1;
        else modX = 0;
        if (modY > 0) modY = 1;
        else if (modY < 0) modY = -1;
        else modY = 0;
        if (modZ > 0) modZ = 1;
        else if (modZ < 0) modZ = -1;
        else modZ = 0;
        // construct new vector
        return new WorldVectorFace(world, x1, y1, z1, VectorFace.fromMods(modX, modY, modZ));
    }

}
