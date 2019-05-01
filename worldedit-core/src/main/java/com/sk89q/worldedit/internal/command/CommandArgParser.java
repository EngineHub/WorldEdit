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

package com.sk89q.worldedit.internal.command;

import java.util.stream.Stream;

public class CommandArgParser {

    private enum State {
        NORMAL,
        QUOTE
    }

    private final Stream.Builder<String> args = Stream.builder();
    private final StringBuilder currentArg = new StringBuilder();
    private final String input;
    private int index = 0;
    private State state = State.NORMAL;

    public CommandArgParser(String input) {
        this.input = input;
    }

    public Stream<String> parseArgs() {
        for (; index < input.length(); index++) {
            char c = input.charAt(index);
            switch (state) {
                case NORMAL:
                    handleNormal(c);
                    break;
                case QUOTE:
                    handleQuote(c);
            }
        }
        finishArg(true);
        return args.build();
    }

    private void handleNormal(char c) {
        switch (c) {
            case '"':
                state = State.QUOTE;
                break;
            case ' ':
                finishArg(true);
                break;
            case '\\':
                if (index + 1 < input.length()) {
                    index++;
                }
                appendChar(input.charAt(index));
                break;
            default:
                appendChar(c);
        }
    }

    private void handleQuote(char c) {
        switch (c) {
            case '"':
                state = State.NORMAL;
                finishArg(false);
                break;
            case '\\':
                if (index + 1 < input.length()) {
                    index++;
                }
                appendChar(input.charAt(index));
                break;
            default:
                appendChar(c);
        }
    }

    private void finishArg(boolean requireText) {
        if (currentArg.length() == 0 && requireText) {
            return;
        }
        args.add(currentArg.toString());
        currentArg.setLength(0);
    }

    private void appendChar(char c) {
        currentArg.append(c);
    }

}
