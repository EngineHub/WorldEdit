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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("A heightmap")
public class HeightMapFilterTest {

    /**
     * A simple kernel test to validate the kernel on flat world works fine.
     *
     * <p>The kernel should not change the height because everything is flat!</p>
     *
     * @param height The height to test
     * @param kernel The used kernel
     */
    private void testKernelOnFlat(float height, Kernel kernel) {
        HeightMapFilter filter = new HeightMapFilter(kernel);

        float[] data = new float[9 * 9];
        Arrays.fill(data, height);

        float[] output = filter.filter(data, 1, 1, 0);
        assertEquals(height, output[0], 0.05);
    }

    /**
     * Test the Gaussian kernel with the HeightMapFilter.
     * @param height the height to test, parameterized
     */
    @ParameterizedTest
    @ValueSource(floats = {-25.0f, -10.0f,  0f, 10f, 25f})
    public void testGaussianHeightMap(float height) {
        testKernelOnFlat(height, new GaussianKernel(1, 1));

        testKernelOnFlat(height, new GaussianKernel(3, 1));
    }

    /**
     * Test the linear kernel with the HeightMapFilter.
     * @param height the height to test, parameterized
     */
    @ParameterizedTest
    @ValueSource(floats = {-25.0f, -10.0f,  0f, 10f, 25f})
    public void testLinearHeightMap(float height) {
        testKernelOnFlat(height, new LinearKernel(1));

        testKernelOnFlat(height, new LinearKernel(3));
    }
}
