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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Gaussian Kernel for HeightMaps")
public class GaussianKernelTest {
    private void testGaussian(GaussianKernel kernel) {
        float[] data = kernel.getKernelData(null);
        float sum = 0;
        for (float datum : data) {
            assertTrue(datum >= 0);
            sum += datum;
        }
        // The sum has to be 1
        assertEquals(1f, sum, 0.01);
    }

    /**
     * Test the creation of the gaussian kernel with Sigma 1.
     * @param radius the radius to test.
     */
    @ParameterizedTest(name = "radius={0}")
    @ValueSource(ints = { 1, 2, 5, 10 })
    public void testGaussianKernelSigma1(int radius) {
        testGaussian(new GaussianKernel(radius, 1));
    }

    /**
     * Test the creation of the gaussian kernel with Sigma 5.
     * @param radius the radius to test.
     */
    @ParameterizedTest(name = "radius={0}")
    @ValueSource(ints = { 1, 2, 5, 10 })
    public void testGaussianKernelSigma5(int radius) {
        testGaussian(new GaussianKernel(radius, 5));
    }
}
