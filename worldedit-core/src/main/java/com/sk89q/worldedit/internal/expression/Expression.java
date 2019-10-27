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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.antlr.ExpressionLexer;
import com.sk89q.worldedit.antlr.ExpressionParser;
import com.sk89q.worldedit.internal.expression.invoke.ExpressionCompiler;
import com.sk89q.worldedit.session.request.Request;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Compiles and evaluates expressions.
 *
 * <p>Supported operators:</p>
 *
 * <ul>
 *     <li>Logical: &amp;&amp;, ||, ! (unary)</li>
 *     <li>Bitwise: ~ (unary), &gt;&gt;, &lt;&lt;</li>
 *     <li>Arithmetic: +, -, *, /, % (modulo), ^ (power), - (unary), --, ++ (prefix only)</li>
 *     <li>Comparison: &lt;=, &gt;=, &gt;, &lt;, ==, !=, ~= (near)</li>
 * </ul>
 *
 * <p>Supported functions: abs, acos, asin, atan, atan2, cbrt, ceil, cos, cosh,
 * exp, floor, ln, log, log10, max, max, min, min, rint, round, sin, sinh,
 * sqrt, tan, tanh and more. (See the Functions class or the wiki)</p>
 *
 * <p>Constants: e, pi</p>
 *
 * <p>To compile an equation, run
 * {@code Expression.compile("expression here", "var1", "var2"...)}.
 * If you wish to run the equation multiple times, you can then optimize it,
 * by calling {@link #optimize()}. You can then run the equation as many times
 * as you want by calling {@link #evaluate(double...)}. You do not need to
 * pass values for all slots specified while compiling.
 * To query slots after evaluation, you can use the {@linkplain #getSlots() slot table}.
 */
public class Expression {

    private static final ThreadLocal<Stack<Expression>> instance = new ThreadLocal<>();
    private static final ExecutorService evalThread = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("worldedit-expression-eval-%d")
            .build());

    private final SlotTable slots = new SlotTable();
    private final List<String> providedSlots;
    private final ExpressionParser.AllStatementsContext root;
    private final SetMultimap<String, MethodHandle> functions = Functions.getFunctionMap();
    private ExpressionEnvironment environment;
    private final CompiledExpression compiledExpression;

    public static Expression compile(String expression, String... variableNames) throws ExpressionException {
        return new Expression(expression, variableNames);
    }

    private Expression(String expression, String... variableNames) throws ExpressionException {
        slots.putSlot("e", new LocalSlot.Constant(Math.E));
        slots.putSlot("pi", new LocalSlot.Constant(Math.PI));
        slots.putSlot("true", new LocalSlot.Constant(1));
        slots.putSlot("false", new LocalSlot.Constant(0));

        for (String variableName : variableNames) {
            slots.initVariable(variableName)
                .orElseThrow(() -> new ExpressionException(-1,
                    "Tried to overwrite identifier '" + variableName + "'"));
        }
        this.providedSlots = ImmutableList.copyOf(variableNames);

        CharStream cs = CharStreams.fromString(expression, "<input>");
        ExpressionLexer lexer = new ExpressionLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new LexerErrorListener());
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ParserErrorListener());
        try {
            root = parser.allStatements();
            Objects.requireNonNull(root, "Unable to parse root, but no exceptions?");
        } catch (ParseCancellationException e) {
            throw new ParserException(parser.getState(), e);
        }
        ParseTreeWalker.DEFAULT.walk(new ExpressionValidator(slots.keySet(), functions), root);
        this.compiledExpression = new ExpressionCompiler().compileExpression(root, functions);
    }

    public double evaluate(double... values) throws EvaluationException {
        return evaluate(values, WorldEdit.getInstance().getConfiguration().calculationTimeout);
    }

    public double evaluate(double[] values, int timeout) throws EvaluationException {
        for (int i = 0; i < values.length; ++i) {
            String slotName = providedSlots.get(i);
            LocalSlot.Variable slot = slots.getVariable(slotName)
                .orElseThrow(() -> new EvaluationException(-1,
                    "Tried to assign to non-variable " + slotName + "."));

            slot.setValue(values[i]);
        }

        // evaluation exceptions are thrown out of this method
        if (timeout < 0) {
            return evaluateRoot();
        }
        return evaluateRootTimed(timeout);
    }

    private double evaluateRootTimed(int timeout) throws EvaluationException {
        CountDownLatch startLatch = new CountDownLatch(1);
        Request request = Request.request();
        Future<Double> result = evalThread.submit(() -> {
            Request local = Request.request();
            local.setSession(request.getSession());
            local.setWorld(request.getWorld());
            local.setEditSession(request.getEditSession());
            try {
                startLatch.countDown();
                return Expression.this.evaluateRoot();
            } finally {
                Request.reset();
            }
        });
        try {
            startLatch.await();
            return result.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            result.cancel(true);
            throw new ExpressionTimeoutException("Calculations exceeded time limit.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            Throwables.throwIfInstanceOf(cause, EvaluationException.class);
            Throwables.throwIfUnchecked(cause);
            throw new RuntimeException(cause);
        }
    }

    private Double evaluateRoot() throws EvaluationException {
        pushInstance();
        try {
            return compiledExpression.execute(new ExecutionData(slots, functions));
        } finally {
            popInstance();
        }
    }

    public void optimize() {
        // TODO optimizing
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public SlotTable getSlots() {
        return slots;
    }

    public static Expression getInstance() {
        return instance.get().peek();
    }

    private void pushInstance() {
        Stack<Expression> threadLocalExprStack = instance.get();
        if (threadLocalExprStack == null) {
            instance.set(threadLocalExprStack = new Stack<>());
        }

        threadLocalExprStack.push(this);
    }

    private void popInstance() {
        Stack<Expression> threadLocalExprStack = instance.get();

        threadLocalExprStack.pop();

        if (threadLocalExprStack.isEmpty()) {
            instance.set(null);
        }
    }

    public ExpressionEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ExpressionEnvironment environment) {
        this.environment = environment;
    }

}
