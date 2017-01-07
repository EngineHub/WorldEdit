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

package com.sk89q.worldedit.math.convolution;

/*
 * This class was originally part of the JDK, java.awt.image.Kernel,
 * and has been modified to not load the entire AWT toolkit, since
 * that was apparently added in Java 8, even though this class
 * is completely standalone.
 */
public class Kernel {

    private int width;
    private int height;
    private int xOrigin;
    private int yOrigin;
    private float[] data;

    public Kernel(int width, int height, float[] data) {
        this.width = width;
        this.height = height;
        this.xOrigin = (width - 1) >> 1;
        this.yOrigin = (height - 1) >> 1;
        int len = width * height;
        if (data.length < len) {
            throw new IllegalArgumentException("Data array too small (is " + data.length + " and should be " + len);
        }
        this.data = new float[len];
        System.arraycopy(data, 0, this.data, 0, len);
    }

    public final int getXOrigin() {
        return xOrigin;
    }

    public final int getYOrigin() {
        return yOrigin;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final float[] getKernelData(float[] data) {
        if (data == null) {
            data = new float[this.data.length];
        } else if (data.length < this.data.length) {
            throw new IllegalArgumentException("Data array too small (should be " + this.data.length + " but is " + data.length + " )");
        }
        System.arraycopy(this.data, 0, data, 0, this.data.length);
        return data;
    }

}