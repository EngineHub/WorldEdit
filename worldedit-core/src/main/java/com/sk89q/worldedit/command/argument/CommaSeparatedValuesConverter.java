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

package com.sk89q.worldedit.command.argument;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.sk89q.worldedit.util.formatting.text.TextComponent.space;

public class CommaSeparatedValuesConverter<T> implements ArgumentConverter<T> {

    public static <T> CommaSeparatedValuesConverter<T> wrap(ArgumentConverter<T> delegate) {
        return wrapAndLimit(delegate, -1);
    }

    public static <T> CommaSeparatedValuesConverter<T> wrapNoRepeats(ArgumentConverter<T> delegate) {
        return wrapAndLimitNoRepeats(delegate, -1);
    }

    public static <T> CommaSeparatedValuesConverter<T> wrapAndLimit(ArgumentConverter<T> delegate, int maximum) {
        return new CommaSeparatedValuesConverter<>(delegate, maximum, true);
    }

    public static <T> CommaSeparatedValuesConverter<T> wrapAndLimitNoRepeats(ArgumentConverter<T> delegate, int maximum) {
        return new CommaSeparatedValuesConverter<>(delegate, maximum, false);
    }

    private static final Splitter COMMA = Splitter.on(',');

    private final ArgumentConverter<T> delegate;
    private final int maximum;
    private final boolean repeats;

    private CommaSeparatedValuesConverter(ArgumentConverter<T> delegate, int maximum, boolean repeats) {
        checkArgument(maximum == -1 || maximum > 1,
            "Maximum must be bigger than 1, or exactly -1");
        this.delegate = delegate;
        this.maximum = maximum;
        this.repeats = repeats;
    }

    @Override
    public Component describeAcceptableArguments() {
        TextComponent.Builder result = TextComponent.builder("");
        if (maximum > -1) {
            result.append(TextComponent.of("up to "))
                .append(TextComponent.of(maximum))
                .append(space());
        }
        result.append(TextComponent.of("comma separated values of: "))
            .append(delegate.describeAcceptableArguments());
        return result.build();
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        String lastInput = Iterables.getLast(COMMA.split(input), "");
        assert lastInput != null;
        List<String> suggestions = delegate.getSuggestions(lastInput, context);
        if (input.contains(",")) {
            String prefix = input.substring(0, input.length() - lastInput.length());
            Set<String> entries = ImmutableSet.copyOf(COMMA.split(input));
            suggestions = suggestions
                .stream()
                .filter(suggestion -> repeats || !entries.contains(suggestion))
                .map(suggestion -> prefix + suggestion)
                .collect(Collectors.toList());
        }
        return suggestions;
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        ImmutableList.Builder<T> result = ImmutableList.builder();
        for (String input : COMMA.split(argument)) {
            ConversionResult<T> temp = delegate.convert(input, context);
            if (!temp.isSuccessful()) {
                return temp;
            }
            result.addAll(temp.get());
        }
        return SuccessfulConversion.from(result.build());
    }

}
