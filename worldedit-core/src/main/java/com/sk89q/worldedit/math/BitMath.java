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

package com.sk89q.worldedit.math;

public final class BitMath {

    public static int mask(int bits) {
        return ~(~0 << bits);
    }

    public static int unpackX(long packed) {
        return extractSigned(packed, 0, 26);
    }

    public static int unpackZ(long packed) {
        return extractSigned(packed, 26, 26);
    }

    public static int unpackY(long packed) {
        return extractSigned(packed, 26 + 26, 12);
    }

    public static int extractSigned(long i, int shift, int bits) {
        return fixSign((int) (i >> shift) & mask(bits), bits);
    }

    public static int fixSign(int i, int bits) {
        // Using https://stackoverflow.com/a/29266331/436524
        return i << (32 - bits) >> (32 - bits);
    }

    private BitMath() {
    }

}
