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

package com.sk89q.worldedit.spout;

import org.spout.api.chat.ChatSection;
import org.spout.api.command.Command;
import org.spout.api.command.CommandSource;
import org.spout.api.command.RawCommandExecutor;
import org.spout.api.exception.CommandException;

import java.util.List;

/**
 * @author zml2008
 */
public class SpoutRawCommandExecutor implements RawCommandExecutor {

    private final WorldEditPlugin plugin;

    public SpoutRawCommandExecutor(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Command cmd, CommandSource source, String name, List<ChatSection> args, int baseIndex, boolean fuzzyLookup) throws CommandException {
        String[] argArray = new String[args.size() - baseIndex + 1];
        argArray[0] = "/" + cmd.getPreferredName();
        for (int i = baseIndex; i < args.size(); ++i) {
            argArray[i - baseIndex + 1] = args.get(i).getPlainString();
        }

        if (!plugin.getWorldEdit().handleCommand(plugin.wrapCommandSender(source), argArray)) {
            throw new CommandException("Unknown command: '/" + name + "'!");
        }
    }
}
