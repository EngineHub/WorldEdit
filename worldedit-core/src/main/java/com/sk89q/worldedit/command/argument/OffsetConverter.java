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
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.annotation.Offset;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;

import static com.sk89q.worldedit.util.formatting.text.Component.text;

public class OffsetConverter implements ArgumentConverter<BlockVector3> {

    public static void register(WorldEdit worldEdit, CommandManager commandManager) {
        commandManager.registerConverter(
            Key.of(BlockVector3.class, Offset.class),
            new OffsetConverter(worldEdit)
        );
    }

    private final DirectionVectorConverter directionVectorConverter;
    private final VectorConverter<Integer, BlockVector3> vectorConverter =
        VectorConverter.BLOCK_VECTOR_3_CONVERTER;

    private OffsetConverter(WorldEdit worldEdit) {
        directionVectorConverter = new DirectionVectorConverter(worldEdit, true);
    }

    @Override
    public Component describeAcceptableArguments() {
        return text()
            .append(directionVectorConverter.describeAcceptableArguments())
            .append(text(", or "))
            .append(vectorConverter.describeAcceptableArguments())
            .build();
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return ImmutableList.copyOf(Iterables.concat(
            directionVectorConverter.getSuggestions(input, context),
            vectorConverter.getSuggestions(input, context)
        ));
    }

    @Override
    public ConversionResult<BlockVector3> convert(String input, InjectedValueAccess context) {
        return directionVectorConverter.convert(input, context)
            .orElse(vectorConverter.convert(input, context));
    }
}
