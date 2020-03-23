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

import com.sk89q.worldedit.antlr.ExpressionBaseListener;
import com.sk89q.worldedit.antlr.ExpressionParser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.sk89q.worldedit.internal.expression.ExpressionHelper.check;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.resolveFunction;

class ExpressionValidator extends ExpressionBaseListener {

    private final Set<String> variableNames = new HashSet<>();
    private final Functions functions;

    ExpressionValidator(Collection<String> variableNames,
                        Functions functions) {
        this.variableNames.addAll(variableNames);
        this.functions = functions;
    }

    private void bindVariable(String name) {
        variableNames.add(name);
    }

    @Override
    public void enterAssignment(ExpressionParser.AssignmentContext ctx) {
        bindVariable(ctx.target.getText());
    }

    @Override
    public void enterSimpleForStatement(ExpressionParser.SimpleForStatementContext ctx) {
        bindVariable(ctx.counter.getText());
    }

    @Override
    public void enterIdExpr(ExpressionParser.IdExprContext ctx) {
        String text = ctx.source.getText();
        check(variableNames.contains(text), ctx,
            "Variable '" + text + "' is not bound");
    }

    @Override
    public void enterFunctionCall(ExpressionParser.FunctionCallContext ctx) {
        resolveFunction(functions, ctx);
    }
}
