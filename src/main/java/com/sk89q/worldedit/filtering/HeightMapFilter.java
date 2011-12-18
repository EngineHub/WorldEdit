// $Id$
/*
 * WorldEditLibrary
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

package com.sk89q.worldedit.filtering;

import java.awt.image.Kernel;

/**
 * Allows applications of Kernels onto the region's heightmap.
 * Only used for smoothing (with a GaussianKernel).
 * 
 * @author Grum
 */

public class HeightMapFilter {
    private Kernel kernel;

    /**
     * Construct the HeightMapFilter object.
     * 
     * @param kernel
     */
    public HeightMapFilter(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Construct the HeightMapFilter object.
     * 
     * @param kernelWidth
     * @param kernelHeight
     * @param kernelData
     */
    public HeightMapFilter(int kernelWidth, int kernelHeight, float[] kernelData) {
        this.kernel = new Kernel(kernelWidth, kernelHeight, kernelData);
    }

    /**
     * @return the kernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Set Kernel
     * 
     * @param kernel
     */
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Filter with a 2D kernel
     * 
     * @param inData
     * @param width
     * @param height
     * @return the modified heightmap
     */
    public int[] filter(int[] inData, int width, int height) {
        int index = 0;
        float[] matrix = kernel.getKernelData(null);
        int[] outData = new int[inData.length];

        int kh = kernel.getHeight();
        int kw = kernel.getWidth();
        int kox = kernel.getXOrigin();
        int koy = kernel.getYOrigin();

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
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
                        if (f == 0) continue;

                        int offsetX = x + kx - kox;
                        // Clamp coordinates inside data
                        if (offsetX < 0 || offsetX >= width) {
                            offsetX = x;
                        }

                        z += f * inData[offsetY + offsetX];
                    }
                }
                outData[index++] = (int) (z + 0.5);
            }
        }
        return outData;
    }
}
