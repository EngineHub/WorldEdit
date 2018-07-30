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

package com.sk89q.worldedit.util.command.composition;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;
import com.sk89q.worldedit.util.command.composition.FlagParser.FlagData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

public class FlagParser implements CommandExecutor<FlagData> {

    private final Map<Character, CommandExecutor<?>> flags = Maps.newHashMap();

    public <T> Flag<T> registerFlag(char flag, CommandExecutor<T> executor) {
        Flag<T> ret = new Flag<>(flag);
        flags.put(flag, executor);
        return ret;
    }

    @Override
    public FlagData call(CommandArgs args, CommandLocals locals) throws CommandException {
        Map<Character, Object> values = Maps.newHashMap();
        try {
            while (true) {
                String next = args.peek();
                if (next.equals("--")) {
                    args.next();
                    break;
                } else if (next.length() > 0 && next.charAt(0) == '-') {
                    args.next();

                    if (next.length() == 1) {
                        throw new CommandException("- must be followed by a flag (like -a), otherwise use -- before the - (i.e. /cmd -- - is a dash).");
                    } else {
                        for (int i = 1; i < next.length(); i++) {
                            char flag = next.charAt(i);
                            CommandExecutor<?> executor = flags.get(flag);
                            if (executor != null) {
                                values.put(flag, executor.call(args, locals));
                            } else {
                                throw new CommandException("Unknown flag: -" + flag + " (try one of -" + Joiner.on("").join(flags.keySet()) + " or put -- to skip flag parsing, i.e. /cmd -- -this begins with a dash).");
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (MissingArgumentException ignored) {
        }

        return new FlagData(values);
    }

    @Override
    public List<String> getSuggestions(CommandArgs args, CommandLocals locals) throws MissingArgumentException {
        List<String> suggestions = Collections.emptyList();

        while (true) {
            String next = args.peek();
            if (next.equals("--")) {
                args.next();
                break;
            } else if (next.length() > 0 && next.charAt(0) == '-') {
                args.next();

                if (!args.hasNext()) { // Completing -| or -???|
                    List<String> flagSuggestions = Lists.newArrayList();
                    for (Character flag : flags.keySet()) {
                        if (next.indexOf(flag) < 1) { // Don't add any flags that the user has entered
                            flagSuggestions.add(next + flag);
                        }
                    }
                    return flagSuggestions;
                } else { // Completing -??? ???|
                    for (int i = 1; i < next.length(); i++) {
                        char flag = next.charAt(i);
                        CommandExecutor<?> executor = flags.get(flag);
                        if (executor != null) {
                            suggestions = executor.getSuggestions(args, locals);
                        } else {
                            return suggestions;
                        }
                    }
                }
            } else {
                return suggestions;
            }
        }

        return suggestions;
    }

    @Override
    public String getUsage() {
        List<String> options = Lists.newArrayList();
        for (Entry<Character, CommandExecutor<?>> entry : flags.entrySet()) {
            String usage = entry.getValue().getUsage();
            options.add("[-" + entry.getKey() + (!usage.isEmpty() ? " " + usage : "") + "]");
        }
        return Joiner.on(" ").join(options);
    }

    @Override
    public String getDescription() {
        return "Read flags";
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        for (CommandExecutor<?> executor : flags.values()) {
            if (!executor.testPermission(locals)) {
                return false;
            }
        }

        return true;
    }

    public static class FlagData {
        private final Map<Character, Object> data;

        private FlagData(Map<Character, Object> data) {
            this.data = data;
        }

        public int size() {
            return data.size();
        }

        public boolean isEmpty() {
            return data.isEmpty();
        }

        public Object get(char key) {
            return data.get(key);
        }

        public boolean containsKey(char key) {
            return data.containsKey(key);
        }

    }

    public static final class Flag<T> {
        private final char flag;

        private Flag(char flag) {
            this.flag = flag;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public T get(FlagData data) {
            return (T) data.get(flag);
        }

        @SuppressWarnings("unchecked")
        public T get(FlagData data, T fallback) {
            T value = get(data);
            if (value == null) {
                return fallback;
            } else {
                return value;
            }
        }
    }

}
