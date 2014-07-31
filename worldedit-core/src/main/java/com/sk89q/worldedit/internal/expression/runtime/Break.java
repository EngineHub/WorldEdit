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

package com.sk89q.worldedit.internal.expression.runtime;

/**
 * A break or continue statement.
 */
public class Break extends Node {

    boolean doContinue;

    public Break(int position, boolean doContinue) {
        super(position);

        this.doContinue = doContinue;
    }

    @Override
    public double getValue() throws EvaluationException {
        throw new BreakException(doContinue);
    }

    @Override
    public char id() {
        return 'b';
    }

    @Override
    public String toString() {
        return doContinue ? "continue" : "break";
    }

}
