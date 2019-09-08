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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An abstract implementation of a factory for internal usage.
 *
 * @param <E> the element that the factory returns
 */
@SuppressWarnings("ProtectedField")
public abstract class AbstractFactory<E> {

    protected final WorldEdit worldEdit;
    private final List<InputParser<E>> parsers = new ArrayList<>();

    /**
     * Create a new factory.
     *
     * @param worldEdit the WorldEdit instance
     * @param defaultParser the parser to fall back to
     */
    protected AbstractFactory(WorldEdit worldEdit, InputParser<E> defaultParser) {
        checkNotNull(worldEdit);
        checkNotNull(defaultParser);
        this.worldEdit = worldEdit;
        this.parsers.add(defaultParser);
    }

    /**
     * Gets an immutable list of parsers.
     *
     * To add parsers, use the register method.
     *
     * @return the parsers
     */
    public List<InputParser<E>> getParsers() {
        return Collections.unmodifiableList(parsers);
    }

    public E parseFromInput(String input, ParserContext context) throws InputParseException {
        E match;

        for (InputParser<E> parser : parsers) {
            match = parser.parseFromInput(input, context);

            if (match != null) {
                return match;
            }
        }

        throw new NoMatchException("No match for '" + input + "'");
    }

    public List<String> getSuggestions(String input) {
        return parsers.stream().flatMap(
                p -> p.getSuggestions(input)
        ).collect(Collectors.toList());
    }

    /**
     * Registers an InputParser to this factory
     *
     * @param inputParser The input parser
     */
    public void register(InputParser<E> inputParser) {
        checkNotNull(inputParser);

        parsers.add(parsers.size() - 1, inputParser);
    }
}
