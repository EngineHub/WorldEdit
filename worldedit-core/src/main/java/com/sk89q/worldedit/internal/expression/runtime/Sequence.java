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

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.parser.ParserException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sequence of operations, usually separated by semicolons in the
 * input stream.
 */
public class Sequence extends Node {

    final RValue[] sequence;

    public Sequence(int position, RValue... sequence) {
        super(position);

        this.sequence = sequence;
    }

    @Override
    public char id() {
        return 's';
    }

    @Override
    public double getValue() throws EvaluationException {
        double ret = 0;
        for (RValue invokable : sequence) {
            ret = invokable.getValue();
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("seq(");
        boolean first = true;
        for (RValue invokable : sequence) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(invokable);
            first = false;
        }

        return sb.append(')').toString();
    }

    @Override
    public RValue optimize() throws EvaluationException {
        final List<RValue> newSequence = new ArrayList<>();

        RValue droppedLast = null;
        for (RValue invokable : sequence) {
            droppedLast = null;
            invokable = invokable.optimize();
            if (invokable instanceof Sequence) {
                Collections.addAll(newSequence, ((Sequence) invokable).sequence);
            } else if (invokable instanceof Constant) {
                droppedLast = invokable;
            } else {
                newSequence.add(invokable);
            }
        }

        if (droppedLast != null) {
            newSequence.add(droppedLast);
        }

        if (newSequence.size() == 1) {
            return newSequence.get(0);
        }

        return new Sequence(getPosition(), newSequence.toArray(new RValue[newSequence.size()]));
    }

    @Override
    public RValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        for (int i = 0; i < sequence.length; ++i) {
            sequence[i] = sequence[i].bindVariables(expression, false);
        }

        return this;
    }

}
