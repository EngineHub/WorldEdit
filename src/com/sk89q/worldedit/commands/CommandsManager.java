// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;

/**
 * Manager for handling commands.
 * 
 * @author sk89q
 */
public class CommandsManager {
    /**
     * Mapping of commands (including aliases) with their method.
     */
    public Map<String, Method> commands = new HashMap<String, Method>();
    /**
     * Mapping of commands (not including aliases) with a description.
     */
    public Map<String, String> descs = new HashMap<String, String>();
    
    /**
     * Register an object that contains commands (denoted by the
     * <code>com.sk89q.util.commands.Command</code> annotation. The methods are
     * cached into a map for later usage and it reduces the overhead of
     * reflection (method lookup via reflection is relatively slow).
     * 
     * @param cls
     */
    public void register(Class<?> cls) {
        for (Method method : cls.getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }

            Command cmd = method.getAnnotation(Command.class);
            
            // Cache the commands
            for (String alias : cmd.aliases()) {
                commands.put(alias, method);
            }
            
            // Build a list of commands and their usage details
            if (cmd.usage().length() == 0) {
                descs.put(cmd.aliases()[0], cmd.desc());
            } else {
                descs.put(cmd.aliases()[0], cmd.usage() + " - " + cmd.desc());
            }
        }
    }
    
    /**
     * Checks to see whether there is a command.
     * 
     * @param command
     * @return
     */
    public boolean hasCommand(String command) {
        return commands.containsKey(command.toLowerCase());
    }
    
    /**
     * Get a list of command descriptions.
     * 
     * @return
     */
    public Map<String, String> getCommands() {
        return descs;
    }
    
    /**
     * Get the usage string for a command.
     * 
     * @param command
     * @param cmd
     * @return
     */
    private String getUsage(String command, Command cmd) {
        return command
                + (cmd.flags().length() > 0 ? " [-" + cmd.flags() + "] " : " ")
                + cmd.usage();
    }
    
    /**
     * Attempt to execute a command.
     * 
     * @param args
     * @param we
     * @param session
     * @param player
     * @param editSession
     * @return
     */
    public boolean execute(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException, Throwable {
        Method method = commands.get(args.getCommand().toLowerCase());
        
        if (method == null) {
            return false; // No command
        }
        
        if (!checkPermissions(method, player)) {
            return true;
        }

        Command cmd = method.getAnnotation(Command.class);
        
        if (args.argsLength() < cmd.min()) {
            player.printError("Too few arguments.");
            player.printError(getUsage(args.getCommand(), cmd));
            return true;
        }
        
        if (cmd.max() != -1 && args.argsLength() > cmd.max()) {
            player.printError("Too many arguments.");
            player.printError(getUsage(args.getCommand(), cmd));
            return true;
        }
        
        for (char flag : args.getFlags()) {
            if (cmd.flags().indexOf(String.valueOf(flag)) == -1) {
                player.printError("Unknown flag: " + flag);
                player.printError(getUsage(args.getCommand(), cmd));
                return true;
            }
        }
        
        try {
            method.invoke(null, args, we, session, player, editSession);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof WorldEditException) {
                throw (WorldEditException)e.getCause();
            } else if (e.getCause() instanceof NumberFormatException) {
                throw (NumberFormatException)e.getCause();
            } else {
                throw e.getCause();
            }
        }
        
        return true;
    }
    
    private boolean checkPermissions(Method method, LocalPlayer player) {
        CommandPermissions perms = method.getAnnotation(CommandPermissions.class);
        if (perms == null) {
            return true;
        }
        
        for (String perm : perms.value()) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        
        player.printError("You don't have permission for this command.");
        return false;
    }
}
