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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.enginehub.piston.converter.SuggestionHelper.limitByPrefix;

public class SideEffectConverter implements ArgumentConverter<SideEffect> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SideEffect.class), new SideEffectConverter());
    }

    private final TextComponent choices = TextComponent.of("any side effect");

    private SideEffectConverter() {
    }

    private Collection<SideEffect> getSideEffects() {
        return WorldEdit.getInstance().getPlatformManager().getSupportedSideEffects();
    }

    @Override
    public Component describeAcceptableArguments() {
        return choices;
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return limitByPrefix(getSideEffects().stream()
            .filter(SideEffect::isExposed)
            .map(sideEffect -> sideEffect.name().toLowerCase(Locale.US)), input);
    }

    @Override
    public ConversionResult<SideEffect> convert(String argument, InjectedValueAccess context) {
        try {
            return SuccessfulConversion.fromSingle(SideEffect.valueOf(argument.toUpperCase(Locale.US)));
        } catch (Exception e) {
            return FailedConversion.from(e);
        }
    }
}
