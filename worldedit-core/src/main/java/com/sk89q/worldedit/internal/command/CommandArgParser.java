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
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

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

    /**
     * The inverse of {@link #parse(String, boolean)}. Escapes all characters necessary to ensure that it parses into
     * the original {@code arg}.
     *
     * @param arg the raw argument text
     * @return the escaped argument text
     */
    public static String escape(String arg) {
        if (!needsQuoting(arg)) {
            return arg;
        }
        // It might be nice to do smart quoting, since we support both ' and " in arguments
        // For now, I'm just doing the simple option
        return quote(arg, '"');
    }

    /**
     * Wrap {@code arg} in {@code quoteChar}, escaping as needed so that {@link #parse(String, boolean)} reads it back
     * as exactly one argument with the original text. Unlike {@link #escape(String)}, this always adds quotes.
     *
     * @param arg the raw argument text
     * @param quoteChar the quote character to use, either {@code '"'} or {@code '\''}
     * @return the quoted argument text
     */
    public static String quote(String arg, char quoteChar) {
        checkArgument(quoteChar == '"' || quoteChar == '\'', "Not a quote character: %s", quoteChar);
        StringBuilder builder = new StringBuilder(arg.length() + 2);
        builder.append(quoteChar);
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);
            if (c == '\\' || c == quoteChar) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append(quoteChar);
        return builder.toString();
    }

    private static boolean needsQuoting(String arg) {
        if (arg.isEmpty()) {
            return true;
        }
        // These characters are only special at the start of an argument
        char first = arg.charAt(0);
        if (first == '"' || first == '\'' || first == '\\') {
            return true;
        }
        return arg.chars().anyMatch(Character::isWhitespace);
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
                        // Include the backslash, so a suggestion replaces it
                        int start = index;
                        index++;
                        yield new ExtractedArg("", start, index);
                    }
                    throw new CommandArgParseException(TranslatableComponent.of(
                        "worldedit.command.parse-error.invalid-escape-at-end"
                    ));
                }
                char escaped = input.charAt(index + 1);
                if (escaped == '\'' || escaped == '"') {
                    int startIndex = index;
                    index++;
                    yield finishUnquoted(startIndex);
                }
                // Otherwise, this is an error.
                throw invalidEscapedCharacter(escaped);
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
                    throw invalidEscapedCharacter(next);
                }
                continue;
            }
            if (c == quoteChar) {
                int end = index;
                if (index < input.length() && !Character.isWhitespace(takeChar())) {
                    throw new CommandArgParseException(TranslatableComponent.of(
                        "worldedit.command.parse-error.expected-whitespace-after-quote"
                    ));
                }
                return new ExtractedArg(builder.toString(), start, end);
            }
            builder.append(c);
        }
        if (forCompletions) {
            // Add an extra offset to the index to signal that we are done completely.
            int end = index;
            index++;
            return new ExtractedArg(builder.toString(), start, end, true);
        }
        throw new CommandArgParseException(TranslatableComponent.of(
            "worldedit.command.parse-error.unterminated-quote"
        ));
    }

    private static CommandArgParseException invalidEscapedCharacter(char c) {
        return new CommandArgParseException(TranslatableComponent.of(
            "worldedit.command.parse-error.invalid-escaped-character", TextComponent.of(String.valueOf(c))
        ));
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
