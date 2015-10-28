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
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;

import java.util.Collections;
import java.util.List;

public class CommandArgs {

    private final List<String> arguments;
    private int position = 0;

    public CommandArgs(List<String> arguments) {
        this.arguments = arguments;
    }

    public CommandArgs(CommandArgs args) {
        this(Lists.newArrayList(args.arguments));
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

    public String uncheckedNext() {
        if (hasNext()) {
            return arguments.get(position);
        } else {
            return null;
        }
    }

    public String peek() throws MissingArgumentException {
        try {
            return arguments.get(position);
        } catch (IndexOutOfBoundsException ignored) {
            throw new MissingArgumentException("Too few arguments specified.");
        }
    }

    public String uncheckedPeek() {
        if (hasNext()) {
            return arguments.get(position);
        } else {
            return null;
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

    public void requireAllConsumed() throws UnusedArgumentsException {
        if (hasNext()) {
            StringBuilder builder = new StringBuilder();
            try {
                builder.append(peekRemaining());
            } catch (MissingArgumentException e) {
                throw new RuntimeException("This should not have happened", e);
            }
            throw new UnusedArgumentsException("There were unused arguments: " + builder);
        }
    }

    public static class Parser {
        private boolean usingHangingArguments = false;

        public boolean isUsingHangingArguments() {
            return usingHangingArguments;
        }

        public Parser setUsingHangingArguments(boolean usingHangingArguments) {
            this.usingHangingArguments = usingHangingArguments;
            return this;
        }

        public CommandArgs parse(String arguments) throws CommandException {
            CommandContext context = new CommandContext(CommandContext.split("_ " + arguments), Collections.<Character>emptySet(), false, null, false);
            List<String> args = Lists.newArrayList();
            for (int i = 0; i < context.argsLength(); i++) {
                args.add(context.getString(i));
            }
            if (isUsingHangingArguments()) {
                if (arguments.isEmpty() || arguments.endsWith(" ")) {
                    args.add("");
                }
            }
            return new CommandArgs(args);
        }
    }
}
