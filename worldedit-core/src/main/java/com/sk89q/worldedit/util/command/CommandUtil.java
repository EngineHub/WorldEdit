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

package com.sk89q.worldedit.util.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.internal.command.CommandLoggingHandler;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandUtil {

    public static Map<String, Command> getSubCommands(Command currentCommand) {
        return currentCommand.getParts().stream()
            .filter(p -> p instanceof SubCommandPart)
            .flatMap(p -> ((SubCommandPart) p).getCommands().stream())
            .collect(Collectors.toMap(Command::getName, Function.identity()));
    }

    private static String clean(String input) {
        return PlatformCommandManager.COMMAND_CLEAN_PATTERN.matcher(input).replaceAll("");
    }

    private static final Comparator<Command> BY_CLEAN_NAME =
        Comparator.comparing(c -> clean(c.getName()));

    public static Comparator<Command> byCleanName() {
        return BY_CLEAN_NAME;
    }

    private static final CommandPermissionsConditionGenerator PERM_GEN = new CommandPermissionsConditionGenerator();

    public static final Logger COMMAND_LOG =
        Logger.getLogger("com.sk89q.worldedit.CommandLog");
    private static final List<CommandCallListener> CALL_LISTENERS = ImmutableList.of(
        new CommandLoggingHandler(WorldEdit.getInstance(), COMMAND_LOG)
    );

    public static <CI> void register(CommandManager manager, CommandRegistration<CI> registration, CI instance) {
        registration.containerInstance(instance)
            .commandManager(manager)
            .listeners(CALL_LISTENERS);
        if (registration instanceof CommandPermissionsConditionGenerator.Registration) {
            ((CommandPermissionsConditionGenerator.Registration) registration).commandPermissionsConditionGenerator(
                PERM_GEN
            );
        }
        registration.build();
    }

    private CommandUtil() {
    }
}
