// $Id$
/*
 * WorldEdit
 * Copyright (C) 2011 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.bukkit.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

/**
 * @author zml2008
 */
public class CommandRegistration {
	private final Plugin plugin;
    private final CommandExecutor executor;
	private CommandMap fallbackCommands;

    public CommandRegistration(Plugin plugin) {
        this(plugin, plugin);
    }

    public CommandRegistration(Plugin plugin, CommandExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }
    
    public boolean registerAll(List<Command> registered) {
        CommandMap commandMap = getCommandMap();
        if (registered == null || commandMap == null) {
            return false;
        }
        for (Command command : registered) {
            commandMap.register(plugin.getDescription().getName(),
                    new DynamicPluginCommand(command.aliases(), command.desc(), "/" + command.aliases()[0] + " " + command.usage(), executor));
        }
        return true;
    }

    public CommandMap getCommandMap() {
        CommandMap commandMap = ReflectionUtil.getField(plugin.getServer().getPluginManager(), "commandMap");
        if (commandMap == null) {
            if (fallbackCommands != null) {
                commandMap = fallbackCommands;
            } else {
                Bukkit.getServer().getLogger().severe(plugin.getDescription().getName() +
                        ": Could not retrieve server CommandMap, using fallback instead! Please report to http://redmine.sk89q.com");
                fallbackCommands = commandMap = new SimpleCommandMap(Bukkit.getServer());
                Bukkit.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS,
                        new FallbackRegistrationListener(fallbackCommands), Event.Priority.Normal, plugin);
            }
        }
        return commandMap;
    }

    public boolean unregisterCommands() {
        CommandMap commandMap = getCommandMap();
        List<String> toRemove = new ArrayList<String>();
        Map<String, org.bukkit.command.Command> knownCommands = ReflectionUtil.getField(commandMap, "knownCommands");
        Set<String> aliases = ReflectionUtil.getField(commandMap, "aliases");
        if (knownCommands == null || aliases == null) {
            return false;
        }
        for (Iterator<org.bukkit.command.Command> i = knownCommands.values().iterator(); i.hasNext();) {
            org.bukkit.command.Command cmd = i.next();
            if (cmd instanceof DynamicPluginCommand && ((DynamicPluginCommand) cmd).getOwner().equals(plugin)) {
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
