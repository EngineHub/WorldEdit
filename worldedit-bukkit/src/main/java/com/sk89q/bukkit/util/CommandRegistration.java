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

package com.sk89q.bukkit.util;

import com.sk89q.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandRegistration {

    static {
        Bukkit.getServer().getHelpMap().registerHelpTopicFactory(
            DynamicPluginCommand.class,
            new DynamicPluginCommandHelpTopic.Factory()
        );
    }

    protected final Plugin plugin;
    protected final CommandExecutor executor;
    private CommandMap serverCommandMap;
    private CommandMap fallbackCommands;

    public CommandRegistration(Plugin plugin) {
        this(plugin, plugin);
    }

    public CommandRegistration(Plugin plugin, CommandExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    public Plugin getCommandOwner(String label) {
        if (serverCommandMap == null) {
            return null;
        }
        Command command = serverCommandMap.getCommand(label);
        if (command instanceof PluginIdentifiableCommand) {
            return ((PluginIdentifiableCommand) command).getPlugin();
        }
        return null;
    }

    public boolean register(List<CommandInfo> registered) {
        CommandMap commandMap = getCommandMap();
        if (registered == null || commandMap == null) {
            return false;
        }
        for (CommandInfo command : registered) {
            DynamicPluginCommand cmd = new DynamicPluginCommand(command.getAliases(),
                    command.getDesc(), "/" + command.getAliases()[0] + " " + command.getUsage(), executor, command.getRegisteredWith(), plugin);
            cmd.setPermissions(command.getPermissions());
            commandMap.register(plugin.getDescription().getName(), cmd);
        }
        return true;
    }

    public CommandMap getCommandMap() {
        if (serverCommandMap != null) {
            return serverCommandMap;
        }
        if (fallbackCommands != null) {
            return fallbackCommands;
        }

        CommandMap commandMap = ReflectionUtil.getField(plugin.getServer().getPluginManager(), "commandMap");
        if (commandMap == null) {
            Bukkit.getServer().getLogger().severe(plugin.getDescription().getName()
                + ": Could not retrieve server CommandMap, using fallback instead!");
            fallbackCommands = commandMap = new SimpleCommandMap(Bukkit.getServer());
            Bukkit.getServer().getPluginManager().registerEvents(new FallbackRegistrationListener(fallbackCommands), plugin);
        } else {
            serverCommandMap = commandMap;
        }
        return commandMap;
    }

    public boolean unregisterCommands() {
        CommandMap commandMap = getCommandMap();
        List<String> toRemove = new ArrayList<>();
        Map<String, org.bukkit.command.Command> knownCommands = ReflectionUtil.getField(commandMap, "knownCommands");
        Set<String> aliases = ReflectionUtil.getField(commandMap, "aliases");
        if (knownCommands == null || aliases == null) {
            return false;
        }
        for (Iterator<org.bukkit.command.Command> i = knownCommands.values().iterator(); i.hasNext();) {
            org.bukkit.command.Command cmd = i.next();
            if (cmd instanceof DynamicPluginCommand && ((DynamicPluginCommand) cmd).getOwner().equals(executor)) {
                i.remove();
                for (String alias : cmd.getAliases()) {
                    org.bukkit.command.Command aliasCmd = knownCommands.get(alias);
                    if (cmd.equals(aliasCmd)) {
                        aliases.remove(alias);
                        toRemove.add(alias);
                    }
                }
            }
        }
        for (String string : toRemove) {
            knownCommands.remove(string);
        }
        return true;
    }

}
