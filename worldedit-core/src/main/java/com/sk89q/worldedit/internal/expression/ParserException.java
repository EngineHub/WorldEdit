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

package com.sk89q.worldedit.internal.expression;

/**
 * Thrown when the parser encounters a problem.
 */
public class ParserException extends ExpressionException {

    public ParserException(int position) {
        super(position, getPrefix(position));
    }

    public ParserException(int position, String message, Throwable cause) {
        super(position, getPrefix(position) + ": " + message, cause);
    }

    public ParserException(int position, String message) {
        super(position, getPrefix(position) + ": " + message);
    }

    public ParserException(int position, Throwable cause) {
        super(position, getPrefix(position), cause);
    }

    private static String getPrefix(int position) {
        return position < 0 ? "Parser error" : ("Parser error at " + (position + 1));
    }

}
