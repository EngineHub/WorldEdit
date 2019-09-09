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
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.annotation.VertHeight;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

/**
 * Converter for handling default heights as the
 * {@linkplain LocalConfiguration#defaultVerticalHeight currently configured
 * height}.
 */
public class HeightConverter implements ArgumentConverter<Integer> {

    /**
     * The value that converts to the default vertical height.
     */
    public static final String DEFAULT_VALUE = "default-vertical-height";

    private static final ArgumentConverter<Integer> INT_CONVERTER =
        ArgumentConverters.get(TypeToken.of(int.class));

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(int.class, VertHeight.class),
            new HeightConverter()
        );
    }

    private HeightConverter() {
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of("Any integer");
    }

    @Override
    public ConversionResult<Integer> convert(String argument, InjectedValueAccess context) {
        if (DEFAULT_VALUE.equals(argument)) {
            return SuccessfulConversion.fromSingle(
                WorldEdit.getInstance().getConfiguration().defaultVerticalHeight
            );
        }
        return INT_CONVERTER.convert(argument, context);
    }
}
