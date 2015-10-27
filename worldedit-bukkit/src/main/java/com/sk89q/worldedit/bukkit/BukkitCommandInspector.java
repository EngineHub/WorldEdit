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

package com.sk89q.worldedit.bukkit;

import com.sk89q.bukkit.util.CommandInspector;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Description;
import com.sk89q.worldedit.util.command.Dispatcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

class BukkitCommandInspector implements CommandInspector {

    private static final Logger logger = Logger.getLogger(BukkitCommandInspector.class.getCanonicalName());
    private final WorldEditPlugin plugin;
    private final Dispatcher dispatcher;

    BukkitCommandInspector(WorldEditPlugin plugin, Dispatcher dispatcher) {
        checkNotNull(plugin);
        checkNotNull(dispatcher);
        this.plugin = plugin;
        this.dispatcher = dispatcher;
    }

    @Override
    public String getShortText(Command command) {
        CommandMapping mapping = dispatcher.get(command.getName());
        if (mapping != null) {
            return mapping.getDescription().getDescription();
        } else {
            logger.warning("BukkitCommandInspector doesn't know how about the command '" + command + "'");
            return "Help text not available";
        }
    }

    @Override
    public String getFullText(Command command) {
        CommandMapping mapping = dispatcher.get(command.getName());
        if (mapping != null) {
            Description description = mapping.getDescription();
            return "Usage: " + description.getUsage() + (description.getHelp() != null ? "\n" + description.getHelp() : "");
        } else {
            logger.warning("BukkitCommandInspector doesn't know how about the command '" + command + "'");
            return "Help text not available";
        }
    }

    @Override
    public boolean testPermission(CommandSender sender, Command command) {
        CommandMapping mapping = dispatcher.get(command.getName());
        if (mapping != null) {
            CommandLocals locals = new CommandLocals();
            locals.put(Actor.class, plugin.wrapCommandSender(sender));
            return mapping.getCallable().testPermission(locals);
        } else {
            logger.warning("BukkitCommandInspector doesn't know how about the command '" + command + "'");
            return false;
        }
    }
}
