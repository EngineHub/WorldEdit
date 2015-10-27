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

package com.sk89q.worldedit.util.command.argument;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CommandArgs {

    private final List<String> arguments;
    private final Map<Character, String> flags;
    private int position = 0;
    private Set<Character> consumedFlags = Sets.newHashSet();

    public CommandArgs(List<String> arguments, Map<Character, String> flags) {
        this.arguments = arguments;
        this.flags = Maps.newHashMap(flags);
    }

    public boolean hasNext() {
        return position < arguments.size();
    }

    public String next() throws MissingArgumentException {
        try {
            return arguments.get(position++);
        } catch (IndexOutOfBoundsException ignored) {
            throw new MissingArgumentException("Too few arguments specified.");
        }
    }

    public String peek() throws MissingArgumentException {
        try {
            return arguments.get(position);
        } catch (IndexOutOfBoundsException ignored) {
            throw new MissingArgumentException("Too few arguments specified.");
        }
    }

    public String remaining() throws MissingArgumentException {
        if (hasNext()) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            while (hasNext()) {
                if (!first) {
                    builder.append(" ");
                }
                builder.append(next());
                first = false;
            }
            return builder.toString();
        } else {
            throw new MissingArgumentException("Too few arguments specified.");
        }
    }

    public String peekRemaining() throws MissingArgumentException {
        if (hasNext()) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            while (hasNext()) {
                if (!first) {
                    builder.append(" ");
                }
                builder.append(next());
                first = false;
            }
            return builder.toString();
        } else {
            throw new MissingArgumentException();
        }
    }

    public int position() {
        return position;
    }

    public int size() {
        return arguments.size();
    }

    public void markConsumed() {
        position = arguments.size();
    }

    public CommandArgs split() {
        return new CommandArgs(getUnusedArguments(), getUnusedFlags());
    }

    private List<String> getUnusedArguments() {
        List<String> args = Lists.newArrayList();
        while (position < arguments.size()) {
            args.add(arguments.get(position++));
        }
        return args;
    }

    private Map<Character, String> getUnusedFlags() {
        Map<Character, String> flags = Maps.newHashMap();
        for (Entry<Character, String> entry : this.flags.entrySet()) {
            if (!consumedFlags.contains(entry.getKey())) {
                flags.put(entry.getKey(), entry.getValue());
                consumedFlags.add(entry.getKey());
            }
        }
        return flags;
    }

    public void requireAllConsumed() throws UnusedArgumentsException {
        StringBuilder builder = new StringBuilder();
        boolean hasUnconsumed = false;

        if (flags.size() > consumedFlags.size()) {
            hasUnconsumed = true;
            for (Entry<Character, String> entry : flags.entrySet()) {
                if (!consumedFlags.contains(entry.getKey())) {
                    builder.append("-").append(entry.getKey()).append(" ");
                    if (entry.getValue() != null) {
                        builder.append(entry.getValue()).append(" ");
                    }
                }
            }
        }

        if (hasNext()) {
            hasUnconsumed = true;
            try {
                builder.append(peekRemaining());
            } catch (MissingArgumentException e) {
                throw new RuntimeException("This should not have happened", e);
            }
        }

        if (hasUnconsumed) {
            throw new UnusedArgumentsException("There were unused arguments: " + builder);
        }
    }

    public int nextInt() throws ArgumentParseException, MissingArgumentException {
        String next = next();
        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number, got '" + next + "'");
        }
    }

    public short nextShort() throws ArgumentParseException, MissingArgumentException {
        String next = next();
        try {
            return Short.parseShort(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number, got '" + next + "'");
        }
    }

    public byte nextByte() throws ArgumentParseException, MissingArgumentException {
        String next = next();
        try {
            return Byte.parseByte(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number, got '" + next + "'");
        }
    }

    public double nextDouble() throws ArgumentParseException, MissingArgumentException {
        String next = next();
        try {
            return Double.parseDouble(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number, got '" + next + "'");
        }
    }

    public float nextFloat() throws ArgumentParseException, MissingArgumentException {
        String next = next();
        try {
            return Float.parseFloat(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number, got '" + next + "'");
        }
    }

    public boolean nextBoolean() throws ArgumentParseException, MissingArgumentException {
        String next = next();
        if (next.equalsIgnoreCase("yes") || next.equalsIgnoreCase("true") || next.equalsIgnoreCase("y") || next.equalsIgnoreCase("1")) {
            return true;
        } else if (next.equalsIgnoreCase("no") || next.equalsIgnoreCase("false") || next.equalsIgnoreCase("n") || next.equalsIgnoreCase("0")) {
            return false;
        } else {
            throw new ArgumentParseException("Expected a boolean (yes/no), got '" + next + "'");
        }
    }

    public boolean containsFlag(char c) {
        boolean contains = flags.containsKey(c);
        if (contains) {
            consumedFlags.add(c);
        }
        return contains;
    }

    public String getFlag(char c, String fallback) {
        boolean contains = flags.containsKey(c);
        if (contains) {
            consumedFlags.add(c);
            String value = flags.get(c);
            if (value == null) {
                return fallback;
            } else {
                return value;
            }
        }
        return fallback;
    }

    public int getIntFlag(char c, int fallback) throws ArgumentParseException {
        String next = getFlag(c, String.valueOf(fallback));
        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number for flag '-" + c + "', got '" + next + "'");
        }
    }

    public short getShortFlag(char c, short fallback) throws ArgumentParseException {
        String next = getFlag(c, String.valueOf(fallback));
        try {
            return Short.parseShort(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number for flag '-" + c + "', got '" + next + "'");
        }
    }

    public byte getByteFlag(char c, byte fallback) throws ArgumentParseException {
        String next = getFlag(c, String.valueOf(fallback));
        try {
            return Byte.parseByte(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number for flag '-" + c + "', got '" + next + "'");
        }
    }

    public double getDoubleFlag(char c, double fallback) throws ArgumentParseException {
        String next = getFlag(c, String.valueOf(fallback));
        try {
            return Double.parseDouble(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number for flag '-" + c + "', got '" + next + "'");
        }
    }

    public float getFloatFlag(char c, float fallback) throws ArgumentParseException {
        String next = getFlag(c, String.valueOf(fallback));
        try {
            return Float.parseFloat(next);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Expected a number for flag '-" + c + "', got '" + next + "'");
        }
    }

    public boolean getBooleanFlag(char c, boolean fallback) throws ArgumentParseException {
        String next = getFlag(c, String.valueOf(fallback));
        if (next.equalsIgnoreCase("yes") || next.equalsIgnoreCase("true") || next.equalsIgnoreCase("y") || next.equalsIgnoreCase("1")) {
            return true;
        } else if (next.equalsIgnoreCase("no") || next.equalsIgnoreCase("false") || next.equalsIgnoreCase("n") || next.equalsIgnoreCase("0")) {
            return false;
        } else {
            throw new ArgumentParseException("Expected a boolean (yes/no), got '" + next + "'");
        }
    }

    public static class Parser {
        private boolean parseFlags = true;
        private boolean usingHangingArguments = false;
        private Set<Character> valueFlags = Sets.newHashSet();

        public boolean isParseFlags() {
            return parseFlags;
        }

        public Parser setParseFlags(boolean parseFlags) {
            this.parseFlags = parseFlags;
            return this;
        }

        public boolean isUsingHangingArguments() {
            return usingHangingArguments;
        }

        public Parser setUsingHangingArguments(boolean usingHangingArguments) {
            this.usingHangingArguments = usingHangingArguments;
            return this;
        }

        public Set<Character> getValueFlags() {
            return valueFlags;
        }

        public Parser setValueFlags(Set<Character> valueFlags) {
            this.valueFlags = valueFlags;
            return this;
        }

        public CommandArgs parse(String arguments) throws CommandException {
            CommandContext context = new CommandContext(CommandContext.split("_ " + arguments), valueFlags, false, null, parseFlags);
            List<String> args = Lists.newArrayList();
            for (int i = 0; i < context.argsLength(); i++) {
                args.add(context.getString(i));
            }
            if (isUsingHangingArguments()) {
                if (arguments.isEmpty() || arguments.endsWith(" ")) {
                    args.add("");
                }
            }
            Map<Character, String> flags = Maps.newHashMap(context.getValueFlags());
            for (Character c : context.getFlags()) {
                flags.put(c, null);
            }
            return new CommandArgs(args, flags);
        }
    }
}
