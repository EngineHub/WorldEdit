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

package com.sk89q.worldedit.reorder.buffer;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class BufferConditions {

    public static int checkWithin(int a, int b) {
        if (a < 0 || a > b) {
            throw new IllegalArgumentException();
        }
        return a;
    }

    public static void checkBounds(int dstLen, int off, int len) {
        if (off < 0 || off > dstLen || len < 0 || len > (dstLen - off)) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static int checkReadPosition(int position, int limit) {
        if (position < 0 || position >= limit) {
            throw new BufferUnderflowException();
        }
        return position;
    }

    public static int checkWritePosition(int position, int limit) {
        if (position < 0 || position >= limit) {
            throw new BufferOverflowException();
        }
        return position;
    }

    public static int checkIndex(int index, int limit) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return index;
    }

    private BufferConditions() {
    }
}
