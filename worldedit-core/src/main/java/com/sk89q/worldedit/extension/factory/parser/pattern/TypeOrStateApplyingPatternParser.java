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

package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.function.pattern.ExtentBufferedCompositePattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.StateApplyingPattern;
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class TypeOrStateApplyingPatternParser extends InputParser<Pattern> {

    public TypeOrStateApplyingPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input) {
        if (input.isEmpty()) {
            return Stream.of("^");
        }
        if (!input.startsWith("^")) {
            return Stream.empty();
        }
        input = input.substring(1);

        String[] parts = input.split("\\[", 2);
        String type = parts[0];

        if (parts.length == 1) {
            return worldEdit.getBlockFactory().getSuggestions(input).stream().map(s -> "^" + s);
        } else {
            if (type.isEmpty()) {
                return Stream.empty(); // without knowing a type, we can't really suggest states
            } else {
                return SuggestionHelper.getBlockPropertySuggestions(type, parts[1]).map(s -> "^" + s);
            }
        }
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("^")) {
            return null;
        }
        Extent extent = context.requireExtent();
        input = input.substring(1);

        String[] parts = input.split("\\[", 2);
        String type = parts[0];

        if (parts.length == 1) {
            return new TypeApplyingPattern(extent,
                    worldEdit.getBlockFactory().parseFromInput(type, context).getBlockType().getDefaultState());
        } else {
            // states given
            if (!parts[1].endsWith("]")) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-rbracket"));
            }
            final String[] states = parts[1].substring(0, parts[1].length() - 1).split(",");
            Map<String, String> statesToSet = new HashMap<>();
            for (String state : states) {
                if (state.isEmpty()) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.empty-state"));
                }
                String[] propVal = state.split("=", 2);
                if (propVal.length != 2) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-equals-separator"));
                }
                final String prop = propVal[0];
                if (prop.isEmpty()) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.empty-property"));
                }
                final String value = propVal[1];
                if (value.isEmpty()) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.empty-value"));
                }
                if (statesToSet.put(prop, value) != null) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.duplicate-property", TextComponent.of(prop)));
                }
            }
            if (type.isEmpty()) {
                return new StateApplyingPattern(extent, statesToSet);
            } else {
                Extent buffer = new ExtentBuffer(extent);
                Pattern typeApplier = new TypeApplyingPattern(buffer,
                        worldEdit.getBlockFactory().parseFromInput(type, context).getBlockType().getDefaultState());
                Pattern stateApplier = new StateApplyingPattern(buffer, statesToSet);
                return new ExtentBufferedCompositePattern(buffer, typeApplier, stateApplier);
            }
        }
    }

}
