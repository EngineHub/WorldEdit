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

package com.sk89q.worldedit.math.convolution;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows applications of Kernels onto the region's height map.
 *
 * <p>Only used for smoothing (with a GaussianKernel).</p>
 */
public class HeightMapFilter {

    private Kernel kernel;

    /**
     * Construct the HeightMapFilter object.
     *
     * @param kernel the kernel
     */
    public HeightMapFilter(Kernel kernel) {
        checkNotNull(kernel);
        this.kernel = kernel;
    }

    /**
     * Construct the HeightMapFilter object.
     *
     * @param kernelWidth the width
     * @param kernelHeight the height
     * @param kernelData the data
     */
    public HeightMapFilter(int kernelWidth, int kernelHeight, float[] kernelData) {
        checkNotNull(kernelData);
        this.kernel = new Kernel(kernelWidth, kernelHeight, kernelData);
    }

    /**
     * Get the kernel.
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Set the kernel.
     *
     * @param kernel the kernel
     */
    public void setKernel(Kernel kernel) {
        checkNotNull(kernel);

        this.kernel = kernel;
    }

    /**
     * Filter with a 2D kernel.
     *
     * @param inData the data
     * @param width the width
     * @param height the height
     *
     * @return the modified height map
     */
    public int[] filter(int[] inData, int width, int height) {
        return filter(inData, width, height, 0.5F);
    }

    /**
     * Filter with a 2D kernel.
     *
     * @param inData the data
     * @param width the width
     * @param height the height
     * @param offset the offset added to the height
     *
     * @return the modified height map
     */
    public int[] filter(int[] inData, int width, int height, float offset) {
        checkNotNull(inData);

        float[] inDataFloat = new float[inData.length];
        for (int i = 0; i < inData.length; i++) {
            inDataFloat[i] = inData[i];
        }

        int index = 0;
        float[] matrix = kernel.getKernelData(null);
        int[] outData = new int[inData.length];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                outData[index++] = (int) Math.floor(calculateHeight(inDataFloat, width, height, offset, matrix, x, y));
            }
        }
        return outData;
    }

    /**
     * Filter with a 2D kernel for float values.
     *
     * @param inData the data
     * @param width the width
     * @param height the height
     *
     * @return the modified height map
     */
    public float[] filter(float[] inData, int width, int height, float offset) {
        checkNotNull(inData);

        int index = 0;
        float[] matrix = kernel.getKernelData(null);
        float[] outData = new float[inData.length];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                outData[index++] = calculateHeight(inData, width, height, offset, matrix, x, y);
            }
        }
        return outData;
    }

    /**
     * Calculate the height based on the existing data and the kernel data.
     *
     * @param inData the existing height data
     * @param width the width
     * @param height the height
     * @param offset the offset to add to the height
     * @param matrix the matrix with the kernel's data
     * @param x the current x coordinate
     * @param y the current y coordinate
     * @return the calculated height for that block
     */
    private float calculateHeight(float[] inData, int width, int height, float offset, float[] matrix, int x, int y) {
        int kh = kernel.getHeight();
        int kw = kernel.getWidth();
        int kox = kernel.getXOrigin();
        int koy = kernel.getYOrigin();

        float z = 0;

        for (int ky = 0; ky < kh; ++ky) {
            int offsetY = y + ky - koy;
            // Clamp coordinates inside data
            if (offsetY < 0 || offsetY >= height) {
                offsetY = y;
            }

            offsetY *= width;

            int matrixOffset = ky * kw;
            for (int kx = 0; kx < kw; ++kx) {
                float f = matrix[matrixOffset + kx];
                if (f == 0) {
                    continue;
                }

                int offsetX = x + kx - kox;
                // Clamp coordinates inside data
                if (offsetX < 0 || offsetX >= width) {
                    offsetX = x;
                }

                z += f * inData[offsetY + offsetX];
            }
        }
        return z + offset;
    }

}
