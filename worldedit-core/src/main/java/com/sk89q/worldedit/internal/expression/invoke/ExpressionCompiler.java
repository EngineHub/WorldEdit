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

package com.sk89q.worldedit.internal.expression.invoke;

import com.google.common.collect.SetMultimap;
import com.sk89q.worldedit.antlr.ExpressionParser;
import com.sk89q.worldedit.internal.expression.CompiledExpression;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.COMPILED_EXPRESSION_SIG;
import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.safeInvoke;
import static java.lang.invoke.MethodType.methodType;

/**
 * Compiles an expression from an AST into {@link MethodHandle}s.
 */
public class ExpressionCompiler {

    private static final String CE_EXECUTE = "execute";
    private static final MethodType HANDLE_TO_CE =
        methodType(CompiledExpression.class, MethodHandle.class);

    private static final MethodHandle HANDLE_TO_CE_CONVERTER;

    static {
        MethodHandle handleInvoker = MethodHandles.invoker(COMPILED_EXPRESSION_SIG);
        try {
            HANDLE_TO_CE_CONVERTER = LambdaMetafactory.metafactory(
                MethodHandles.lookup(),
                // Implementing CompiledExpression.execute
                CE_EXECUTE,
                // Take a handle, to be converted to CompiledExpression
                HANDLE_TO_CE,
                // Raw signature for SAM type
                COMPILED_EXPRESSION_SIG,
                // Handle to call the captured handle.
                handleInvoker,
                // Actual signature at invoke time
                COMPILED_EXPRESSION_SIG
            ).dynamicInvoker().asType(HANDLE_TO_CE);
        } catch (LambdaConversionException e) {
            throw new IllegalStateException("Failed to load ExpressionCompiler MetaFactory", e);
        }
    }

    public CompiledExpression compileExpression(ExpressionParser.AllStatementsContext root,
                                                SetMultimap<String, MethodHandle> functions) {
        MethodHandle invokable = root.accept(new CompilingVisitor(functions));
        return (CompiledExpression) safeInvoke(
            HANDLE_TO_CE_CONVERTER, h -> h.invoke(invokable)
        );
    }
}
