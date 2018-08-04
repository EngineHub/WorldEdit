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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BlockStateHolder;

class RandomPatternParser extends InputParser<Pattern> {

    RandomPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        BlockFactory blockRegistry = worldEdit.getBlockFactory();
        RandomPattern randomPattern = new RandomPattern();

        String[] splits = input.split(",");
        for (String token : StringUtil.parseListInQuotes(splits, ',', '[', ']')) {
            BlockStateHolder block;

            double chance;

            // Parse special percentage syntax
            if (token.matches("[0-9]+(\\.[0-9]*)?%.*")) {
                String[] p = token.split("%");

                if (p.length < 2) {
                    throw new InputParseException("Missing the type after the % symbol for '" + input + "'");
                } else {
                    chance = Double.parseDouble(p[0]);
                    block = blockRegistry.parseFromInput(p[1], context);
                }
            } else {
                chance = 1;
                block = blockRegistry.parseFromInput(token, context);
            }

            randomPattern.add(new BlockPattern(block), chance);
        }

        return randomPattern;
    }
}
