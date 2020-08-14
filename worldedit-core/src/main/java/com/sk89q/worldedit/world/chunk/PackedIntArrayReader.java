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

package com.sk89q.worldedit.world.chunk;

import static com.google.common.base.Preconditions.checkElementIndex;

public class PackedIntArrayReader {
    private static final int[] FACTORS = new int[64];

    static {
        FACTORS[0] = -1;
        for (int i = 2; i <= 64; i++) {
            FACTORS[i - 1] = (int) (Integer.toUnsignedLong(-1) / i);
        }
    }

    private static final int SIZE = 4096;

    private final long[] data;
    private final int elementBits;
    private final long maxValue;
    private final int elementsPerLong;
    private final int factor;

    public PackedIntArrayReader(long[] data) {
        this.data = data;
        this.elementBits = data.length * 64 / 4096;
        this.maxValue = (1L << elementBits) - 1L;
        this.elementsPerLong = 64 / elementBits;
        this.factor = FACTORS[elementsPerLong - 1];
        int j = (SIZE + this.elementsPerLong - 1) / this.elementsPerLong;
        if (j != data.length) {
            throw new IllegalStateException("Invalid packed-int array provided, should be of length " + j);
        }
    }

    public int get(int index) {
        checkElementIndex(index, SIZE);
        int i = this.adjustIndex(index);
        long l = this.data[i];
        int j = (index - i * this.elementsPerLong) * this.elementBits;
        return (int) (l >> j & this.maxValue);
    }

    private int adjustIndex(int i) {
        return (int) ((long) i * factor + factor >> 32);
    }

}
