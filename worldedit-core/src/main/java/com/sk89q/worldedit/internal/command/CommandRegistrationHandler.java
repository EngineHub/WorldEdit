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

package com.sk89q.worldedit.internal.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;

import java.util.List;

public class CommandRegistrationHandler {

    private static final CommandPermissionsConditionGenerator PERM_GEN = new CommandPermissionsConditionGenerator();

    private final List<CommandCallListener> callListeners;

    public CommandRegistrationHandler(List<CommandCallListener> callListeners) {
        this.callListeners = ImmutableList.copyOf(callListeners);
    }

    public <CI> void register(CommandManager manager, CommandRegistration<CI> registration, CI instance) {
        registration.containerInstance(instance)
            .commandManager(manager)
            .listeners(callListeners);
        if (registration instanceof CommandPermissionsConditionGenerator.Registration) {
            ((CommandPermissionsConditionGenerator.Registration) registration).commandPermissionsConditionGenerator(
                PERM_GEN
            );
        }
        registration.build();
    }
}
