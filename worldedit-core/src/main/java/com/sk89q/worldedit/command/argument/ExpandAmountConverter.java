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

import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.stream.Stream;

import static org.enginehub.piston.converter.SuggestionHelper.limitByPrefix;

public class ExpandAmountConverter implements ArgumentConverter<ExpandAmount> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(ExpandAmount.class), new ExpandAmountConverter());
    }

    private final ArgumentConverter<Integer> integerConverter =
        ArgumentConverters.get(TypeToken.of(int.class));

    private ExpandAmountConverter() {
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of("`vert` or " + integerConverter.describeAcceptableArguments());
    }

    @Override
    public List<String> getSuggestions(String input) {
        return limitByPrefix(Stream.concat(
            Stream.of("vert"), integerConverter.getSuggestions(input).stream()
        ), input);
    }

    @Override
    public ConversionResult<ExpandAmount> convert(String argument, InjectedValueAccess context) {
        if (argument.equalsIgnoreCase("vert")
            || argument.equalsIgnoreCase("vertical")) {
            return SuccessfulConversion.fromSingle(ExpandAmount.vert());
        }
        return integerConverter.convert(argument, context).mapSingle(ExpandAmount::from);
    }
}
