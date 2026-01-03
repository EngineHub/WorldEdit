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
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.generator.TreeGenerator;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.generation.TreeType;

import javax.annotation.Nullable;

/**
 * Plants a tree.
 */
public class TreePlanter implements BlockTool {

    private final TreeType treeType;

    public TreePlanter(TreeType treeType) {
        this.treeType = treeType;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.tree");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {

        try (EditSession editSession = BlockTool.createEditSession(player, session, clicked)) {
            try {
                boolean successful = new TreeGenerator(editSession, treeType).apply(clicked.toVector().toBlockPoint());

                if (!successful) {
                    player.printError(TranslatableComponent.of("worldedit.tool.tree.obstructed"));
                }
            } catch (MaxChangedBlocksException e) {
                player.printError(TranslatableComponent.of("worldedit.tool.max-block-changes"));
            } catch (WorldEditException ignored) {
                // This should never happen
            } finally {
                session.remember(editSession);
            }
        }

        return true;
    }

}
