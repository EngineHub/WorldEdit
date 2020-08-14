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
import com.sk89q.worldedit.function.pattern.Pattern;
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
 * A tool that flood fills blocks.
 */
public class FloodFillTool implements BlockTool {

    private final int range;
    private final Pattern pattern;

    public FloodFillTool(int range, Pattern pattern) {
        this.range = range;
        this.pattern = pattern;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.flood-fill");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
        World world = (World) clicked.getExtent();

        BlockVector3 origin = clicked.toVector().toBlockPoint();
        BlockType initialType = world.getBlock(origin).getBlockType();

        if (initialType.getMaterial().isAir()) {
            return true;
        }

        if (initialType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }

        try (EditSession editSession = session.createEditSession(player)) {
            try {
                recurse(editSession, origin, origin, range, initialType, new HashSet<>());
            } catch (MaxChangedBlocksException e) {
                player.printError(TranslatableComponent.of("worldedit.tool.max-block-changes"));
            } finally {
                session.remember(editSession);
            }
        }

        return true;
    }

    private void recurse(EditSession editSession, BlockVector3 pos, BlockVector3 origin, int size, BlockType initialType,
            Set<BlockVector3> visited) throws MaxChangedBlocksException {

        if (origin.distance(pos) > size || visited.contains(pos)) {
            return;
        }

        visited.add(pos);

        if (editSession.getBlock(pos).getBlockType() == initialType) {
            editSession.setBlock(pos, pattern.applyBlock(pos));
        } else {
            return;
        }

        recurse(editSession, pos.add(1, 0, 0),
                origin, size, initialType, visited);
        recurse(editSession, pos.add(-1, 0, 0),
                origin, size, initialType, visited);
        recurse(editSession, pos.add(0, 0, 1),
                origin, size, initialType, visited);
        recurse(editSession, pos.add(0, 0, -1),
                origin, size, initialType, visited);
        recurse(editSession, pos.add(0, 1, 0),
                origin, size, initialType, visited);
        recurse(editSession, pos.add(0, -1, 0),
                origin, size, initialType, visited);
    }

}
