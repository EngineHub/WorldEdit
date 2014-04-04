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

package com.sk89q.worldedit.extension.registry;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.internal.registry.AbstractRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * A registry of known {@link BaseBlock}s. Provides methods to instantiate
 * new blocks from input.
 * </p>
 * Instances of this class can be taken from
 * {@link WorldEdit#getBlockRegistry()}.
 */
public class BlockRegistry extends AbstractRegistry<BaseBlock> {

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance.
     */
    public BlockRegistry(WorldEdit worldEdit) {
        super(worldEdit);

        parsers.add(new DefaultBlockParser(worldEdit));
    }

    /**
     * Return a set of blocks from a comma-delimited list of blocks.
     *
     * @param input the input
     * @param context the context
     * @return a set of blocks
     * @throws InputParseException thrown in error with the input
     */
    public Set<BaseBlock> parseFromListInput(String input, ParserContext context) throws InputParseException {
        Set<BaseBlock> blocks = new HashSet<BaseBlock>();
        for (String token : input.split(",")) {
            blocks.add(parseFromInput(token, context));
        }
        return blocks;
    }

}
