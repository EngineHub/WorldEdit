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

package com.sk89q.worldedit.internal.registry;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;

import java.util.stream.Stream;

/**
 * Input parser interface for {@link AbstractFactory}.
 *
 * @param <E> the element
 */
@SuppressWarnings("ProtectedField")
public abstract class InputParser<E> {

    protected final WorldEdit worldEdit;

    protected InputParser(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    public abstract E parseFromInput(String input, ParserContext context) throws InputParseException;

    /**
     * Gets a stream of suggestions of input to this parser.
     *
     * @return a stream of suggestions
     */
    public Stream<String> getSuggestions(String input) {
        return Stream.empty();
    }
}
