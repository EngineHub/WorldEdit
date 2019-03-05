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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;

import javax.annotation.Nullable;
import java.util.function.IntSupplier;

/**
 * A mask that evaluates an expression.
 *
 * <p>Expressions are evaluated as {@code true} if they return a value
 * greater than {@code 0}.</p>
 */
public class ExpressionMask extends AbstractMask {

    private final Expression expression;
    private final IntSupplier timeout;

    /**
     * Create a new instance.
     *
     * @param expression the expression
     * @throws ExpressionException thrown if there is an error with the expression
     */
    public ExpressionMask(String expression) throws ExpressionException {
        this(Expression.compile(checkNotNull(expression), "x", "y", "z"));
    }

    /**
     * Create a new instance.
     *
     * @param expression the expression
     */
    public ExpressionMask(Expression expression) {
        this(expression, null);
    }

    public ExpressionMask(Expression expression, @Nullable IntSupplier timeout) {
        checkNotNull(expression);
        this.expression = expression;
        this.timeout = timeout;
    }

    @Override
    public boolean test(BlockVector3 vector) {
        try {
            if (expression.getEnvironment() instanceof WorldEditExpressionEnvironment) {
                ((WorldEditExpressionEnvironment) expression.getEnvironment()).setCurrentBlock(vector.toVector3());
            }
            if (timeout == null) {
                return expression.evaluate(vector.getX(), vector.getY(), vector.getZ()) > 0;
            } else {
                return expression.evaluate(new double[]{vector.getX(), vector.getY(), vector.getZ()},
                        timeout.getAsInt()) > 0;
            }
        } catch (EvaluationException e) {
            return false;
        }
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return new ExpressionMask2D(expression, timeout);
    }

}
