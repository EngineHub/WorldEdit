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

package com.sk89q.worldedit.internal.command;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Parser for command arguments.
 *
 * <p>Rules:
 * <ul>
 *     <li>Arguments are separated by whitespace.</li>
 *     <li>Starting an argument with a quote character will start quote mode and include all characters until the next
 *     unescaped quote character. Backslash is the escape character, and only works for quotes and backslashes when
 *     in quote mode. Quote mode ends where the argument does. There must be whitespace following the unescaped quote
 *     character or an error will be thrown, and should be reported to the user.</li>
 *     <li>If {@code forCompletions}, quotes and escapes may be partial and end at the end of the string.
 *     This is required to do completions while in quotes. Partial escapes will just be ignored.</li>
 * </ul>
 */
public class CommandArgParser extends AbstractIterator<ExtractedArg> {

    public static Stream<ExtractedArg> parse(String argString, boolean forCompletions) {
        return Streams.stream(new CommandArgParser(argString, forCompletions));
    }

    private final String input;
    private final boolean forCompletions;
    private int index;

    private CommandArgParser(String input, boolean forCompletions) {
        this.input = input;
        this.forCompletions = forCompletions;
    }

    @Override
    protected @Nullable ExtractedArg computeNext() {
        if (index > input.length()) {
            return endOfData();
        }
        if (index == input.length()) {
            // May need to special case this to handle trailing whitespace or empty strings. Index is bumped
            // to signal end.
            index++;
            if (input.isEmpty()) {
                return new ExtractedArg("", 0, 0);
            }
            if (Character.isWhitespace(input.charAt(input.length() - 1))) {
                return new ExtractedArg("", input.length(), input.length());
            }
            return endOfData();
        }
        char startChar = input.charAt(index);
        return switch (startChar) {
            case '\'', '"' -> finishQuoted();
            case '\\' -> {
                // Check if escaping quote, if so we need to feed the unquoted reader some extra info.
                if (index + 1 >= input.length()) {
                    if (forCompletions) {
                        index++;
                        yield new ExtractedArg("", index, index);
                    }
                    throw new CommandArgParseException("Invalid escape at end of string");
                }
                char escaped = input.charAt(index + 1);
                if (escaped == '\'' || escaped == '"') {
                    int startIndex = index;
                    index++;
                    yield finishUnquoted(startIndex);
                }
                // Otherwise, this is an error.
                throw new CommandArgParseException("Invalid escaped character: " + escaped);
            }
            default -> finishUnquoted(index);
        };
    }

    private char takeChar() {
        char c = input.charAt(index);
        index++;
        return c;
    }

    private ExtractedArg finishQuoted() {
        int start = index;
        char quoteChar = takeChar();
        StringBuilder builder = new StringBuilder();
        while (index < input.length()) {
            char c = takeChar();
            if (c == '\\') {
                if (index >= input.length()) {
                    // Error out.
                    break;
                }
                char next = takeChar();
                if (next == quoteChar || next == '\\') {
                    builder.append(next);
                } else {
                    throw new CommandArgParseException("Invalid escaped character: " + next);
                }
                continue;
            }
            if (c == quoteChar) {
                int end = index;
                if (index < input.length() && !Character.isWhitespace(takeChar())) {
                    throw new CommandArgParseException("Expected whitespace after quote");
                }
                return new ExtractedArg(builder.toString(), start, end);
            }
            builder.append(c);
        }
        if (forCompletions) {
            // Add an extra offset to the index to signal that we are done completely.
            int end = index;
            index++;
            return new ExtractedArg(builder.toString(), start, end);
        }
        throw new CommandArgParseException("Unterminated quote");
    }

    /**
     * Finish reading an unquoted argument.
     *
     * @param startIndex the index at which the argument started, which may be behind the current index that the string
     *     should be copied from
     * @return the extracted argument
     */
    private ExtractedArg finishUnquoted(int startIndex) {
        int start = index;
        while (index < input.length()) {
            char c = takeChar();
            if (Character.isWhitespace(c)) {
                int end = index - 1;
                return new ExtractedArg(input.substring(start, end), startIndex, end);
            }
        }
        return new ExtractedArg(input.substring(start), startIndex, index);
    }
}
