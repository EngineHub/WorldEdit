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

package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

public class EnumConverter<E extends Enum<E>> implements ArgumentConverter<E> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SelectorChoice.class),
            basic(SelectorChoice.class, SelectorChoice.UNKNOWN));
        commandManager.registerConverter(Key.of(TreeGenerator.TreeType.class),
            full(TreeGenerator.TreeType.class,
                t -> ImmutableSet.copyOf(t.lookupKeys),
                null));
        commandManager.registerConverter(Key.of(EditSession.ReorderMode.class),
            full(EditSession.ReorderMode.class,
                r -> ImmutableSet.of(r.getDisplayName()),
                null));
    }

    private static <E extends Enum<E>> EnumConverter<E> basic(Class<E> enumClass) {
        return full(enumClass, e -> ImmutableSet.of(e.name()), null);
    }

    private static <E extends Enum<E>> EnumConverter<E> basic(Class<E> enumClass, E unknownValue) {
        return full(enumClass, e -> ImmutableSet.of(e.name()), unknownValue);
    }

    private static <E extends Enum<E>> EnumConverter<E> full(Class<E> enumClass,
                                                             Function<E, Set<String>> lookupKeys,
                                                             @Nullable E unknownValue) {
        return new EnumConverter<>(enumClass, lookupKeys, unknownValue);
    }

    private final Component choices;
    private final ImmutableMap<String, E> map;
    @Nullable
    private final E unknownValue;

    private EnumConverter(Class<E> enumClass,
                          Function<E, Set<String>> lookupKeys,
                          @Nullable E unknownValue) {
        ImmutableSortedMap.Builder<String, E> map = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
        Stream.Builder<Set<String>> choices = Stream.builder();
        EnumSet<E> validValues = EnumSet.allOf(enumClass);
        if (unknownValue != null) {
            validValues.remove(unknownValue);
        }
        for (E e : validValues) {
            Set<String> keys = lookupKeys.apply(e);
            checkState(keys.size() > 0, "No lookup keys for enum value %s", e);
            choices.add(keys);
            for (String key : keys) {
                map.put(key, e);
            }
        }
        this.choices = TextComponent.of(choices.build()
            .map(choice -> choice.stream().collect(joining("|", "[", "]")))
            .collect(joining("|")));
        this.map = map.build();
        this.unknownValue = unknownValue;
    }

    @Override
    public Component describeAcceptableArguments() {
        return choices;
    }

    @Override
    public ConversionResult<E> convert(String argument, InjectedValueAccess context) {
        E result = map.getOrDefault(argument, unknownValue);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException("Not a valid choice: " + argument))
            : SuccessfulConversion.fromSingle(result);
    }
}
