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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.math.BlockVector2;

import javax.annotation.Nullable;
import java.util.function.IntSupplier;

public class ExpressionMask2D extends AbstractMask2D {

    private final Expression expression;
    private final IntSupplier timeout;

    /**
     * Create a new instance.
     *
     * @param expression the expression
     * @throws ExpressionException thrown if there is an error with the expression
     */
    public ExpressionMask2D(String expression) throws ExpressionException {
        this(Expression.compile(checkNotNull(expression), "x", "z"));
    }

    /**
     * Create a new instance.
     *
     * @param expression the expression
     */
    public ExpressionMask2D(Expression expression) {
        this(expression, null);
    }

    public ExpressionMask2D(Expression expression, @Nullable IntSupplier timeout) {
        checkNotNull(expression);
        this.expression = expression;
        this.timeout = timeout;
    }

    @Override
    public boolean test(BlockVector2 vector) {
        try {
            if (timeout != null) {
                return expression.evaluate(vector.getX(), 0, vector.getZ()) > 0;
            } else {
                return expression.evaluate(new double[]{vector.getX(), 0, vector.getZ()}, timeout.getAsInt()) > 0;
            }
        } catch (EvaluationException e) {
            return false;
        }
    }

}
