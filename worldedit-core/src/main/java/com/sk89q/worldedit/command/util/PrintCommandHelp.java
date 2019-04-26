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

package com.sk89q.worldedit.command.util;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.CodeFormat;
import com.sk89q.worldedit.util.formatting.component.CommandListBox;
import com.sk89q.worldedit.util.formatting.component.CommandUsageBox;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.command.CommandUtil.byCleanName;
import static com.sk89q.worldedit.util.command.CommandUtil.getSubCommands;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the //help command.
 */
// Stored in a separate class to prevent import conflicts.
public class PrintCommandHelp {

    private static Command detectCommand(CommandManager manager, String command) {
        Optional<Command> mapping;

        // First try the command as entered
        mapping = manager.getCommand(command);
        if (mapping.isPresent()) {
            return mapping.get();
        }

        // If tried with slashes, try dropping a slash
        if (command.startsWith("/")) {
            mapping = manager.getCommand(command.substring(1));
            return mapping.orElse(null);
        }

        // Otherwise, check /command, since that's common
        mapping = manager.getCommand("/" + command);
        return mapping.orElse(null);
    }

    public static void help(List<String> commandPath, int page, WorldEdit we, Actor actor) {
        if (page < 1) {
            actor.printError("Page must be >= 1.");
            return;
        }
        CommandManager manager = we.getPlatformManager().getPlatformCommandMananger().getCommandManager();

        final int perPage = actor instanceof Player ? 8 : 20; // More pages for console

        if (commandPath.isEmpty()) {
            printAllCommands(page, perPage, manager.getAllCommands(), actor, ImmutableList.of());
            return;
        }

        List<String> visited = new ArrayList<>();
        Command currentCommand = detectCommand(manager, commandPath.get(0));
        if (currentCommand == null) {
            actor.printError(String.format("The command '%s' could not be found.", commandPath.get(0)));
            return;
        }
        visited.add(commandPath.get(0));

        // Drill down to the command
        for (int i = 1; i < commandPath.size(); i++) {
            String subCommand = commandPath.get(i);
            Map<String, Command> subCommands = getSubCommands(currentCommand);

            if (subCommands.isEmpty()) {
                actor.printError(String.format("'%s' has no sub-commands. (Maybe '%s' is for a parameter?)",
                    Joiner.on(" ").join(visited), subCommand));
                return;
            }

            if (subCommands.containsKey(subCommand)) {
                visited.add(subCommand);
                currentCommand = subCommands.get(subCommand);
            } else {
                actor.printError(String.format("The sub-command '%s' under '%s' could not be found.",
                    subCommand, Joiner.on(" ").join(visited)));
                return;
            }
        }

        Map<String, Command> subCommands = getSubCommands(currentCommand);

        if (subCommands.isEmpty()) {
            // Create the message
            CommandUsageBox box = new CommandUsageBox(currentCommand, String.join(" ", visited));
            actor.print(box.create());
        } else {
            printAllCommands(page, perPage, subCommands.values().stream(), actor, visited);
        }
    }

    private static void printAllCommands(int page, int perPage, Stream<Command> commandStream, Actor actor,
                                         List<String> commandList) {
        // Get a list of aliases
        List<Command> commands = commandStream
            .sorted(byCleanName())
            .collect(toList());

        // Calculate pagination
        int offset = perPage * (page - 1);
        int pageTotal = (int) Math.ceil(commands.size() / (double) perPage);

        // Box
        CommandListBox box = new CommandListBox(String.format("Help: page %d/%d ", page, pageTotal));
        TextComponent.Builder tip = box.getContents().getBuilder().color(TextColor.GRAY);

        if (offset >= commands.size()) {
            tip.color(TextColor.RED)
                .append(TextComponent.of(String.format("There is no page %d (total number of pages is %d).\n", page, pageTotal)));
        } else {
            List<Command> list = commands.subList(offset, Math.min(offset + perPage, commands.size()));

            tip.append(TextComponent.of("Type "));
            tip.append(CodeFormat.wrap("//help [<page>] <command...>"));
            tip.append(TextComponent.of(" for more information.\n"));

            // Add each command
            for (Command mapping : list) {
                String alias = (commandList.isEmpty() ? "/" : "") + mapping.getName();
                String command = Stream.concat(commandList.stream(), Stream.of(mapping.getName()))
                    .collect(Collectors.joining(" ", "/", ""));
                box.appendCommand(alias, mapping.getDescription(), command);
            }
        }

        actor.print(box.create());
    }

    private PrintCommandHelp() {
    }
}
