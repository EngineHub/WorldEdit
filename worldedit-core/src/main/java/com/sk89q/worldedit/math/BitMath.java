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

package com.sk89q.worldedit.math;

public class BitMath {

    public static final long BITS_26 = 0x3_FF_FF_FF;
    public static final int BITS_14 = 0x3F_FF;
    public static final int BITS_12 = 0x0F_FF;
    public static final int BITS_8 = 0xFF;
    public static final int BITS_4 = 0x0F;

    private static final int FIX_SIGN_SHIFT = 32 - 26;

    public static int unpackX(long packed) {
        return fixSign26((int) (packed & BITS_26));
    }

    public static int unpackZ(long packed) {
        return fixSign26((int) ((packed >> 26) & BITS_26));
    }

    public static int unpackY(long packed) {
        return (int) ((packed >> (26 + 26)) & BITS_12);
    }

    /**
     * Fix horizontal sign -- we have a 26-bit two's-complement int,
     * we need it to be a 32-bit two's-complement int.
     */
    public static int fixSign26(int h) {
        // Using https://stackoverflow.com/a/29266331/436524
        return (h << FIX_SIGN_SHIFT) >> FIX_SIGN_SHIFT;
    }

    private BitMath() {
    }

}
