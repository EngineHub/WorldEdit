// $Id$
/*
 * WorldEdit
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

package com.sk89q.worldedit.data;

import org.junit.*;

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.blocks.BlockID;

import static org.junit.Assert.*;

public class BlockDataTest {
    @Test
    public void testRotateFlip() {
        for (int type = 0; type < 256; ++type) {
            for (int data = 0; data < 16; ++data) {
                final String message = type+"/"+data;

                assertEquals(message, data, BlockData.rotate90(type, BlockData.rotate90Reverse(type, data)));
                assertEquals(message, data, BlockData.rotate90Reverse(type, BlockData.rotate90(type, data)));

                int flipped = BlockData.flip(type, BlockData.flip(type, data, FlipDirection.WEST_EAST), FlipDirection.NORTH_SOUTH);

                assertEquals(message, flipped, BlockData.rotate90(type, BlockData.rotate90(type, data)));
                assertEquals(message, flipped, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, data)));

                assertEquals(message, data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.NORTH_SOUTH), FlipDirection.NORTH_SOUTH));
                assertEquals(message, data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.WEST_EAST), FlipDirection.WEST_EAST));
                assertEquals(message, data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.UP_DOWN), FlipDirection.UP_DOWN));
            }
        }
    }

    @Test
    public void testCycle() {
        for (int type = 0; type < 256; ++type) {
            if (type == BlockID.CLOTH)
                continue;

            for (int data = 0; data < 16; ++data) {
                final String message = type+"/"+data;

                int cycled = BlockData.cycle(type, data, 1);

                if (cycled <= data) {
                    continue;
                }

                assertEquals(message, data+1, cycled); 
            }
        }
    }
}
