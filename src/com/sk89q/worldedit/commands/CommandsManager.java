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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sk89q.util.StringUtil;
import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.util.commands.NestedCommand;
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
     * Mapping of nested commands (including aliases) with a description.
     */
    public Map<Method, Map<String, Method>> commands
            = new HashMap<Method, Map<String, Method>>();
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
        registerMethods(cls, null);
    }
    
    /**
     * Register the methods of a class.
     * 
     * @param cls
     * @param parent
     */
    private void registerMethods(Class<?> cls, Method parent) {
        Map<String, Method> map;
        
        if (commands.containsKey(parent)) {
            map = commands.get(parent);
        } else {
            map = new HashMap<String, Method>();
            commands.put(parent, map);
        }
        
        for (Method method : cls.getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }

            Command cmd = method.getAnnotation(Command.class);
            
            // Cache the commands
            for (String alias : cmd.aliases()) {
                map.put(alias, method);
            }
            
            // Build a list of commands and their usage details
            if (cmd.usage().length() == 0) {
                descs.put(cmd.aliases()[0], cmd.desc());
            } else {
                descs.put(cmd.aliases()[0], cmd.usage() + " - " + cmd.desc());
            }
            
            if (method.isAnnotationPresent(NestedCommand.class)) {
                NestedCommand nestedCmd = method.getAnnotation(NestedCommand.class);
                
                for (Class<?> nestedCls : nestedCmd.value()) {
                    registerMethods(nestedCls, method);
                }
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
        return commands.get(null).containsKey(command.toLowerCase());
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
     * @param args
     * @param level
     * @param cmd
     * @return
     */
    private String getUsage(String[] args, int level, Command cmd) {
        StringBuilder command = new StringBuilder();
        
        command.append("/");
        
        for (int i = 0; i <= level; i++) {
            command.append(args[i] + " ");
        }
        
        command.append(cmd.flags().length() > 0 ? "[-" + cmd.flags() + "] " : "");
        command.append(cmd.usage());
        
        return command.toString();
    }
    
    /**
     * Get the usage string for a nested command.
     * 
     * @param args
     * @param level
     * @param method
     * @param palyer
     * @return
     */
    private String getNestedUsage(String[] args, int level, Method method,
            LocalPlayer player) {
        StringBuilder command = new StringBuilder();
        
        command.append("/");
        
        for (int i = 0; i <= level; i++) {
            command.append(args[i] + " ");
        }

        
        Map<String, Method> map = commands.get(method);
        
        command.append("<");
        
        List<String> allowedCommands = new ArrayList<String>();
        
        for (Map.Entry<String, Method> entry : map.entrySet()) {
            Method childMethod = entry.getValue();
            
            if (hasPermission(childMethod, player)) {
                Command childCmd = childMethod.getAnnotation(Command.class);
                
                allowedCommands.add(childCmd.aliases()[0]);
            }
        }
        
        if (allowedCommands.size() > 0) {
            command.append(StringUtil.joinString(allowedCommands, "|", 0));
        } else {
            command.append("action");
        }
        
        command.append(">");
        
        return command.toString();
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
    public boolean execute(String[] args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException, Throwable {
        return executeMethod(null, args, we, session, player, editSession, 0);
    }
    
    /**
     * Attempt to execute a command.
     * 
     * @param parent
     * @param args
     * @param we
     * @param session
     * @param player
     * @param editSession
     * @param level
     * @return
     */
    public boolean executeMethod(Method parent, String[] args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession,
            int level) throws WorldEditException, Throwable {
        String cmdName = args[level];
        
        Map<String, Method> map = commands.get(parent);
        Method method = map.get(cmdName.toLowerCase());
        
        if (method == null) {
            if (parent == null) { // Root
                return false;
            } else {
                player.printError(getNestedUsage(args, level - 1, method, player));
                return true;
            }
        }
        
        if (!checkPermissions(method, player)) {
            return true;
        }
        
        int argsCount = args.length - 1 - level;

        if (method.isAnnotationPresent(NestedCommand.class)) {
            if (argsCount == 0) {
                player.printError(getNestedUsage(args, level, method, player));
                return true;
            } else {
                return executeMethod(method, args, we, session, player,
                        editSession, level + 1);
            }
        } else {
            Command cmd = method.getAnnotation(Command.class);
            
            String[] newArgs = new String[args.length - level];
            System.arraycopy(args, level, newArgs, 0, args.length - level);
            
            CommandContext context = new CommandContext(newArgs);
            
            if (context.argsLength() < cmd.min()) {
                player.printError("Too few arguments.");
                player.printError(getUsage(args, level, cmd));
                return true;
            }
            
            if (cmd.max() != -1 && context.argsLength() > cmd.max()) {
                player.printError("Too many arguments.");
                player.printError(getUsage(args, level, cmd));
                return true;
            }
            
            for (char flag : context.getFlags()) {
                if (cmd.flags().indexOf(String.valueOf(flag)) == -1) {
                    player.printError("Unknown flag: " + flag);
                    player.printError(getUsage(args, level, cmd));
                    return true;
                }
            }
            
            try {
                method.invoke(null, context, we, session, player, editSession);
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
        }
        
        return true;
    }
    
    /**
     * Checks permissions, prints an error if needed.
     * 
     * @param method
     * @param player
     * @return
     */
    private boolean checkPermissions(Method method, LocalPlayer player) {
        if (!method.isAnnotationPresent(CommandPermissions.class)) {
            return true;
        }

        CommandPermissions perms = method.getAnnotation(CommandPermissions.class);
        
        for (String perm : perms.value()) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        
        player.printError("You don't have permission for this command.");
        return false;
    }
    
    /**
     * Returns whether a player has access to a command.
     * 
     * @param method
     * @param player
     * @return
     */
    private boolean hasPermission(Method method, LocalPlayer player) {
        CommandPermissions perms = method.getAnnotation(CommandPermissions.class);
        if (perms == null) {
            return true;
        }
        
        for (String perm : perms.value()) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        
        return false;
    }
}
