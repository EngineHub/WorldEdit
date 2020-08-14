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

package com.sk89q.worldedit.util.formatting.component;

import com.google.common.collect.Iterables;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.util.HelpGenerator;

import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.internal.command.CommandUtil.getSubCommands;

/**
 * A box to describe usage of a command.
 */
public class CommandUsageBox extends TextComponentProducer {

    /**
     * Create a new usage box.
     *
     * @param commands the commands to describe
     * @param commandString the commands that were used, such as "/we" or "/brush sphere"
     * @param helpRootCommand the command used to get subcommand help
     */
    public CommandUsageBox(List<Command> commands, String commandString, String helpRootCommand) throws InvalidComponentException {
        this(commands, commandString, helpRootCommand, null);
    }

    /**
     * Create a new usage box.
     *
     * @param commands the commands to describe
     * @param commandString the commands that were used, such as "/we" or "/brush sphere"
     * @param helpRootCommand the command used to get subcommand help
     * @param parameters list of parameters to use
     */
    public CommandUsageBox(List<Command> commands, String commandString, String helpRootCommand,
                           @Nullable CommandParameters parameters) throws InvalidComponentException {
        checkNotNull(commands);
        checkNotNull(commandString);
        checkNotNull(helpRootCommand);
        attachCommandUsage(commands, commandString, helpRootCommand);
    }

    private void attachCommandUsage(List<Command> commands, String commandString, String helpRootCommand) {
        TextComponentProducer boxContent = new TextComponentProducer()
            .append(HelpGenerator.create(commands).getFullHelp());
        if (getSubCommands(Iterables.getLast(commands)).size() > 0) {
            boxContent.append(TextComponent.newline())
                .append(ColorConfig.helpText().wrap(TextComponent.builder("> ")
                    .append(ColorConfig.mainText().wrap(TextComponent.builder("List Subcommands")
                        .decoration(TextDecoration.ITALIC, true)
                        .clickEvent(ClickEvent.runCommand(helpRootCommand + " -s " + commandString))
                        .hoverEvent(HoverEvent.showText(TextComponent.of("List all subcommands of this command")))
                        .build()))
                    .build()));
        }
        MessageBox box = new MessageBox("Help for " + commandString,
            boxContent);

        append(box.create());
    }

}
