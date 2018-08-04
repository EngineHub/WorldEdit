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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A pickaxe mode that removes floating treetops (logs and leaves not connected
 * to anything else)
 */
public class FloatingTreeRemover implements BlockTool {
    private int rangeSq;

    public FloatingTreeRemover() {
        rangeSq = 100*100;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.deltree");
    }

    private boolean isTreeBlock(BlockType type) {
        return BlockCategories.LEAVES.contains(type)
                || BlockCategories.LOGS.contains(type)
                || type == BlockTypes.RED_MUSHROOM_BLOCK
                || type == BlockTypes.BROWN_MUSHROOM_BLOCK
                || type == BlockTypes.MUSHROOM_STEM
                || type == BlockTypes.VINE;
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config,
            Player player, LocalSession session, Location clicked) {

        final World world = (World) clicked.getExtent();
        final BlockState state = world.getBlock(clicked.toVector());

        if (!isTreeBlock(state.getBlockType())) {
            player.printError("That's not a tree.");
            return true;
        }

        final EditSession editSession = session.createEditSession(player);

        try {
            final Set<Vector> blockSet = bfs(world, clicked.toVector());
            if (blockSet == null) {
                player.printError("That's not a floating tree.");
                return true;
            }

            for (Vector blockVector : blockSet) {
                final BlockState otherState = editSession.getBlock(blockVector);
                if (isTreeBlock(otherState.getBlockType())) {
                    editSession.setBlock(blockVector, BlockTypes.AIR.getDefaultState());
                }
            }
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            session.remember(editSession);
        }

        return true;
    }

    private Vector[] recurseDirections = {
            Direction.NORTH.toVector(),
            Direction.EAST.toVector(),
            Direction.SOUTH.toVector(),
            Direction.WEST.toVector(),
            Direction.UP.toVector(),
            Direction.DOWN.toVector(),
    };

    /**
     * Helper method.
     *
     * @param world the world that contains the tree
     * @param origin any point contained in the floating tree
     * @return a set containing all blocks in the tree/shroom or null if this is not a floating tree/shroom.
     */
    private Set<Vector> bfs(World world, Vector origin) throws MaxChangedBlocksException {
        final Set<Vector> visited = new HashSet<>();
        final LinkedList<Vector> queue = new LinkedList<>();

        queue.addLast(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            final Vector current = queue.removeFirst();
            for (Vector recurseDirection : recurseDirections) {
                final Vector next = current.add(recurseDirection);
                if (origin.distanceSq(next) > rangeSq) {
                    // Maximum range exceeded => stop walking
                    continue;
                }

                if (visited.add(next)) {
                    BlockState state = world.getBlock(next);
                    if (state.getBlockType() == BlockTypes.AIR || state.getBlockType() == BlockTypes.SNOW) {
                        continue;
                    }
                    if (isTreeBlock(state.getBlockType())) {
                        queue.addLast(next);
                    } else {
                        // we hit something solid - evaluate where we came from
                        final BlockType currentType = world.getBlock(current).getBlockType();
                        if (!BlockCategories.LEAVES.contains(currentType) && currentType != BlockTypes.VINE) {
                            // log/shroom touching a wall/the ground => this is not a floating tree, bail out
                            return null;
                        }
                    }
                }
            }
        }

        return visited;
    }
}
