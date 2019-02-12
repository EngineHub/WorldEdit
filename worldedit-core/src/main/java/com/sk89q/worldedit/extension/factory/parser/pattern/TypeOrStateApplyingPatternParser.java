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

package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.function.pattern.ExtentBufferedCompositePattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.StateApplyingPattern;
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.Map;
import java.util.Set;


public class TypeOrStateApplyingPatternParser extends InputParser<Pattern> {

    public TypeOrStateApplyingPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
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
            if (!parts[1].endsWith("]")) throw new InputParseException("Invalid state format.");
            Map<String, String> statesToSet = Splitter.on(',')
                    .omitEmptyStrings().trimResults().withKeyValueSeparator('=')
                    .split(parts[1].substring(0, parts[1].length() - 1));
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
