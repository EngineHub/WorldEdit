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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.google.common.base.CharMatcher;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Parses mask input strings.
 */
public class BlocksMaskParser extends InputParser<Mask> {

    private static final CharMatcher LIST_DELIMITER = CharMatcher.is(',');

    public BlocksMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        List<String> blocks = StringUtil.splitOutsideBrackets(input, LIST_DELIMITER);
        String token = blocks.get(blocks.size() - 1);
        String prefix = input.substring(0, input.length() - token.length());
        return worldEdit.getBlockFactory().getSuggestions(token, context).stream().map(s -> prefix + s);
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
            return new BlockMask(context.requireExtent(), holders);
        } catch (NoMatchException _) {
            return null;
        }
    }

}
