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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;

import static com.google.common.base.Preconditions.checkNotNull;

public class ExpressionMask2D extends AbstractMask2D {

    private final Expression expression;

    /**
     * Create a new instance.
     *
     * @param expression the expression
     * @throws ExpressionException thrown if there is an error with the expression
     */
    public ExpressionMask2D(String expression) throws ExpressionException {
        checkNotNull(expression);
        this.expression = Expression.compile(expression, "x", "z");
    }

    /**
     * Create a new instance.
     *
     * @param expression the expression
     */
    public ExpressionMask2D(Expression expression) {
        checkNotNull(expression);
        this.expression = expression;
    }

    @Override
    public boolean test(Vector2D vector) {
        try {
            return expression.evaluate(vector.getX(), 0, vector.getZ()) > 0;
        } catch (EvaluationException e) {
            return false;
        }
    }

}
