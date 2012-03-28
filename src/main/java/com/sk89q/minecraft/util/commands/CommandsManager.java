// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.minecraft.util.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.util.StringUtil;

/**
 * <p>Manager for handling commands. This allows you to easily process commands,
 * including nested commands, by correctly annotating methods of a class.</p>
 *
 * <p>To use this, it is merely a matter of registering classes containing
 * the commands (as methods with the proper annotations) with the
 * manager. When you want to process a command, use one of the
 * <code>execute</code> methods. If something is wrong, such as incorrect
 * usage, insufficient permissions, or a missing command altogether, an
 * exception will be raised for upstream handling.</p>
 *
 * <p>Methods of a class to be registered can be static, but if an injector
 * is registered with the class, the instances of the command classes
 * will be created automatically and methods will be called non-statically.</p>
 *
 * <p>To mark a method as a command, use {@link Command}. For nested commands,
 * see {@link NestedCommand}. To handle permissions, use
 * {@link CommandPermissions}.</p>
 *
 * <p>This uses Java reflection extensively, but to reduce the overhead of
 * reflection, command lookups are completely cached on registration. This
 * allows for fast command handling. Method invocation still has to be done
 * with reflection, but this is quite fast in that of itself.</p>
 *
 * @author sk89q
 * @param <T> command sender class
 */
public abstract class CommandsManager<T> {
    /**
     * Logger for general errors.
     */
    protected static final Logger logger =
            Logger.getLogger(CommandsManager.class.getCanonicalName());

    /**
     * Mapping of commands (including aliases) with a description. Root
     * commands are stored under a key of null, whereas child commands are
     * cached under their respective {@link Method}. The child map has
     * the key of the command name (one for each alias) with the
     * method.
     */
    protected Map<Method, Map<String, Method>> commands = new HashMap<Method, Map<String, Method>>();

    /**
     * Used to store the instances associated with a method.
     */
    protected Map<Method, Object> instances = new HashMap<Method, Object>();

    /**
     * Mapping of commands (not including aliases) with a description. This
     * is only for top level commands.
     */
    protected Map<String, String> descs = new HashMap<String, String>();

    /**
     * Stores the injector used to getInstance.
     */
    protected Injector injector;

    /**
     * Mapping of commands (not including aliases) with a description. This
     * is only for top level commands.
     */
    protected Map<String, String> helpMessages = new HashMap<String, String>();

    /**
     * Register an class that contains commands (denoted by {@link Command}.
     * If no dependency injector is specified, then the methods of the
     * class will be registered to be called statically. Otherwise, new
     * instances will be created of the command classes and methods will
     * not be called statically.
     *
     * @param cls
     */
    public void register(Class<?> cls) {
        registerMethods(cls, null);
    }

    /**
     * Register an class that contains commands (denoted by {@link Command}.
     * If no dependency injector is specified, then the methods of the
     * class will be registered to be called statically. Otherwise, new
     * instances will be created of the command classes and methods will
     * not be called statically. A List of {@link Command} annotations from
     * registered commands is returned.
     *
     * @param cls
     * @return A List of {@link Command} annotations from registered commands,
     * for use in eg. a dynamic command registration system.
     */
    public List<Command> registerAndReturn(Class<?> cls) {
        return registerMethods(cls, null);
    }

