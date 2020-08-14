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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A pickaxe mode that recursively finds adjacent blocks within range of
 * an initial block and of the same type.
 */
public class RecursivePickaxe implements BlockTool {

    private final double range;

    public RecursivePickaxe(double range) {
        this.range = range;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.superpickaxe.recursive");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
        World world = (World) clicked.getExtent();

        BlockVector3 origin = clicked.toVector().toBlockPoint();
        BlockType initialType = world.getBlock(origin).getBlockType();

        if (initialType.getMaterial().isAir()) {
            return false;
        }

        if (initialType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
            return false;
        }

        try (EditSession editSession = session.createEditSession(player)) {
            editSession.getSurvivalExtent().setToolUse(config.superPickaxeManyDrop);

            try {
                recurse(server, editSession, world, clicked.toVector().toBlockPoint(),
                        clicked.toVector().toBlockPoint(), range, initialType, new HashSet<>());
            } catch (MaxChangedBlocksException e) {
                player.printError(TranslatableComponent.of("worldedit.tool.max-block-changes"));
            } finally {
                session.remember(editSession);
            }
        }

        return true;
    }

    private static void recurse(Platform server, EditSession editSession, World world, BlockVector3 pos,
            BlockVector3 origin, double size, BlockType initialType, Set<BlockVector3> visited) throws MaxChangedBlocksException {

        final double distanceSq = origin.distanceSq(pos);
        if (distanceSq > size * size || visited.contains(pos)) {
            return;
        }

        visited.add(pos);

        if (editSession.getBlock(pos).getBlockType() != initialType) {
            return;
        }

        editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());

        world.queueBlockBreakEffect(server, pos, initialType, distanceSq);

        recurse(server, editSession, world, pos.add(1, 0, 0),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(-1, 0, 0),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, 0, 1),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, 0, -1),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, 1, 0),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, -1, 0),
                origin, size, initialType, visited);
    }

}
