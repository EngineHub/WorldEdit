// $Id$
/*
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

package com.sk89q.minecraft.util.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandContext {
    protected static final String QUOTE_CHARS = "\'\"";
    protected final String[] args;
    protected final Set<Character> booleanFlags = new HashSet<Character>();
    protected final Map<Character, String> valueFlags = new HashMap<Character, String>();

    public CommandContext(String args) {
        this(args.split(" "));
    }

    public CommandContext(String[] args) {
        char quotedChar;
        for (int i = 1; i < args.length; ++i) {
            if (args[i].length() == 0) {
                args = removePortionOfArray(args, i, i, null);
            } else if (QUOTE_CHARS.indexOf(String.valueOf(args[i].charAt(0))) != -1) {
                StringBuilder build = new StringBuilder();
                quotedChar = args[i].charAt(0);
                int endIndex = i;
                for (; endIndex < args.length; endIndex++) {
                    if (args[endIndex].charAt(args[endIndex].length() - 1) == quotedChar) {
                        if (endIndex != i) build.append(" ");
                        build.append(args[endIndex].substring(endIndex == i ? 1 : 0, args[endIndex].length() - 1));
                        break;
                    } else if (endIndex == i) {
                        build.append(args[endIndex].substring(1));
                    } else {
                        build.append(" ").append(args[endIndex]);
                    }
                }
                args = removePortionOfArray(args, i, endIndex, build.toString());
            } else if (args[i].charAt(0) == '-' && args[i].matches("^-[a-zA-Z]+$")) {
                for (int k = 1; k < args[i].length(); ++k) {
                    booleanFlags.add(args[i].charAt(k));
                }
                args = removePortionOfArray(args, i, i, null);
            }
        }
        this.args = args;
    }

    public CommandContext(String args, Set<Character> isValueFlag) throws CommandException {
        this(args.split(" "), isValueFlag);
    }

    /**
     * @param args An array with arguments empty strings will be ignored by most things
     * @param isValueFlag A set containing all value flags. Pass null to disable flag parsing altogether.
     * @throws CommandException This is thrown if a value flag was passed without a value.
     */
    public CommandContext(String[] args, Set<Character> isValueFlag) throws CommandException {
        if (isValueFlag == null) {
            this.args = args;
            return;
        }
        
        int nextArg = 1;

        while (nextArg < args.length) {
            // Fetch argument
            String arg = args[nextArg++];

            // Empty argument? (multiple consecutive spaces)
            if (arg.isEmpty())
                continue;

            // No more flags?
            if (arg.charAt(0) != '-' || arg.length() == 1 || !arg.matches("^-[a-zA-Z]+$")) {
                --nextArg;
                break;
            }

            // Handle flag parsing terminator --
            if (arg.equals("--"))
                break;

            // Go through the flags
            for (int i = 1; i < arg.length(); ++i) {
                char flagName = arg.charAt(i);

                if (isValueFlag.contains(flagName)) {
                    // Skip empty arguments...
                    while (nextArg < args.length && args[nextArg].isEmpty())
                        ++nextArg;

                    if (nextArg >= args.length)
                        throw new CommandException("No value specified for the '-"+flagName+"' flag.");

                    // If it is a value flag, read another argument and add it
                    valueFlags.put(flagName, args[nextArg++]);
                }
                else {
                    booleanFlags.add(flagName);
                }
            }
        }

        this.args = Arrays.copyOfRange(args, nextArg-1, args.length);
        this.args[0] = args[0];
    }

    public String getCommand() {
        return args[0];
    }

    public boolean matches(String command) {
        return args[0].equalsIgnoreCase(command);
    }

    public String getString(int index) {
        return args[index + 1];
    }

    public String getString(int index, String def) {
        return index + 1 < args.length ? args[index + 1] : def;
    }

    public String getJoinedStrings(int initialIndex) {
        initialIndex = initialIndex + 1;
        StringBuilder buffer = new StringBuilder(args[initialIndex]);
        for (int i = initialIndex + 1; i < args.length; ++i) {
            buffer.append(" ").append(args[i]);
        }
        return buffer.toString();
    }

    public int getInteger(int index) throws NumberFormatException {
        return Integer.parseInt(args[index + 1]);
    }

    public int getInteger(int index, int def) throws NumberFormatException {
        return index + 1 < args.length ? Integer.parseInt(args[index + 1]) : def;
    }

    public double getDouble(int index) throws NumberFormatException {
        return Double.parseDouble(args[index + 1]);
    }

    public double getDouble(int index, double def) throws NumberFormatException {
        return index + 1 < args.length ? Double.parseDouble(args[index + 1]) : def;
    }

    public String[] getSlice(int index) {
        String[] slice = new String[args.length - index];
        System.arraycopy(args, index, slice, 0, args.length - index);
        return slice;
    }

    public String[] getPaddedSlice(int index, int padding) {
        String[] slice = new String[args.length - index + padding];
        System.arraycopy(args, index, slice, padding, args.length - index);
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
        if (value == null)
            return def;

        return value;
    }

    public int getFlagInteger(char ch) throws NumberFormatException {
        return Integer.parseInt(valueFlags.get(ch));
    }

    public int getFlagInteger(char ch, int def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null)
            return def;

        return Integer.parseInt(value);
    }

    public double getFlagDouble(char ch) throws NumberFormatException {
        return Double.parseDouble(valueFlags.get(ch));
    }

    public double getFlagDouble(char ch, double def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null)
            return def;

        return Double.parseDouble(value);
    }

    public int length() {
        return args.length;
    }

    public int argsLength() {
        return args.length - 1;
    }

    public static String[] removePortionOfArray(String[] array, int from, int to, String replace) {
        String[] newArray = new String[from + array.length - to - (replace == null ? 1 : 0)];
        System.arraycopy(array, 0, newArray, 0, from);
        if (replace != null) newArray[from] = replace;
        System.arraycopy(array, to + (replace == null ? 0 : 1), newArray, from + (replace == null ? 0 : 1),
                    array.length - to - 1);
        return newArray;
    }
}
