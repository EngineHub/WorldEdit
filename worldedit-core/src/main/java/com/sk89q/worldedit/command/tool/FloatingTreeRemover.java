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
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A pickaxe mode that removes floating treetops (logs and leaves not connected
 * to anything else).
 */
public class FloatingTreeRemover implements BlockTool {
    private final int rangeSq;

    public FloatingTreeRemover() {
        rangeSq = 100 * 100;
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
                              Player player, LocalSession session, Location clicked,
                              @Nullable Direction face) {

        final World world = (World) clicked.getExtent();
        final BlockState state = world.getBlock(clicked.toVector().toBlockPoint());

        if (!isTreeBlock(state.getBlockType())) {
            player.printError(TranslatableComponent.of("worldedit.tool.deltree.not-tree"));
            return true;
        }

        try (EditSession editSession = session.createEditSession(player)) {
            try {
                final Set<BlockVector3> blockSet = bfs(world, clicked.toVector().toBlockPoint());
                if (blockSet == null) {
                    player.printError(TranslatableComponent.of("worldedit.tool.deltree.not-floating"));
                    return true;
                }

                for (BlockVector3 blockVector : blockSet) {
                    final BlockState otherState = editSession.getBlock(blockVector);
                    if (isTreeBlock(otherState.getBlockType())) {
                        editSession.setBlock(blockVector, BlockTypes.AIR.getDefaultState());
                    }
                }
            } catch (MaxChangedBlocksException e) {
                player.printError(TranslatableComponent.of("worldedit.tool.max-block-changes"));
            } finally {
                session.remember(editSession);
            }
        }

        return true;
    }

    private final BlockVector3[] recurseDirections = {
            Direction.NORTH.toBlockVector(),
            Direction.EAST.toBlockVector(),
            Direction.SOUTH.toBlockVector(),
            Direction.WEST.toBlockVector(),
            Direction.UP.toBlockVector(),
            Direction.DOWN.toBlockVector(),
    };

    /**
     * Helper method.
     *
     * @param world the world that contains the tree
     * @param origin any point contained in the floating tree
     * @return a set containing all blocks in the tree/shroom or null if this is not a floating tree/shroom.
     */
    private Set<BlockVector3> bfs(World world, BlockVector3 origin) {
        final Set<BlockVector3> visited = new HashSet<>();
        final LinkedList<BlockVector3> queue = new LinkedList<>();

        queue.addLast(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            final BlockVector3 current = queue.removeFirst();
            for (BlockVector3 recurseDirection : recurseDirections) {
                final BlockVector3 next = current.add(recurseDirection);
                if (origin.distanceSq(next) > rangeSq) {
                    // Maximum range exceeded => stop walking
                    continue;
                }

                if (visited.add(next)) {
                    BlockState state = world.getBlock(next);
                    if (state.getBlockType().getMaterial().isAir() || state.getBlockType() == BlockTypes.SNOW) {
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
