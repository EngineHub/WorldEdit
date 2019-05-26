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
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.session.request.RequestExtent;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Parses mask input strings.
 */
public class BlocksMaskParser extends InputParser<Mask> {

    public BlocksMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input) {
        return worldEdit.getBlockFactory().getSuggestions(input).stream();
    }

    @Override
    public Mask parseFromInput(String component, ParserContext context) throws InputParseException {
        ParserContext tempContext = new ParserContext(context);
        tempContext.setRestricted(false);
        tempContext.setPreferringWildcard(true);
        try {
            Set<BaseBlock> holders = worldEdit.getBlockFactory().parseFromListInput(component, tempContext);
            if (holders.isEmpty()) {
                return null;
            }
            return new BlockMask(new RequestExtent(), holders);
        } catch (NoMatchException e) {
            return null;
        }
    }

}
