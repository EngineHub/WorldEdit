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

import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
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

public class SideEffectSetConverter implements ArgumentConverter<SideEffectSet> {

    public static void register(CommandManager commandManager) {
        ArgumentConverter<SideEffect> sideEffectConverter = commandManager.getConverter(Key.of(SideEffect.class))
            .orElseThrow(() -> new IllegalStateException("SideEffectSetConverter must be registered after SideEffectConverter"));
        commandManager.registerConverter(
            Key.of(SideEffectSet.class),
            new SideEffectSetConverter(CommaSeparatedValuesConverter.wrapNoRepeats(sideEffectConverter))
        );
    }

    private final TextComponent choices = TextComponent.of("any side effects");
    private final CommaSeparatedValuesConverter<SideEffect> sideEffectConverter;

    private SideEffectSetConverter(CommaSeparatedValuesConverter<SideEffect> sideEffectConverter) {
        this.sideEffectConverter = sideEffectConverter;
    }

    @Override
    public Component describeAcceptableArguments() {
        return choices;
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return sideEffectConverter.getSuggestions(input, context);
    }

    @Override
    public ConversionResult<SideEffectSet> convert(String argument, InjectedValueAccess context) {
        try {
            ConversionResult<SideEffect> result = sideEffectConverter.convert(argument, context);
            if (result.isSuccessful()) {
                SideEffectSet set = SideEffectSet.none();
                for (SideEffect sideEffect : result.get()) {
                    set = set.with(sideEffect, SideEffect.State.ON);
                }
                return SuccessfulConversion.fromSingle(set);
            } else {
                return result.failureAsAny();
            }
        } catch (Exception e) {
            return FailedConversion.from(e);
        }
    }
}
