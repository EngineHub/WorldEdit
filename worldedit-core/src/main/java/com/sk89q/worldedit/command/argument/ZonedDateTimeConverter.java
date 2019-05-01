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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;

public class ZonedDateTimeConverter implements ArgumentConverter<ZonedDateTime> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(ZonedDateTime.class), new ZonedDateTimeConverter());
    }

    private ZonedDateTimeConverter() {
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of("any date");
    }

    @Override
    public ConversionResult<ZonedDateTime> convert(String argument, InjectedValueAccess context) {
        LocalSession session = context.injectedValue(Key.of(LocalSession.class))
            .orElseThrow(() -> new IllegalStateException("Need a local session"));
        Calendar date = session.detectDate(argument);
        if (date == null) {
            return FailedConversion.from(new IllegalArgumentException("Not a date: " + argument));
        }
        return SuccessfulConversion.fromSingle(date.toInstant().atZone(ZoneOffset.UTC));
    }
}
