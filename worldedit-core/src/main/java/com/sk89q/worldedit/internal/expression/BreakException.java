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
 * Thrown when a break or continue is encountered.
 * Loop constructs catch this exception.
 */
public class BreakException extends RuntimeException {

    public static final BreakException BREAK = new BreakException(false);
    public static final BreakException CONTINUE = new BreakException(true);

    public final boolean doContinue;

    private BreakException(boolean doContinue) {
        super(doContinue ? "'continue' encountered outside a loop" : "'break' encountered outside a loop",
            null, true, false);

        this.doContinue = doContinue;
    }

}
