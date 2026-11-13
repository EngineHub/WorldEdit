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

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Drives WorldEdit the way a player does: commands typed by the client player in a singleplayer world,
 * checked against the server's level, with a screenshot of the result.
 *
 * <p>Screenshots are written to {@code worldedit-fabric/build/run/clientGameTest/screenshots}.</p>
 */
public class SetCommandClientGameTest implements FabricClientGameTest {
    private static final BlockPos MIN = new BlockPos(0, -60, 4);
    private static final BlockPos MAX = MIN.offset(4, 4, 4);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(settings -> settings.setAllowCommands(true))
                .create()) {
            TestServerConnection connection = singleplayer.getConnection();
            connection.waitForChunksRender();

            // Stand south-west of the region, looking at it
            singleplayer.getServer().runCommand("tp @a 2 -56 0 0 25");
            context.waitTicks(5);

            sendCommand(context, "/pos1 " + MIN.getX() + "," + MIN.getY() + "," + MIN.getZ());
            sendCommand(context, "/pos2 " + MAX.getX() + "," + MAX.getY() + "," + MAX.getZ());
            sendCommand(context, "/set stone");
            singleplayer.getServer().waitFor(server -> allBlocks(server.overworld(), Blocks.STONE));
            connection.waitForClientboundPackets();
            context.waitFor(client -> allBlocks(client.level, Blocks.STONE));
            // Changed sections are re-meshed asynchronously, after the client has the blocks
            context.waitTicks(20);
            connection.waitForChunksRender();
            context.takeScreenshot("worldedit-set-stone");

            sendCommand(context, "/undo");
            singleplayer.getServer().waitFor(server -> allBlocks(server.overworld(), Blocks.AIR));
        }
    }

    /**
     * Sends a command as the client player, like typing it in chat. {@code "/set stone"} is {@code //set stone}.
     */
    private static void sendCommand(ClientGameTestContext context, String command) {
        context.runOnClient(client -> client.player.connection.sendCommand(command));
        context.waitTicks(2);
    }

    private static boolean allBlocks(Level level, Block block) {
        for (BlockPos pos : BlockPos.betweenClosed(MIN, MAX)) {
            if (!level.getBlockState(pos).is(block)) {
                return false;
            }
        }
        return true;
    }
}
