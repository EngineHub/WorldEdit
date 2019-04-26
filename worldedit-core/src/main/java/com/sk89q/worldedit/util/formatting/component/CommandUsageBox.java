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

package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.util.formatting.StyledFragment;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParameters;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.util.command.CommandUtil.byCleanName;
import static com.sk89q.worldedit.util.command.CommandUtil.getSubCommands;

/**
 * A box to describe usage of a command.
 */
public class CommandUsageBox extends StyledFragment {

    /**
     * Create a new usage box.
     *
     * @param command the command to describe
     * @param commandString the command that was used, such as "/we" or "/brush sphere"
     */
    public CommandUsageBox(Command command, String commandString) {
        this(command, commandString, null);
    }

    /**
     * Create a new usage box.
     *
     * @param command the command to describe
     * @param commandString the command that was used, such as "/we" or "/brush sphere"
     * @param parameters list of parameters to use
     */
    public CommandUsageBox(Command command, String commandString, @Nullable CommandParameters parameters) {
        checkNotNull(command);
        checkNotNull(commandString);
        Map<String, Command> subCommands = getSubCommands(command);
        if (subCommands.isEmpty()) {
            attachCommandUsage(command, commandString);
        } else {
            attachSubcommandUsage(subCommands, commandString, parameters);
        }
    }

    private void attachSubcommandUsage(Map<String, Command> dispatcher, String commandString, @Nullable CommandParameters parameters) {
        CommandListBox box = new CommandListBox("Subcommands");
        String prefix = !commandString.isEmpty() ? commandString + " " : "";

        List<Command> list = dispatcher.values().stream()
            .sorted(byCleanName())
            .collect(Collectors.toList());

        for (Command mapping : list) {
            if (parameters == null || mapping.getCondition().satisfied(parameters)) {
                box.appendCommand(prefix + mapping.getName(), mapping.getDescription());
            }
        }

        append(box);
    }

    private void attachCommandUsage(Command description, String commandString) {
        MessageBox box = new MessageBox("Help for " + commandString);

        box.getContents().append(description.getFullHelp());

        append(box);
    }

}
