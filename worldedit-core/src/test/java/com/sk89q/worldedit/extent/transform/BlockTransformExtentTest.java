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

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockData;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.LegacyBlockRegistry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@Ignore("Old BlockData class needs to be updated manually. Current block definitions are in blocks.json, " +
        "which is automatically generated and generally accurate.")
public class BlockTransformExtentTest {

    private static final Transform ROTATE_90 = new AffineTransform().rotateY(-90);
    private static final Transform ROTATE_NEG_90 = new AffineTransform().rotateY(90);
    private final Set<BlockType> ignored = new HashSet<BlockType>();

    @Before
    public void setUp() throws Exception {
        ignored.add(BlockType.BED); // Broken in existing rotation code?
        ignored.add(BlockType.WOODEN_DOOR); // Complicated
        ignored.add(BlockType.IRON_DOOR); // Complicated
        ignored.add(BlockType.END_PORTAL); // Not supported in existing rotation code
    }

    @Test
    public void testTransform() throws Exception {
        BlockRegistry blockRegistry = new LegacyBlockRegistry();
        for (BlockType type : BlockType.values()) {
            if (ignored.contains(type)) {
                continue;
            }

            BaseBlock orig = new BaseBlock(type.getID());
            for (int i = 1; i < 4; i++) {
                BaseBlock rotated = BlockTransformExtent.transform(new BaseBlock(orig), ROTATE_90, blockRegistry);
                BaseBlock reference = new BaseBlock(orig.getType(), BlockData.rotate90(orig.getType(), orig.getData()));
                assertThat(type + "#" + type.getID() + " rotated " + (90 * i) + " degrees did not match BlockData.rotate90()'s expected result", rotated, equalTo(reference));
                orig = rotated;
            }

            orig = new BaseBlock(type.getID());
            for (int i = 0; i < 4; i++) {
                BaseBlock rotated = BlockTransformExtent.transform(new BaseBlock(orig), ROTATE_NEG_90, blockRegistry);
                BaseBlock reference = new BaseBlock(orig.getType(), BlockData.rotate90Reverse(orig.getType(), orig.getData()));
                assertThat(type + "#" + type.getID() + " rotated " + (-90 * i) + " degrees did not match BlockData.rotate90Reverse()'s expected result", rotated, equalTo(reference));
                orig = rotated;
            }
        }
    }
}