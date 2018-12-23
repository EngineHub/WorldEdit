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

package com.sk89q.worldedit.internal.registry;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;

import java.util.List;

/**
 * An input parser that only performs a single function from aliases.
 *
 * @param <E> the element
 */
public abstract class SimpleInputParser<E> extends InputParser<E> {

    public SimpleInputParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    /**
     * The strings this parser matches
     *
     * @return the matching aliases
     */
    public abstract List<String> getMatchedAliases();

    @Override
    public E parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!getMatchedAliases().contains(input)) {
            return null;
        }

        return parseFromSimpleInput(input, context);
    }

    public abstract E parseFromSimpleInput(String input, ParserContext context) throws InputParseException;

    /**
     * Gets the primary name of this matcher
     *
     * @return the primary match
     */
    public String getPrimaryMatcher() {
        return getMatchedAliases().get(0);
    }

    @Override
    public List<String> getSuggestions() {
        return Lists.newArrayList(getPrimaryMatcher());
    }
}
