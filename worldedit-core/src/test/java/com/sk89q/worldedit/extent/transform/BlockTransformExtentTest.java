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

package com.sk89q.worldedit.extent.transform;

import static org.junit.Assert.assertEquals;

import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

@Ignore("A platform is currently required to get properties, preventing this test.")
public class BlockTransformExtentTest {

    private static final Transform ROTATE_90 = new AffineTransform().rotateY(-90);
    private static final Transform ROTATE_NEG_90 = new AffineTransform().rotateY(90);
    private final Set<BlockType> ignored = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        BlockType.REGISTRY.register("worldedit:test", new BlockType("worldedit:test"));
    }

    @Test
    public void testTransform() throws Exception {
        for (BlockType type : BlockType.REGISTRY.values()) {
            if (ignored.contains(type)) {
                continue;
            }

            BlockState base = type.getDefaultState();
            BlockState rotated = base;

            for (int i = 1; i < 4; i++) {
                rotated = BlockTransformExtent.transform(base, ROTATE_90);
            }
            assertEquals(base, rotated);
            rotated = base;
            for (int i = 1; i < 4; i++) {
                rotated = BlockTransformExtent.transform(base, ROTATE_NEG_90);
            }
            assertEquals(base, rotated);
        }
    }
}