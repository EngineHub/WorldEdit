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
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockCategoryPatternParser extends InputParser<Pattern> {

    public BlockCategoryPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public List<String> getSuggestions() {
        return BlockCategory.REGISTRY.keySet().stream().map(str -> "##" + str).collect(Collectors.toList());
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("##")) {
            return null;
        }
        String tag = input.substring(2).toLowerCase();
        boolean anyState = false;
        if (tag.startsWith("*")) {
            tag = tag.substring(1);
            anyState = true;
        }

        BlockCategory category = BlockCategory.REGISTRY.get(tag);
        if (category == null) {
            throw new InputParseException("Unknown block tag: " + tag);
        }
        RandomPattern randomPattern = new RandomPattern();

        Set<BlockType> blocks = category.getAll();
        if (blocks.isEmpty()) {
            throw new InputParseException("Block tag " + category.getId() + " had no blocks!");
        }

        if (anyState) {
            blocks.stream().flatMap(blockType -> blockType.getAllStates().stream()).forEach(state ->
                randomPattern.add(new BlockPattern(state), 1.0));
        } else {
            for (BlockType blockType : blocks) {
                randomPattern.add(new BlockPattern(blockType.getDefaultState()), 1.0);
            }
        }

        return randomPattern;
    }
}
