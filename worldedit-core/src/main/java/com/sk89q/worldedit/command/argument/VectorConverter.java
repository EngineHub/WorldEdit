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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
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

public class VectorConverter<C, T> implements ArgumentConverter<T> {
    public static void register(CommandManager commandManager) {
        CommaSeparatedValuesConverter<Integer> intConverter = CommaSeparatedValuesConverter.wrap(ArgumentConverters.get(TypeToken.of(int.class)));
        CommaSeparatedValuesConverter<Double> doubleConverter = CommaSeparatedValuesConverter.wrap(ArgumentConverters.get(TypeToken.of(double.class)));
        commandManager.registerConverter(Key.of(BlockVector2.class),
            new VectorConverter<>(
                intConverter,
                2,
                cmps -> BlockVector2.at(cmps.get(0), cmps.get(1)),
                "block vector with x and z"
            ));
        commandManager.registerConverter(Key.of(Vector2.class),
            new VectorConverter<>(
                doubleConverter,
                2,
                cmps -> Vector2.at(cmps.get(0), cmps.get(1)),
                "vector with x and z"
            ));
        commandManager.registerConverter(Key.of(BlockVector3.class),
            new VectorConverter<>(
                intConverter,
                3,
                cmps -> BlockVector3.at(cmps.get(0), cmps.get(1), cmps.get(2)),
                "block vector with x, y, and z"
            ));
        commandManager.registerConverter(Key.of(Vector3.class),
            new VectorConverter<>(
                doubleConverter,
                3,
                cmps -> Vector3.at(cmps.get(0), cmps.get(1), cmps.get(2)),
                "vector with x, y, and z"
            ));
    }

    private final ArgumentConverter<C> componentConverter;
    private final int componentCount;
    private final Function<List<C>, T> vectorConstructor;
    private final String acceptableArguments;


    private VectorConverter(ArgumentConverter<C> componentConverter,
                           int componentCount,
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
        if (components.get().size() != componentCount) {
            return FailedConversion.from(new IllegalArgumentException(
                "Must have exactly " + componentCount + " vector components"));
        }
        T vector = vectorConstructor.apply(ImmutableList.copyOf(components.get()));
        return SuccessfulConversion.fromSingle(vector);
    }
}
