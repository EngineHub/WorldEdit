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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
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

import static java.util.stream.Collectors.toList;

public class CommandUtil {

    private static final Component DEPRECATION_MARKER = TextComponent.of("This command is deprecated.");

    private static Component makeDeprecatedFooter(String reason, Component replacement) {
        return TextComponent.builder()
            .append(DEPRECATION_MARKER)
            .append(" " + reason + ".")
            .append(TextComponent.newline())
            .append(replacement.color(TextColor.GOLD))
            .build();
    }

    public interface NewCommandGenerator {

        String newCommand(Command oldCommand, CommandParameters oldParameters);

    }

    public interface ReplacementMessageGenerator {

        /**
         * Generate text that says "Please use [cmd] instead." and allows clicking to dump
         * the command to the text box.
         */
        static ReplacementMessageGenerator forNewCommand(NewCommandGenerator generator) {
            return (oldCommand, oldParameters) -> {
                String suggestedCommand = generator.newCommand(oldCommand, oldParameters);
                return createNewCommandReplacementText(suggestedCommand);
            };
        }

        Component getReplacement(Command oldCommand, CommandParameters oldParameters);

    }

    public static Component createNewCommandReplacementText(String suggestedCommand) {
        return TextComponent.builder("Please use ", TextColor.GOLD)
            .append(TextComponent.of(suggestedCommand)
                .decoration(TextDecoration.UNDERLINED, true)
                .clickEvent(ClickEvent.suggestCommand(suggestedCommand)))
            .append(" instead.")
            .build();
    }

    public static Command deprecate(Command command, String reason,
                                    ReplacementMessageGenerator replacementMessageGenerator) {
        Component deprecatedWarning = makeDeprecatedFooter(
            reason,
            replacementMessageGenerator.getReplacement(
                command,
                NoInputCommandParameters.builder().build()
            )
        );
        return command.toBuilder()
            .action(parameters ->
                deprecatedCommandWarning(parameters, command, reason, replacementMessageGenerator))
            .footer(command.getFooter()
                .map(existingFooter -> existingFooter
                    .append(TextComponent.newline())
                    .append(deprecatedWarning))
                .orElse(deprecatedWarning))
            .build();
    }

    public static Optional<Component> footerWithoutDeprecation(Command command) {
        return command.getFooter()
            .filter(footer -> anyComponent(footer, Predicate.isEqual(DEPRECATION_MARKER)))
            .map(CommandUtil::replaceDeprecation)
            .or(command::getFooter);
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
        ReplacementMessageGenerator generator
    ) throws Exception {
        parameters.injectedValue(Key.of(Actor.class))
            .ifPresent(actor ->
                sendDeprecationMessage(parameters, command, reason, generator, actor)
            );
        return command.getAction().run(parameters);
    }

    private static void sendDeprecationMessage(
        CommandParameters parameters,
        Command command,
        String reason,
        ReplacementMessageGenerator generator,
        Actor actor
    ) {
        Component replacement = generator.getReplacement(command, parameters);
        actor.print(
            TextComponent.builder(reason + ". ", TextColor.GOLD)
                .append(replacement)
                .build()
        );
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
     * Fix suggestions to only replace the last space-separated argument from {@code arguments}.
     *
     * @param arguments the full command string, e.g. {@code "/command arg1 arg2"}
     * @param suggestions the suggestions
     * @return a list of fixed suggestions
     */
    public static List<String> fixSuggestions(String arguments, List<FullStringSuggestion> suggestions) {
        int minIndex = arguments.lastIndexOf(' ') + 1;
        if (minIndex == 0) {
            // No space, so no suggestions
            return List.of();
        }
        return suggestions.stream()
            .<String>mapMulti((fullStringSuggestion, downstream) -> {
                if (fullStringSuggestion.replaceStart() < minIndex) {
                    if (fullStringSuggestion.replaceEnd() <= minIndex) {
                        // This suggestion is entirely before the last argument, so ignore it
                        return;
                    }
                    // We might still take this suggestion if the text matches up to minIndex
                    int fromIndex = fullStringSuggestion.replaceStart();
                    int len = minIndex - fromIndex;
                    if (!arguments.regionMatches(
                        fromIndex, fullStringSuggestion.escapedText(), 0, len
                    )) {
                        // This suggestion changes the text before the last argument, so ignore it
                        return;
                    }
                    // Adjust the suggestion so it's only for after minIndex
                    fullStringSuggestion = new FullStringSuggestion(
                        fullStringSuggestion.escapedText().substring(len),
                        fullStringSuggestion.replaceStart() + len,
                        fullStringSuggestion.replaceEnd()
                    );
                }
                // Tweak the suggestion so it replaces the last argument entirely.
                StringBuilder adjusted = new StringBuilder(arguments);
                adjusted.replace(
                    fullStringSuggestion.replaceStart(),
                    fullStringSuggestion.replaceEnd(),
                    fullStringSuggestion.escapedText()
                );
                downstream.accept(adjusted.substring(minIndex));
            })
            .toList();
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
