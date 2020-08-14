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

import java.util.Arrays;

/**
 * A linear Kernel generator (all cells weigh the same).
 */
public class LinearKernel extends Kernel {

    public LinearKernel(int radius) {
        super(radius * 2 + 1, radius * 2 + 1, createKernel(radius));
    }

    private static float[] createKernel(int radius) {
        int diameter = radius * 2 + 1;
        float[] data = new float[diameter * diameter];

        Arrays.fill(data, 1.0f / data.length);

        return data;
    }

}