    /**
     * Register the methods of a class. This will automatically construct
     * instances as necessary.
     *
     * @param cls
     * @param parent
     * @return Commands Registered
     */
    private List<Command> registerMethods(Class<?> cls, Method parent) {
        try {
            if (getInjector() == null) {
                return registerMethods(cls, parent, null);
            } else {
                Object obj = getInjector().getInstance(cls);
                return registerMethods(cls, parent, obj);
            }
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Failed to register commands", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to register commands", e);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Failed to register commands", e);
        }
        return null;
    }

    /**
     * Register the methods of a class.
     *
     * @param cls
     * @param parent
     * @param obj
     * @return
     */
    private List<Command> registerMethods(Class<?> cls, Method parent, Object obj) {
        Map<String, Method> map;
        List<Command> registered = new ArrayList<Command>();

        // Make a new hash map to cache the commands for this class
        // as looking up methods via reflection is fairly slow
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

            boolean isStatic = Modifier.isStatic(method.getModifiers());

            Command cmd = method.getAnnotation(Command.class);

            // Cache the aliases too
            for (String alias : cmd.aliases()) {
                map.put(alias, method);
            }

            // We want to be able invoke with an instance
            if (!isStatic) {
                // Can't register this command if we don't have an instance
                if (obj == null) {
                    continue;
                }

                instances.put(method, obj);
            }

            // Build a list of commands and their usage details, at least for
            // root level commands
            if (parent == null) {
                final String commandName = cmd.aliases()[0];
                final String desc = cmd.desc();

                final String usage = cmd.usage();
                if (usage.length() == 0) {
                    descs.put(commandName, desc);
                } else {
                    descs.put(commandName, usage + " - " + desc);
                }

                String help = cmd.help();
                if (help.length() == 0) {
                    help = desc;
                }

                final CharSequence arguments = getArguments(cmd);
                for (String alias : cmd.aliases()) {
                    final String helpMessage = "/" + alias + " " + arguments + "\n\n" + help;
                    final String key = alias.replaceAll("/", "");
                    String previous = helpMessages.put(key, helpMessage);

                    if (previous != null && !previous.replaceAll("^/[^ ]+ ", "").equals(helpMessage.replaceAll("^/[^ ]+ ", ""))) {
                        helpMessages.put(key, previous + "\n\n" + helpMessage);
                    }
                }

            }

            // Add the command to the registered command list for return
            registered.add(cmd);

            // Look for nested commands -- if there are any, those have
            // to be cached too so that they can be quickly looked
            // up when processing commands
            if (method.isAnnotationPresent(NestedCommand.class)) {
                NestedCommand nestedCmd = method.getAnnotation(NestedCommand.class);

                for (Class<?> nestedCls : nestedCmd.value()) {
                    registerMethods(nestedCls, method);
                }
            }
        }

        if (cls.getSuperclass() != null) {
            registerMethods(cls.getSuperclass(), parent, obj);
        }

        return registered;
    }

    /**
     * Checks to see whether there is a command named such at the root level.
     * This will check aliases as well.
     *
     * @param command
     * @return
     */
    public boolean hasCommand(String command) {
        return commands.get(null).containsKey(command.toLowerCase());
    }

    /**
     * Get a list of command descriptions. This is only for root commands.
     *
     * @return
     */
    public Map<String, String> getCommands() {
        return descs;
    }

    public Map<Method, Map<String, Method>> getMethods() {
        return commands;
    }

    /**
     * Get a map from command name to help message. This is only for root commands.
     *
     * @return
     */
    public Map<String, String> getHelpMessages() {
        return helpMessages;
    }

    /**
     * Get the usage string for a command.
     *
     * @param args
     * @param level
     * @param cmd
     * @return
     */
    protected String getUsage(String[] args, int level, Command cmd) {
        final StringBuilder command = new StringBuilder();

        command.append('/');

        for (int i = 0; i <= level; ++i) {
            command.append(args[i]);
            command.append(' ');
        }
        command.append(getArguments(cmd));

        final String help = cmd.help();
        if (help.length() > 0) {
            command.append("\n\n");
            command.append(help);
        }

        return command.toString();
    }

    protected CharSequence getArguments(Command cmd) {
        final String flags = cmd.flags();

        final StringBuilder command2 = new StringBuilder();
        if (flags.length() > 0) {
            String flagString = flags.replaceAll(".:", "");
            if (flagString.length() > 0) {
                command2.append("[-");
                for (int i = 0; i < flagString.length(); ++i) {
                    command2.append(flagString.charAt(i));
                }
                command2.append("] ");
            }
        }

        command2.append(cmd.usage());

        return command2;
    }

    /**
     * Get the usage string for a nested command.
     *
     * @param args
     * @param level
     * @param method
     * @param player
     * @return
     * @throws CommandException
     */
    protected String getNestedUsage(String[] args, int level,
            Method method, T player) throws CommandException {

        StringBuilder command = new StringBuilder();

        command.append("/");

        for (int i = 0; i <= level; ++i) {
            command.append(args[i] + " ");
        }

        Map<String, Method> map = commands.get(method);
        boolean found = false;

        command.append("<");

        Set<String> allowedCommands = new HashSet<String>();

        for (Map.Entry<String, Method> entry : map.entrySet()) {
            Method childMethod = entry.getValue();
            found = true;

            if (hasPermission(childMethod, player)) {
                Command childCmd = childMethod.getAnnotation(Command.class);

                allowedCommands.add(childCmd.aliases()[0]);
            }
        }

        if (allowedCommands.size() > 0) {
            command.append(StringUtil.joinString(allowedCommands, "|", 0));
        } else {
            if (!found) {
                command.append("?");
            } else {
                //command.append("action");
                throw new CommandPermissionsException();
            }
        }

        command.append(">");

        return command.toString();
    }

    /**
     * Attempt to execute a command. This version takes a separate command
     * name (for the root command) and then a list of following arguments.
     *
     * @param cmd command to run
     * @param args arguments
     * @param player command source
     * @param methodArgs method arguments
     * @throws CommandException
     */
    public void execute(String cmd, String[] args, T player,
            Object... methodArgs) throws CommandException {

        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = cmd;
        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);

        executeMethod(null, newArgs, player, newMethodArgs, 0);
    }

    /**
     * Attempt to execute a command.
     *
     * @param args
     * @param player
     * @param methodArgs
     * @throws CommandException
     */
    public void execute(String[] args, T player,
            Object... methodArgs) throws CommandException {

        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);
        executeMethod(null, args, player, newMethodArgs, 0);
    }

    /**
     * Attempt to execute a command.
     *
     * @param parent
     * @param args
     * @param player
     * @param methodArgs
     * @param level
     * @throws CommandException
     */
    public void executeMethod(Method parent, String[] args,
            T player, Object[] methodArgs, int level) throws CommandException {

        String cmdName = args[level];

        Map<String, Method> map = commands.get(parent);
        Method method = map.get(cmdName.toLowerCase());

        if (method == null) {
            if (parent == null) { // Root
                throw new UnhandledCommandException();
            } else {
                throw new MissingNestedCommandException("Unknown command: " + cmdName,
                        getNestedUsage(args, level - 1, parent, player));
            }
        }

        checkPermission(player, method);

        int argsCount = args.length - 1 - level;

        if (method.isAnnotationPresent(NestedCommand.class)) {
            if (argsCount == 0) {
                throw new MissingNestedCommandException("Sub-command required.",
                        getNestedUsage(args, level, method, player));
            } else {
                executeMethod(method, args, player, methodArgs, level + 1);
            }
        } else if (method.isAnnotationPresent(CommandAlias.class)) {
            CommandAlias aCmd = method.getAnnotation(CommandAlias.class);
            executeMethod(parent, aCmd.value(), player, methodArgs, level);
        } else {
            Command cmd = method.getAnnotation(Command.class);

            String[] newArgs = new String[args.length - level];
            System.arraycopy(args, level, newArgs, 0, args.length - level);

            final Set<Character> valueFlags = new HashSet<Character>();

            char[] flags = cmd.flags().toCharArray();
            Set<Character> newFlags = new HashSet<Character>();
            for (int i = 0; i < flags.length; ++i) {
                if (flags.length > i + 1 && flags[i + 1] == ':') {
                    valueFlags.add(flags[i]);
                    ++i;
                }
                newFlags.add(flags[i]);
            }

            CommandContext context = new CommandContext(newArgs, valueFlags);

            if (context.argsLength() < cmd.min()) {
                throw new CommandUsageException("Too few arguments.", getUsage(args, level, cmd));
            }

            if (cmd.max() != -1 && context.argsLength() > cmd.max()) {
                throw new CommandUsageException("Too many arguments.", getUsage(args, level, cmd));
            }

            if (!cmd.anyFlags()) {
                for (char flag : context.getFlags()) {
                    if (!newFlags.contains(flag)) {
                        throw new CommandUsageException("Unknown flag: " + flag, getUsage(args, level, cmd));
                    }
                }
            }

            methodArgs[0] = context;

            Object instance = instances.get(method);

            invokeMethod(parent, args, player, method, instance, methodArgs, argsCount);
        }
    }

    protected void checkPermission(T player, Method method) throws CommandException {
        if (!hasPermission(method, player)) {
            throw new CommandPermissionsException();
        }
    }

    public void invokeMethod(Method parent, String[] args, T player, Method method,
            Object instance, Object[] methodArgs, int level) throws CommandException {
        try {
            method.invoke(instance, methodArgs);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Failed to execute command", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to execute command", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException) {
                throw (CommandException) e.getCause();
            }

            throw new WrappedCommandException(e.getCause());
        }
    }

    /**
     * Returns whether a player has access to a command.
     *
     * @param method
     * @param player
     * @return
     */
    protected boolean hasPermission(Method method, T player) {
        CommandPermissions perms = method.getAnnotation(CommandPermissions.class);
        if (perms == null) {
            return true;
        }

        for (String perm : perms.value()) {
            if (hasPermission(player, perm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether a player permission..
     *
     * @param player
     * @param perm
     * @return
     */
    public abstract boolean hasPermission(T player, String perm);

    /**
     * Get the injector used to create new instances. This can be
     * null, in which case only classes will be registered statically.
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * Set the injector for creating new instances.
     *
     * @param injector injector or null
     */
    public void setInjector(Injector injector) {
        this.injector = injector;
    }
}
