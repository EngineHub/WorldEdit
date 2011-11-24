// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.expression.runtime;

/**
 * A Java/C-style for loop.
 *
 * @author TomyLobo
 */
public class For extends Node {
    RValue init;
    RValue condition;
    RValue increment;
    RValue body;

    public For(int position, RValue init, RValue condition, RValue increment, RValue body) {
        super(position);

        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public double getValue() throws EvaluationException {
        int iterations = 0;
        double ret = 0.0;

        for (init.getValue(); condition.getValue() > 0; increment.getValue()) {
            if (iterations > 256) {
                throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
            }
            ++iterations;

            try {
                ret = body.getValue();
            } catch (BreakException e) {
                if (e.doContinue) {
                    continue;
                } else {
                    break;
                }
            }
        }

        return ret;
    }

    @Override
    public char id() {
        return 'F';
    }

    @Override
    public String toString() {
        return "for (" + init + "; " + condition + "; " + increment + ") { " + body + " }";
    }

    //TODO: optimizer
}
