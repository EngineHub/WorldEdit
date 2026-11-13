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

package com.sk89q.worldedit.fabric.gametest;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public class SetCuboidGameTest {
    @GameTest
    public void setCuboidToStone(GameTestHelper helper) throws MaxChangedBlocksException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        BlockVector3 min = BlockVector3.at(origin.getX(), origin.getY(), origin.getZ());
        BlockVector3 max = min.add(2, 2, 2);

        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            int changed = session.setBlocks(new CuboidRegion(world, min, max), BlockTypes.STONE.getDefaultState());
            helper.assertValueEqual(changed, 27, "changed block count");
        }

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    helper.assertBlockPresent(Blocks.STONE, x, y, z);
                }
            }
        }
        helper.succeed();
    }
}
