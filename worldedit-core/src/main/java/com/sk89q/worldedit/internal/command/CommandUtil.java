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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.NoInputCommandParameters;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

public class CommandUtil {

    private static final Component DEPRECATION_MARKER = TextComponent.of("This command is deprecated.");

    private static Component makeDeprecatedFooter(String reason, Component newCommand) {
        return TextComponent.builder()
            .append(DEPRECATION_MARKER)
            .append(" " + reason + ".")
            .append(TextComponent.newline())
            .append(TextComponent.of("Use ", TextColor.GOLD, TextDecoration.ITALIC))
            .append(newCommand)
            .append(TextComponent.of(" instead.", TextColor.GOLD, TextDecoration.ITALIC))
            .build();
    }

    public interface NewCommandGenerator {

        String newCommand(Command oldCommand, CommandParameters oldParameters);

    }

    public static Command deprecate(Command command, String reason,
                                    NewCommandGenerator newCommandGenerator) {
        Component deprecatedWarning = makeDeprecatedFooter(
            reason,
            newCommandSuggestion(newCommandGenerator,
                NoInputCommandParameters.builder().build(),
                command)
        );
        return command.toBuilder()
            .action(parameters ->
                deprecatedCommandWarning(parameters, command, reason, newCommandGenerator))
            .footer(command.getFooter()
                .map(existingFooter -> existingFooter
                    .append(TextComponent.newline()).append(deprecatedWarning))
                .orElse(deprecatedWarning))
            .build();
    }

    public static Optional<Component> footerWithoutDeprecation(Command command) {
        return command.getFooter()
            .filter(footer -> anyComponent(footer, Predicate.isEqual(DEPRECATION_MARKER)))
            .map(footer -> Optional.of(
                replaceDeprecation(footer)
            ))
            .orElseGet(command::getFooter);
    }

    public static Optional<Component> deprecationWarning(Command command) {
        return command.getFooter()
            .map(CommandUtil::extractDeprecation)
            .orElseGet(command::getFooter);
    }

    public static boolean isDeprecated(Command command) {
        return command.getFooter()
            .filter(footer -> anyComponent(footer, Predicate.isEqual(DEPRECATION_MARKER)))
            .isPresent();
    }

    private static boolean anyComponent(Component component, Predicate<Component> test) {
        return test.test(component) || component.children().stream()
            .anyMatch(x -> anyComponent(x, test));
    }

    private static Component replaceDeprecation(Component component) {
        if (component.children().stream().anyMatch(Predicate.isEqual(DEPRECATION_MARKER))) {
            return TextComponent.empty();
        }
        return component.children(
            component.children().stream()
                .map(CommandUtil::replaceDeprecation)
                .collect(toList())
        );
    }

    private static Optional<Component> extractDeprecation(Component component) {
        if (component.children().stream().anyMatch(Predicate.isEqual(DEPRECATION_MARKER))) {
            return Optional.of(component);
        }
        return component.children().stream()
            .map(CommandUtil::extractDeprecation)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny();
    }

    private static int deprecatedCommandWarning(
        CommandParameters parameters,
        Command command,
        String reason,
        NewCommandGenerator generator
    ) throws Exception {
        parameters.injectedValue(Key.of(Actor.class))
            .ifPresent(actor -> {
                Component suggestion = newCommandSuggestion(generator, parameters, command);
                actor.print(TextComponent.of(reason + ". Please use ", TextColor.GOLD)
                    .append(suggestion)
                    .append(TextComponent.of(" instead."))
                );
            });
        return command.getAction().run(parameters);
    }

    private static Component newCommandSuggestion(NewCommandGenerator generator,
                                                  CommandParameters parameters,
                                                  Command command) {
        String suggestedCommand = generator.newCommand(command, parameters);
        return TextComponent.of(suggestedCommand)
            .decoration(TextDecoration.UNDERLINED, true)
            .clickEvent(ClickEvent.suggestCommand(suggestedCommand));
    }

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
        Substring lastArg = Iterables.getLast(
            CommandArgParser.spaceSplit(arguments)
        );
        return suggestions.stream()
            // Re-map suggestions to only operate on the last non-quoted word
            .map(suggestion -> onlyOnLastQuotedWord(lastArg, suggestion))
            .map(suggestion -> suggestLast(lastArg, suggestion))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
    }

    private static Substring onlyOnLastQuotedWord(Substring lastArg, Substring suggestion) {
        if (suggestion.getSubstring().startsWith(lastArg.getSubstring())) {
            // This is already fine.
            return suggestion;
        }
        String substr = suggestion.getSubstring();
        int sp = substr.lastIndexOf(' ');
        if (sp < 0) {
            return suggestion;
        }
        return Substring.wrap(substr.substring(sp + 1), suggestion.getStart() + sp + 1, suggestion.getEnd());
    }

    /**
     * Given the last word of a command, mutate the suggestion to replace the last word, if
     * possible.
     */
    private static Optional<String> suggestLast(Substring last, Substring suggestion) {
        if (suggestion.getStart() == last.getEnd() && !last.getSubstring().equals("\"")) {
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

    /**
     * Require {@code condition} to be {@code true}, otherwise throw a {@link CommandException}
     * with the given message.
     *
     * @param condition the condition to check
     * @param message the message for failure
     */
    public static void checkCommandArgument(boolean condition, String message) {
        checkCommandArgument(condition, TextComponent.of(message));
    }

    /**
     * Require {@code condition} to be {@code true}, otherwise throw a {@link CommandException}
     * with the given message.
     *
     * @param condition the condition to check
     * @param message the message for failure
     */
    public static void checkCommandArgument(boolean condition, Component message) {
        if (!condition) {
            throw new CommandException(message, ImmutableList.of());
        }
    }

    public static <T> T requireIV(Key<T> type, String name, InjectedValueAccess injectedValueAccess) {
        return injectedValueAccess.injectedValue(type).orElseThrow(() ->
            new IllegalStateException("No injected value for " + name + " (type " + type + ")")
        );
    }

    private CommandUtil() {
    }
}
