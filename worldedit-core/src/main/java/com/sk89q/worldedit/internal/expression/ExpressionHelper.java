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

import com.google.common.collect.SetMultimap;
import com.sk89q.worldedit.antlr.ExpressionParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.antlr.ExpressionLexer.ID;

class ExpressionHelper {

    static void check(boolean condition, ParserRuleContext ctx, String message) {
        if (!condition) {
            throw evalException(ctx, message);
        }
    }

    static EvaluationException evalException(ParserRuleContext ctx, String message) {
        return new EvaluationException(
            ctx.getStart().getCharPositionInLine(),
            message
        );
    }

    static void checkIterations(int iterations, ParserRuleContext ctx) {
        check(iterations <= 256, ctx, "Loop exceeded 256 iterations");
    }

    static void checkTimeout() {
        if (Thread.interrupted()) {
            throw new ExpressionTimeoutException("Calculations exceeded time limit.");
        }
    }

    static MethodHandle resolveFunction(SetMultimap<String, MethodHandle> functions,
                                        ExpressionParser.FunctionCallContext ctx) {
        String fnName = ctx.name.getText();
        Set<MethodHandle> matchingFns = functions.get(fnName);
        check(!matchingFns.isEmpty(), ctx, "Unknown function '" + fnName + "'");
        for (MethodHandle function : matchingFns) {
            MethodType type = function.type();
            // Validate argc if not varargs
            if (!function.isVarargsCollector() && type.parameterCount() != ctx.args.size()) {
                // skip non-matching function
                continue;
            }
            for (int i = 0; i < ctx.args.size(); i++) {
                ExpressionParser.ExpressionContext arg = ctx.args.get(i);
                getArgumentHandleName(fnName, type, i, arg);
            }
            // good match!
            return function;
        }
        // We matched no function, fail with appropriate message.
        String possibleCounts = matchingFns.stream()
            .map(mh -> mh.isVarargsCollector()
                ? (mh.type().parameterCount() - 1) + "+"
                : String.valueOf(mh.type().parameterCount()))
            .collect(Collectors.joining("/"));
        throw evalException(ctx, "Incorrect number of arguments for function '" + fnName + "', " +
            "expected " + possibleCounts + ", " +
            "got " + ctx.args.size());
    }

    // Special argument handle names
    /**
     * The argument should be wrapped in a {@link LocalSlot.Constant} before being passed.
     */
    static final String WRAPPED_CONSTANT = "<wrapped constant>";

    /**
     * If this argument needs a handle, returns the name of the handle needed. Otherwise, returns
     * {@code null}. If {@code arg} isn't a valid handle reference, throws.
     */
    static String getArgumentHandleName(String fnName, MethodType type, int i,
                                        ParserRuleContext arg) {
        // Pass variable handle in for modification?
        Class<?> pType = type.parameterType(i);
        Optional<String> id = tryResolveId(arg);
        if (pType == LocalSlot.Variable.class) {
            // MUST be an id
            check(id.isPresent(), arg,
                "Function '" + fnName + "' requires a variable in parameter " + i);
            return id.get();
        } else if (pType == LocalSlot.class) {
            return id.orElse(WRAPPED_CONSTANT);
        }
        return null;
    }

    private static Optional<String> tryResolveId(ParserRuleContext arg) {
        Optional<ExpressionParser.WrappedExprContext> wrappedExprContext =
            tryAs(arg, ExpressionParser.WrappedExprContext.class);
        if (wrappedExprContext.isPresent()) {
            return tryResolveId(wrappedExprContext.get().expression());
        }
        Token token = arg.start;
        int tokenType = token.getType();
        boolean isId = arg.start == arg.stop && tokenType == ID;
        return isId ? Optional.of(token.getText()) : Optional.empty();
    }

    private static <T extends ParserRuleContext> Optional<T> tryAs(
        ParserRuleContext ctx,
        Class<T> rule
    ) {
        if (rule.isInstance(ctx)) {
            return Optional.of(rule.cast(ctx));
        }
        if (ctx.children.size() != 1) {
            return Optional.empty();
        }
        List<ParserRuleContext> ctxs = ctx.getRuleContexts(ParserRuleContext.class);
        if (ctxs.size() != 1) {
            return Optional.empty();
        }
        return tryAs(ctxs.get(0), rule);
    }

    private ExpressionHelper() {
    }

}
