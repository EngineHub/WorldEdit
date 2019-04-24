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
import com.google.common.collect.ImmutableSortedMap;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class EnumConverter<E extends Enum<E>> implements ArgumentConverter<E> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SelectorChoice.class),
            new EnumConverter<>(SelectorChoice.class, SelectorChoice.UNKNOWN));
    }

    private final ImmutableMap<String, E> map;
    @Nullable
    private final E unknownValue;

    private EnumConverter(Class<E> enumClass, @Nullable E unknownValue) {
        ImmutableSortedMap.Builder<String, E> map = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
        EnumSet<E> validValues = EnumSet.allOf(enumClass);
        if (unknownValue != null) {
            validValues.remove(unknownValue);
        }
        for (E e : validValues) {
            map.put(e.name(), e);
        }
        this.map = map.build();
        this.unknownValue = unknownValue;
    }

    @Override
    public String describeAcceptableArguments() {
        return String.join("|", map.keySet());
    }

    @Override
    public ConversionResult<E> convert(String argument, InjectedValueAccess context) {
        E result = map.getOrDefault(argument, unknownValue);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException("Not a valid choice: " + argument))
            : SuccessfulConversion.fromSingle(result);
    }
}
