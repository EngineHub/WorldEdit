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

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.command.ScriptingCommands;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;

public class CommandTool implements BlockTool {

    private CommandContext args;
    private ScriptingCommands commands;

    public CommandTool(CommandContext args) {
        this.args = args;
        WorldEdit worldEdit = WorldEdit.getInstance();
        this.commands = new ScriptingCommands(worldEdit);
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.command");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, com.sk89q.worldedit.util.Location clicked) {

        // Sets the last clicked block so that it's accessible through scripting.
        player.setClicked(clicked);

        EditSession editSession = session.createEditSession(player);
        try {
            commands.execute(player, session, editSession, args);
        } catch (WorldEditException e) {
            player.printError(e.toString());
        }
        return true;
    }
}
