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

package com.sk89q.worldedit.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Parameter;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.LinkedList;
import java.util.function.Predicate;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class CommandWrapper {
    private CommandWrapper() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher, CommandMapping command) {
        LiteralArgumentBuilder<CommandSource> base = literal(command.getPrimaryAlias());
        LinkedList<ArgumentBuilder<CommandSource, ?>> parameterStack = new LinkedList<>();
        LinkedList<ArgumentBuilder<CommandSource, ?>> optionalParameterStack = new LinkedList<>();
        boolean hasFlag = false;
        for (Parameter parameter : command.getDescription().getParameters()) {
            if (parameter.isValueFlag()) {
                if (!hasFlag) {
                    hasFlag = true;
                    optionalParameterStack.push(argument("flags", StringArgumentType.string()));
                }
            } else if (parameter.isOptional()) {
                optionalParameterStack.push(argument(parameter.getName(), StringArgumentType.string()));
            } else {
                parameterStack.push(argument(parameter.getName(), StringArgumentType.string()));
            }
        }

        ArgumentBuilder<CommandSource, ?> argument = buildChildNodes(parameterStack, optionalParameterStack, command);
        if (argument != null) {
            base.then(argument);
        } else {
            base.executes(commandFor(command));
        }
        LiteralCommandNode<CommandSource> registered =
            dispatcher.register(
                base.requires(requirementsFor(command))
            );
        for (String alias : command.getAllAliases()) {
            dispatcher.register(
                literal(alias).redirect(registered)
            );
        }
    }

    /**
     * Make the appropriate {@code then()} and {@code execute()} calls to emulate required and
     * optional parameters, given the argument orders.
     *
     * @param parameterStack required parameters
     * @param optionalParameterStack optional parameters
     * @return the node with all calls chained
     */
    private static ArgumentBuilder<CommandSource, ?> buildChildNodes(LinkedList<ArgumentBuilder<CommandSource, ?>> parameterStack,
                                                                     LinkedList<ArgumentBuilder<CommandSource, ?>> optionalParameterStack,
                                                                     CommandMapping mapping) {
        ArgumentBuilder<CommandSource, ?> currentChild = null;
        Command<CommandSource> command = commandFor(mapping);
        while (!optionalParameterStack.isEmpty()) {
            ArgumentBuilder<CommandSource, ?> next = optionalParameterStack.removeLast();
            if (currentChild != null) {
                next.then(currentChild.executes(command));
            }
            currentChild = next;
        }
        boolean requiredExecute = false;
        while (!parameterStack.isEmpty()) {
            ArgumentBuilder<CommandSource, ?> next = parameterStack.removeLast();
            if (currentChild != null) {
                next.then(currentChild);
            }
            if (!requiredExecute) {
                // first required parameter also gets execute
                requiredExecute = true;
                next.executes(command);
            }
            currentChild = next;
        }
        return currentChild;
    }

    private static Command<CommandSource> commandFor(CommandMapping mapping) {
        return FAKE_COMMAND;
    }

    public static final Command<CommandSource> FAKE_COMMAND = ctx -> {
        EntityPlayerMP player = ctx.getSource().asPlayer();
        if (player.world.isRemote()) {
            return 0;
        }
        return 1;
    };

    private static Predicate<CommandSource> requirementsFor(CommandMapping mapping) {
        return ctx -> {
            ForgePermissionsProvider permsProvider = ForgeWorldEdit.inst.getPermissionsProvider();
            return ctx.getEntity() instanceof EntityPlayerMP &&
                mapping.getDescription().getPermissions().stream()
                    .allMatch(perm -> permsProvider.hasPermission(
                        (EntityPlayerMP) ctx.getEntity(), perm
                    ));
        };
    }

}
