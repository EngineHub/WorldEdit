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

package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BlockDataTest {

    @Test
    public void testRotateFlip() {
        for (int type = 0; type < 256; ++type) {
            for (int data = 0; data < 16; ++data) {
                final String message = type + "/" + data;

                //Test r90(r-90(x))==x
                assertEquals(message, data, BlockData.rotate90(type, BlockData.rotate90Reverse(type, data)));
                //Test r-90(r90(x))==x
                assertEquals(message, data, BlockData.rotate90Reverse(type, BlockData.rotate90(type, data)));

                final int flipped = BlockData.flip(type, BlockData.flip(type, data, FlipDirection.WEST_EAST), FlipDirection.NORTH_SOUTH);

                //Test r90(r90(x))==flipNS(flipWE(x))
                assertEquals(message, flipped, BlockData.rotate90(type, BlockData.rotate90(type, data)));
                //Test r-90(r-90(x))==flipNS(flipWE(x))
                assertEquals(message, flipped, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, data)));

                //Test flipNS(flipNS(x))==x
                assertEquals(message, data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.NORTH_SOUTH), FlipDirection.NORTH_SOUTH));
                //Test flipWE(flipWE(x))==x
                assertEquals(message, data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.WEST_EAST), FlipDirection.WEST_EAST));
                //Test flipUD(flipUD(x))==x
                assertEquals(message, data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.UP_DOWN), FlipDirection.UP_DOWN));

                //Test r90(r90(r90(r90(x))))==x
                assertEquals(message, data, BlockData.rotate90(type, BlockData.rotate90(type, BlockData.rotate90(type, BlockData.rotate90(type, data)))));
                //Test r-90(r-90(r-90(r-90(x))))==x
                assertEquals(message, data, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, data)))));
            }
        }
    }
}
