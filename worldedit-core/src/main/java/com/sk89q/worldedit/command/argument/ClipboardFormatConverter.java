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

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.share.ClipboardShareDestination;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.Set;

import static org.enginehub.piston.converter.SuggestionHelper.limitByPrefix;

public class ClipboardFormatConverter implements ArgumentConverter<ClipboardFormat> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(ClipboardFormat.class),
            new ClipboardFormatConverter()
        );
    }

    private final TextComponent choices;

    private ClipboardFormatConverter() {
        this.choices = TextComponent.of("any clipboard format");
    }

    @Override
    public Component describeAcceptableArguments() {
        return this.choices;
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        ClipboardShareDestination destination = context.injectedValue(Key.of(ClipboardShareDestination.class)).orElse(null);

        return limitByPrefix(ClipboardFormats.getAll().stream()
            .filter(format -> destination == null || destination.supportsFormat(format))
            .map(ClipboardFormat::getAliases)
            .flatMap(Set::stream), input);
    }

    @Override
    public ConversionResult<ClipboardFormat> convert(String s, InjectedValueAccess injectedValueAccess) {
        ClipboardFormat result = ClipboardFormats.findByAlias(s);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException("Not a valid schematic format: " + s))
            : SuccessfulConversion.fromSingle(result);
    }
}
