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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomStatePattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.FuzzyBlockState;

public class RandomStatePatternParser extends InputParser<Pattern> {
    public RandomStatePatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("*")) {
            return null;
        }

        boolean wasFuzzy = context.isPreferringWildcard();
        context.setPreferringWildcard(true);
        BaseBlock block = worldEdit.getBlockFactory().parseFromInput(input.substring(1), context);
        context.setPreferringWildcard(wasFuzzy);
        if (block.getStates().size() == block.getBlockType().getPropertyMap().size()) {
            // they requested random with *, but didn't leave any states empty - simplify
            return new BlockPattern(block);
        } else if (block.toImmutableState() instanceof FuzzyBlockState) {
            return new RandomStatePattern((FuzzyBlockState) block.toImmutableState());
        } else {
            return null; // only should happen if parseLogic changes
        }
    }
}
