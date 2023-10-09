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

package com.sk89q.worldedit.command.util;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.adventure.CommandListBox;
import com.sk89q.worldedit.util.formatting.adventure.CommandUsageBox;
import com.sk89q.worldedit.util.formatting.adventure.InvalidComponentException;
import com.sk89q.worldedit.util.adventure.text.Component;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sk89q.worldedit.internal.command.CommandUtil.byCleanName;
import static com.sk89q.worldedit.internal.command.CommandUtil.getSubCommands;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the //help command.
 */
// Stored in a separate class to prevent import conflicts, and because it's aliased via /we help.
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

    public static void help(List<String> commandPath, int page, boolean listSubCommands,
                            CommandManager manager, Actor actor, String helpRootCommand) throws InvalidComponentException {

        if (commandPath.isEmpty()) {
            printCommands(page, manager.getAllCommands(), actor, ImmutableList.of(), helpRootCommand);
            return;
        }

        List<Command> visited = new ArrayList<>();
        Command currentCommand = detectCommand(manager, commandPath.get(0));
        if (currentCommand == null) {
            actor.printError(Component.translatable("worldedit.help.command-not-found", Component.text(commandPath.get(0))));
            return;
        }
        visited.add(currentCommand);

        // Drill down to the command
        for (int i = 1; i < commandPath.size(); i++) {
            String subCommand = commandPath.get(i);
            Map<String, Command> subCommands = getSubCommands(currentCommand);

            if (subCommands.isEmpty()) {
                actor.printError(Component.translatable("worldedit.help.no-subcommands",
                    Component.text(toCommandString(visited)), Component.text(subCommand)));
                // full help for single command
                CommandUsageBox box = new CommandUsageBox(visited, visited.stream()
                        .map(Command::getName).collect(Collectors.joining(" ")), helpRootCommand);
                actor.print(box.build());
                return;
            }

            if (subCommands.containsKey(subCommand)) {
                currentCommand = subCommands.get(subCommand);
                visited.add(currentCommand);
            } else {
                actor.printError(Component.translatable("worldedit.help.subcommand-not-found",
                    Component.text(subCommand), Component.text(toCommandString(visited))));
                // list subcommands for currentCommand
                printCommands(page, getSubCommands(Iterables.getLast(visited)).values().stream(), actor, visited, helpRootCommand);
                return;
            }
        }

        Map<String, Command> subCommands = getSubCommands(currentCommand);

        if (subCommands.isEmpty() || !listSubCommands) {
            // Create the message
            CommandUsageBox box = new CommandUsageBox(visited, toCommandString(visited), helpRootCommand);
            actor.print(box.build());
        } else {
            printCommands(page, subCommands.values().stream(), actor, visited, helpRootCommand);
        }
    }

    private static String toCommandString(List<Command> visited) {
        return "/" + Joiner.on(" ").join(visited.stream().map(Command::getName).iterator());
    }

    private static void printCommands(int page, Stream<Command> commandStream, Actor actor,
                                      List<Command> commandList, String helpRootCommand) throws InvalidComponentException {
        InjectedValueStore store = MapBackedValueStore.create();
        store.injectValue(Key.of(Actor.class), context ->
            Optional.of(actor));

        // Get a list of aliases
        List<Command> commands = commandStream
            .filter(command -> command.getCondition().satisfied(store))
            .sorted(byCleanName())
            .collect(toList());

        String used = commandList.isEmpty() ? null : toCommandString(commandList);
        CommandListBox box = new CommandListBox(
                (used == null ? "Help" : "Subcommands: " + used),
                helpRootCommand + " -s -p %page%" + (used == null ? "" : " " + used),
                helpRootCommand);
        if (!actor.isPlayer()) {
            box.formatForConsole();
        }

        for (Command mapping : commands) {
            String alias = (commandList.isEmpty() ? "/" : "") + mapping.getName();
            String command = Stream.concat(commandList.stream(), Stream.of(mapping))
                .map(Command::getName)
                .collect(Collectors.joining(" ", "/", ""));
            box.appendCommand(alias, mapping.getDescription(), command);
        }

        actor.print(box.build(page));
    }

    private PrintCommandHelp() {
    }
}
