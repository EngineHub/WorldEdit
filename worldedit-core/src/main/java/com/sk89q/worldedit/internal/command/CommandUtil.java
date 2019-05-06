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

import com.google.common.collect.Iterables;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.internal.util.Substring;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

public class CommandUtil {

    public static Map<String, Command> getSubCommands(Command currentCommand) {
        return currentCommand.getParts().stream()
            .filter(p -> p instanceof SubCommandPart)
            .flatMap(p -> ((SubCommandPart) p).getCommands().stream())
            .collect(Collectors.toMap(Command::getName, Function.identity()));
    }

    private static String clean(String input) {
        return PlatformCommandManager.COMMAND_CLEAN_PATTERN.matcher(input).replaceAll("");
    }

    private static final Comparator<Command> BY_CLEAN_NAME =
        Comparator.comparing(c -> clean(c.getName()));

    public static Comparator<Command> byCleanName() {
        return BY_CLEAN_NAME;
    }

    /**
     * Fix {@code suggestions} to replace the last space-separated word in {@code arguments}.
     */
    public static List<String> fixSuggestions(String arguments, List<Substring> suggestions) {
        Substring lastArg = Iterables.getLast(CommandArgParser.spaceSplit(arguments));
        return suggestions.stream()
            .map(suggestion -> CommandUtil.suggestLast(lastArg, suggestion))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
    }

    /**
     * Given the last word of a command, mutate the suggestion to replace the last word, if
     * possible.
     */
    private static Optional<String> suggestLast(Substring last, Substring suggestion) {
        if (suggestion.getStart() == last.getEnd()) {
            // this suggestion is for the next argument.
            if (last.getSubstring().isEmpty()) {
                return Optional.of(suggestion.getSubstring());
            }
            return Optional.of(last.getSubstring() + " " + suggestion.getSubstring());
        }
        StringBuilder builder = new StringBuilder(last.getSubstring());
        int start = suggestion.getStart() - last.getStart();
        int end = suggestion.getEnd() - last.getStart();
        if (start < 0) {
            // Quoted suggestion, can't complete it here.
            return Optional.empty();
        }
        checkState(end <= builder.length(),
            "Suggestion ends too late, last=%s, suggestion=", last, suggestion);
        builder.replace(start, end, suggestion.getSubstring());
        return Optional.of(builder.toString());
    }

    private CommandUtil() {
    }
}
