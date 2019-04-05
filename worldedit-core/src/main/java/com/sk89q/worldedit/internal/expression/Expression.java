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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.expression.lexer.Lexer;
import com.sk89q.worldedit.internal.expression.lexer.tokens.Token;
import com.sk89q.worldedit.internal.expression.parser.Parser;
import com.sk89q.worldedit.internal.expression.runtime.Constant;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.internal.expression.runtime.ExpressionEnvironment;
import com.sk89q.worldedit.internal.expression.runtime.ExpressionTimeoutException;
import com.sk89q.worldedit.internal.expression.runtime.Functions;
import com.sk89q.worldedit.internal.expression.runtime.RValue;
import com.sk89q.worldedit.internal.expression.runtime.ReturnException;
import com.sk89q.worldedit.internal.expression.runtime.Variable;
import com.sk89q.worldedit.session.request.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
 * pass values for all variables specified while compiling.
 * To query variables after evaluation, you can use
 * {@link #getVariable(String, boolean)}. To get a value out of these, use
 * {@link Variable#getValue()}.</p>
 *
 * <p>Variables are also supported and can be set either by passing values
 * to {@link #evaluate(double...)}.</p>
 */
public class Expression {

    private static final ThreadLocal<Stack<Expression>> instance = new ThreadLocal<>();
    private static final ExecutorService evalThread = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("worldedit-expression-eval-%d")
                    .build());

    private final Map<String, RValue> variables = new HashMap<>();
    private final String[] variableNames;
    private RValue root;
    private final Functions functions = new Functions();
    private ExpressionEnvironment environment;

    public static Expression compile(String expression, String... variableNames) throws ExpressionException {
        return new Expression(expression, variableNames);
    }

    private Expression(String expression, String... variableNames) throws ExpressionException {
        this(Lexer.tokenize(expression), variableNames);
    }

    private Expression(List<Token> tokens, String... variableNames) throws ExpressionException {
        this.variableNames = variableNames;

        variables.put("e", new Constant(-1, Math.E));
        variables.put("pi", new Constant(-1, Math.PI));
        variables.put("true", new Constant(-1, 1));
        variables.put("false", new Constant(-1, 0));

        for (String variableName : variableNames) {
            if (variables.containsKey(variableName)) {
                throw new ExpressionException(-1, "Tried to overwrite identifier '" + variableName + "'");
            }
            variables.put(variableName, new Variable(0));
        }

        root = Parser.parse(tokens, this);
    }

    public double evaluate(double... values) throws EvaluationException {
        return evaluate(values, WorldEdit.getInstance().getConfiguration().calculationTimeout);
    }

    public double evaluate(double[] values, int timeout) throws EvaluationException {
        for (int i = 0; i < values.length; ++i) {
            final String variableName = variableNames[i];
            final RValue invokable = variables.get(variableName);
            if (!(invokable instanceof Variable)) {
                throw new EvaluationException(invokable.getPosition(), "Tried to assign constant " + variableName + ".");
            }

            ((Variable) invokable).value = values[i];
        }

        try {
            if (timeout < 0) {
                return evaluateRoot();
            }
            return evaluateRootTimed(timeout);
        } catch (ReturnException e) {
            return e.getValue();
        } // other evaluation exceptions are thrown out of this method
    }

    private double evaluateRootTimed(int timeout) throws EvaluationException {
        Request request = Request.request();
        Future<Double> result = evalThread.submit(() -> {
            Request local = Request.request();
            local.setSession(request.getSession());
            local.setWorld(request.getWorld());
            local.setEditSession(request.getEditSession());
            try {
                return Expression.this.evaluateRoot();
            } finally {
                Request.reset();
            }
        });
        try {
            return result.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            result.cancel(true);
            throw new ExpressionTimeoutException("Calculations exceeded time limit.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof EvaluationException) {
                throw (EvaluationException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    private Double evaluateRoot() throws EvaluationException {
        pushInstance();
        try {
            return root.getValue();
        } finally {
            popInstance();
        }
    }

    public void optimize() throws EvaluationException {
        root = root.optimize();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public RValue getVariable(String name, boolean create) {
        RValue variable = variables.get(name);
        if (variable == null && create) {
            variables.put(name, variable = new Variable(0));
        }

        return variable;
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

    public Functions getFunctions() {
        return functions;
    }

    public ExpressionEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ExpressionEnvironment environment) {
        this.environment = environment;
    }

}
