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

import java.util.TreeSet;

import org.junit.*;

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.blocks.BlockID;

import static org.junit.Assert.*;

/**
 * @author TomyLobo
 */
public class BlockDataTest {
    @Test
    public void testRotateFlip() {
        for (int type = 0; type < 256; ++type) {
            for (int data = 0; data < 16; ++data) {
                final String message = type+"/"+data;

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

    @Test
    public void testCycle() {
        // Test monotony
        for (int type = 0; type < 256; ++type) {
            if (type == BlockID.CLOTH)
                continue;

            for (int data = 0; data < 16; ++data) {
                final String message = type+"/"+data;

                final int cycled = BlockData.cycle(type, data, 1);

                if (cycled <= data) {
                    continue;
                }

                assertEquals(message, data+1, cycled);
            }
        }

        // Test cyclicity
        final TreeSet<Integer> datasTemplate = new TreeSet<Integer>();
        for (int data = 0; data < 16; ++data) {
            datasTemplate.add(data);
        }

        // Forwards...
        for (int type = 0; type < 256; ++type) {
            @SuppressWarnings("unchecked")
            final TreeSet<Integer> datas = (TreeSet<Integer>) datasTemplate.clone();
            while (!datas.isEmpty()) {
                final int start = datas.pollFirst();
                String message = type+"/"+start;
                int current = start;
                boolean first = true;
                while (true) {
                    current = BlockData.cycle(type, current, 1);
                    if (first && current == -1) break;
                    first = false;
                    message += "->"+current;
                    assertTrue(message, current >= 0);
                    assertTrue(message, current < 16);
                    if (current == start) break;
                    assertTrue(message, datas.remove(current));
                }
            }
        }
        
        // ...and backwards
        for (int type = 0; type < 256; ++type) {
            @SuppressWarnings("unchecked")
            final TreeSet<Integer> datas = (TreeSet<Integer>) datasTemplate.clone();
            while (!datas.isEmpty()) {
                final int start = datas.pollFirst();
                String message = type+"/"+start;
                int current = start;
                boolean first = true;
                while (true) {
                    current = BlockData.cycle(type, current, -1);
                    if (first && current == -1) break;
                    first = false;
                    message += "->"+current;
                    assertTrue(message, current >= 0);
                    assertTrue(message, current < 16);
                    if (current == start) break;
                    assertTrue(message, datas.remove(current));
                }
            }
        }
    }
}

