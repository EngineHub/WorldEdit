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

package com.sk89q.minecraft.util.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandContext {
    
    protected final String command;
    protected final List<String> parsedArgs;

    protected final List<Integer> originalArgIndices;
    protected final String[] originalArgs;
    protected final Set<Character> booleanFlags = new HashSet<>();
    protected final Map<Character, String> valueFlags = new HashMap<>();
    protected final SuggestionContext suggestionContext;
    protected final CommandLocals locals;

    public static String[] split(String args) {
        return args.split(" ", -1);
    }

    public CommandContext(String args) throws CommandException {
        this(args.split(" ", -1), null);
    }

    public CommandContext(String[] args) throws CommandException {
        this(args, null);
    }

    public CommandContext(String args, Set<Character> valueFlags) throws CommandException {
        this(args.split(" ", -1), valueFlags);
    }

    public CommandContext(String args, Set<Character> valueFlags, boolean allowHangingFlag) 
            throws CommandException {
        this(args.split(" ", -1), valueFlags, allowHangingFlag, new CommandLocals());
    }

    public CommandContext(String[] args, Set<Character> valueFlags) throws CommandException {
        this(args, valueFlags, false, null);
    }

    /**
     * Parse the given array of arguments.
     *
     * <p>Empty arguments are removed from the list of arguments.</p>
     *
     * @param args an array with arguments
     * @param valueFlags a set containing all value flags (pass null to disable value flag parsing)
     * @param allowHangingFlag true if hanging flags are allowed
     * @param locals the locals, null to create empty one
     * @throws CommandException thrown on a parsing error
     */
    public CommandContext(String[] args, Set<Character> valueFlags, boolean allowHangingFlag, CommandLocals locals) throws CommandException {
        this(args, valueFlags, allowHangingFlag, locals, true);
    }

    /**
     * Parse the given array of arguments.
     *
     * <p>Empty arguments are removed from the list of arguments.</p>
     *
     * @param args an array with arguments
     * @param valueFlags a set containing all value flags (pass null to disable value flag parsing)
     * @param allowHangingFlag true if hanging flags are allowed
     * @param locals the locals, null to create empty one
     * @param parseFlags where to parse flags
     * @throws CommandException thrown on a parsing error
     */
    public CommandContext(String[] args, Set<Character> valueFlags, boolean allowHangingFlag, CommandLocals locals, boolean parseFlags) throws CommandException {
        if (valueFlags == null) {
            valueFlags = Collections.emptySet();
        }

        originalArgs = args;
        command = args[0];
        this.locals = locals != null ? locals : new CommandLocals();
        boolean isHanging = false;
        SuggestionContext suggestionContext = SuggestionContext.hangingValue();

        // Eliminate empty args and combine multiword args first
        List<Integer> argIndexList = new ArrayList<>(args.length);
        List<String> argList = new ArrayList<>(args.length);
        for (int i = 1; i < args.length; ++i) {
            isHanging = false;
            
            String arg = args[i];
            if (arg.isEmpty()) {
                isHanging = true;
                continue;
            }

            argIndexList.add(i);

            switch (arg.charAt(0)) {
            case '\'':
            case '"':
                final StringBuilder build = new StringBuilder();
                final char quotedChar = arg.charAt(0);

                int endIndex;
                for (endIndex = i; endIndex < args.length; ++endIndex) {
                    final String arg2 = args[endIndex];
                    if (arg2.charAt(arg2.length() - 1) == quotedChar && arg2.length() > 1) {
                        if (endIndex != i) build.append(' ');
                        build.append(arg2.substring(endIndex == i ? 1 : 0, arg2.length() - 1));
                        break;
                    } else if (endIndex == i) {
                        build.append(arg2.substring(1));
                    } else {
                        build.append(' ').append(arg2);
                    }
                }

                if (endIndex < args.length) {
                    arg = build.toString();
                    i = endIndex;
                }

                // In case there is an empty quoted string
                if (arg.isEmpty()) {
                    continue;
                }
                // else raise exception about hanging quotes?
            }
            argList.add(arg);
        }

        // Then flags

        this.originalArgIndices = new ArrayList<>(argIndexList.size());
        this.parsedArgs = new ArrayList<>(argList.size());

        if (parseFlags) {
            for (int nextArg = 0; nextArg < argList.size(); ) {
                // Fetch argument
                String arg = argList.get(nextArg++);
                suggestionContext = SuggestionContext.hangingValue();

                // Not a flag?
                if (arg.charAt(0) != '-' || arg.length() == 1 || !arg.matches("^-[a-zA-Z\\?]+$")) {
                    if (!isHanging) {
                        suggestionContext = SuggestionContext.lastValue();
                    }

                    originalArgIndices.add(argIndexList.get(nextArg - 1));
                    parsedArgs.add(arg);
                    continue;
                }

                // Handle flag parsing terminator --
                if (arg.equals("--")) {
                    while (nextArg < argList.size()) {
                        originalArgIndices.add(argIndexList.get(nextArg));
                        parsedArgs.add(argList.get(nextArg++));
                    }
                    break;
                }

                // Go through the flag characters
                for (int i = 1; i < arg.length(); ++i) {
                    char flagName = arg.charAt(i);

                    if (valueFlags.contains(flagName)) {
                        if (this.valueFlags.containsKey(flagName)) {
                            throw new CommandException("Value flag '" + flagName + "' already given");
                        }

                        if (nextArg >= argList.size()) {
                            if (allowHangingFlag) {
                                suggestionContext = SuggestionContext.flag(flagName);
                                break;
                            } else {
                                throw new CommandException("No value specified for the '-" + flagName + "' flag.");
                            }
                        }

                        // If it is a value flag, read another argument and add it
                        this.valueFlags.put(flagName, argList.get(nextArg++));
                        if (!isHanging) {
                            suggestionContext = SuggestionContext.flag(flagName);
                        }
                    } else {
                        booleanFlags.add(flagName);
                    }
                }
            }
        } else {
            for (int i = 0; i < argList.size(); i++) {
                String arg = argList.get(i);
                originalArgIndices.add(argIndexList.get(i));
                parsedArgs.add(arg);
            }
        }
        
        this.suggestionContext = suggestionContext;
    }

    public SuggestionContext getSuggestionContext() {
        return suggestionContext;
    }

    public String getCommand() {
        return command;
    }

    public boolean matches(String command) {
        return this.command.equalsIgnoreCase(command);
    }

    public String getString(int index) {
        return parsedArgs.get(index);
    }

    public String getString(int index, String def) {
        return index < parsedArgs.size() ? parsedArgs.get(index) : def;
    }

    public String getJoinedStrings(int initialIndex) {
        initialIndex = originalArgIndices.get(initialIndex);
        StringBuilder buffer = new StringBuilder(originalArgs[initialIndex]);
        for (int i = initialIndex + 1; i < originalArgs.length; ++i) {
            buffer.append(" ").append(originalArgs[i]);
        }
        return buffer.toString();
    }
    
    public String getRemainingString(int start) {
        return getString(start, parsedArgs.size() - 1);
    }

    public String getString(int start, int end) {
        StringBuilder buffer = new StringBuilder(parsedArgs.get(start));
        for (int i = start + 1; i < end + 1; ++i) {
            buffer.append(" ").append(parsedArgs.get(i));
        }
        return buffer.toString();
    }

    public int getInteger(int index) throws NumberFormatException {
        return Integer.parseInt(parsedArgs.get(index));
    }

    public int getInteger(int index, int def) throws NumberFormatException {
        return index < parsedArgs.size() ? Integer.parseInt(parsedArgs.get(index)) : def;
    }

    public double getDouble(int index) throws NumberFormatException {
        return Double.parseDouble(parsedArgs.get(index));
    }

    public double getDouble(int index, double def) throws NumberFormatException {
        return index < parsedArgs.size() ? Double.parseDouble(parsedArgs.get(index)) : def;
    }

    public String[] getSlice(int index) {
        String[] slice = new String[originalArgs.length - index];
        System.arraycopy(originalArgs, index, slice, 0, originalArgs.length - index);
        return slice;
    }

    public String[] getPaddedSlice(int index, int padding) {
        String[] slice = new String[originalArgs.length - index + padding];
        System.arraycopy(originalArgs, index, slice, padding, originalArgs.length - index);
        return slice;
    }

    public String[] getParsedSlice(int index) {
        String[] slice = new String[parsedArgs.size() - index];
        System.arraycopy(parsedArgs.toArray(new String[parsedArgs.size()]), index, slice, 0, parsedArgs.size() - index);
        return slice;
    }

    public String[] getParsedPaddedSlice(int index, int padding) {
        String[] slice = new String[parsedArgs.size() - index + padding];
        System.arraycopy(parsedArgs.toArray(new String[parsedArgs.size()]), index, slice, padding, parsedArgs.size() - index);
        return slice;
    }

    public boolean hasFlag(char ch) {
        return booleanFlags.contains(ch) || valueFlags.containsKey(ch);
    }

    public Set<Character> getFlags() {
        return booleanFlags;
    }

    public Map<Character, String> getValueFlags() {
        return valueFlags;
    }

    public String getFlag(char ch) {
        return valueFlags.get(ch);
    }

    public String getFlag(char ch, String def) {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return value;
    }

    public int getFlagInteger(char ch) throws NumberFormatException {
        return Integer.parseInt(valueFlags.get(ch));
    }

    public int getFlagInteger(char ch, int def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Integer.parseInt(value);
    }

    public double getFlagDouble(char ch) throws NumberFormatException {
        return Double.parseDouble(valueFlags.get(ch));
    }

    public double getFlagDouble(char ch, double def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Double.parseDouble(value);
    }

    public int argsLength() {
        return parsedArgs.size();
    }

    public CommandLocals getLocals() {
        return locals;
    }
}
