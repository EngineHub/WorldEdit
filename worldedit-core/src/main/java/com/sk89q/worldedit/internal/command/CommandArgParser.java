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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.internal.util.Substring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandArgParser {

    public static ImmutableList<Substring> spaceSplit(String string) {
        ImmutableList.Builder<Substring> result = ImmutableList.builder();
        int index = 0;
        for (String part : Splitter.on(' ').split(string)) {
            result.add(Substring.from(string, index, index + part.length()));
            index += part.length() + 1;
        }
        return result.build();
    }

    private enum State {
        NORMAL,
        QUOTE
    }

    private final Stream.Builder<Substring> args = Stream.builder();
    private final List<Substring> input;
    private final List<Substring> currentArg = new ArrayList<>();
    private int index = 0;
    private State state = State.NORMAL;

    public CommandArgParser(List<Substring> input) {
        this.input = input;
    }

    public Stream<Substring> parseArgs() {
        for (; index < input.size(); index++) {
            Substring nextPart = input.get(index);
            switch (state) {
                case NORMAL:
                    handleNormal(nextPart);
                    break;
                case QUOTE:
                    handleQuote(nextPart);
            }
        }
        return args.build();
    }

    private void handleNormal(Substring part) {
        if (part.getSubstring().startsWith("\"")) {
            state = State.QUOTE;
            currentArg.add(Substring.wrap(
                part.getSubstring().substring(1),
                part.getStart(), part.getEnd()
            ));
        } else {
            currentArg.add(part);
            finishArg();
        }
    }

    private void handleQuote(Substring part) {
        if (part.getSubstring().endsWith("\"")) {
            state = State.NORMAL;
            currentArg.add(Substring.wrap(
                part.getSubstring().substring(0, part.getSubstring().length() - 1),
                part.getStart(), part.getEnd()
            ));
            finishArg();
        } else {
            currentArg.add(part);
        }
    }

    private void finishArg() {
        // Merge the arguments into a single, space-joined, string
        // Keep the original start + end points.
        int start = currentArg.get(0).getStart();
        int end = Iterables.getLast(currentArg).getEnd();
        args.add(Substring.wrap(currentArg.stream()
                .map(Substring::getSubstring)
                .collect(Collectors.joining(" ")),
            start, end
        ));
        currentArg.clear();
    }

}
