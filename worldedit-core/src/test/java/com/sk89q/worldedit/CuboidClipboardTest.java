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

package com.sk89q.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import org.junit.Test;

import static org.junit.Assert.*;

public class CuboidClipboardTest {
    @Test
    public void testFlipCenterPlane() throws Exception {
        testFlip(0, 1, CuboidClipboard.FlipDirection.UP_DOWN);
        testFlip(2, 3, CuboidClipboard.FlipDirection.NORTH_SOUTH);
        testFlip(4, 5, CuboidClipboard.FlipDirection.WEST_EAST);
    }

    private void testFlip(int data, int expectedDataAfterFlip, CuboidClipboard.FlipDirection flipDirection) {
        final CuboidClipboard clipboard = new CuboidClipboard(new Vector(1, 1, 1));
        clipboard.setBlock(Vector.ZERO, new BaseBlock(BlockID.PISTON_BASE, data));
        clipboard.flip(flipDirection);
        assertEquals(expectedDataAfterFlip, clipboard.getBlock(Vector.ZERO).getData());
    }
}
