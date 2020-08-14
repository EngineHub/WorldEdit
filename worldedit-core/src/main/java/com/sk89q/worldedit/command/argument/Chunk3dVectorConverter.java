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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.internal.annotation.Chunk3d;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.function.Function;

public class Chunk3dVectorConverter<C, T> implements ArgumentConverter<T> {
    public static void register(CommandManager commandManager) {
        CommaSeparatedValuesConverter<Integer> intConverter = CommaSeparatedValuesConverter.wrap(ArgumentConverters.get(TypeToken.of(int.class)));
        commandManager.registerConverter(Key.of(BlockVector3.class, Chunk3d.class),
            new Chunk3dVectorConverter<>(
                intConverter,
                Range.closed(2, 3),
                cmps -> {
                    switch (cmps.size()) {
                        case 2:
                            return BlockVector3.at(cmps.get(0), 0, cmps.get(1));
                        case 3:
                            return BlockVector3.at(cmps.get(0), cmps.get(1), cmps.get(2));
                        default:
                            break;
                    }
                    throw new AssertionError("Expected 2 or 3 components");
                },
                "block vector with x,z or x,y,z"
            ));
    }

    private final ArgumentConverter<C> componentConverter;
    private final Range<Integer> componentCount;
    private final Function<List<C>, T> vectorConstructor;
    private final String acceptableArguments;

    private Chunk3dVectorConverter(ArgumentConverter<C> componentConverter,
                                   Range<Integer> componentCount,
                                   Function<List<C>, T> vectorConstructor,
                                   String acceptableArguments) {
        this.componentConverter = componentConverter;
        this.componentCount = componentCount;
        this.vectorConstructor = vectorConstructor;
        this.acceptableArguments = acceptableArguments;
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of("any " + acceptableArguments);
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        ConversionResult<C> components = componentConverter.convert(argument, context);
        if (!components.isSuccessful()) {
            return components.failureAsAny();
        }
        if (!componentCount.contains(components.get().size())) {
            return FailedConversion.from(new IllegalArgumentException(
                "Must have " + componentCount + " vector components"));
        }
        T vector = vectorConstructor.apply(ImmutableList.copyOf(components.get()));
        return SuccessfulConversion.fromSingle(vector);
    }
}
