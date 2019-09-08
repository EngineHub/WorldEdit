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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.OffsetMask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.stream.Stream;

public class OffsetMaskParser extends InputParser<Mask> {

    public OffsetMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input) {
        if (input.isEmpty()) {
            return Stream.of(">", "<");
        }
        final char firstChar = input.charAt(0);
        if (firstChar != '>' && firstChar != '<') {
            return Stream.empty();
        }
        return worldEdit.getMaskFactory().getSuggestions(input.substring(1)).stream().map(s -> firstChar + s);
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        final char firstChar = input.charAt(0);
        if (firstChar != '>' && firstChar != '<') {
            return null;
        }

        Mask submask;
        if (input.length() > 1) {
            submask = worldEdit.getMaskFactory().parseFromInput(input.substring(1), context);
        } else {
            submask = new ExistingBlockMask(context.getExtent());
        }
        OffsetMask offsetMask = new OffsetMask(submask, BlockVector3.at(0, firstChar == '>' ? -1 : 1, 0));
        return new MaskIntersection(offsetMask, Masks.negate(submask));
    }
}
