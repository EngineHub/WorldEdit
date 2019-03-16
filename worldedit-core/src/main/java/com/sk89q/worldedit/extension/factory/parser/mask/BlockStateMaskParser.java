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

import com.google.common.base.Splitter;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockStateMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.session.request.RequestExtent;

public class BlockStateMaskParser extends InputParser<Mask> {

    public BlockStateMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!(input.startsWith("^[") || input.startsWith("^=[")) || !input.endsWith("]")) {
            return null;
        }

        boolean strict = input.charAt(1) == '=';
        String states = input.substring(2 + (strict ? 1 : 0), input.length() - 1);
        try {
            return new BlockStateMask(new RequestExtent(),
                    Splitter.on(',').omitEmptyStrings().trimResults().withKeyValueSeparator('=').split(states),
                    strict);
        } catch (Exception e) {
            throw new InputParseException("Invalid states.", e);
        }
    }
}
