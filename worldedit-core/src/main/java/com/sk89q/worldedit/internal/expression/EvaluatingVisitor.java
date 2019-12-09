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
import com.google.common.collect.SetMultimap;
import com.sk89q.worldedit.antlr.ExpressionBaseVisitor;
import com.sk89q.worldedit.antlr.ExpressionParser;
import it.unimi.dsi.fastutil.doubles.Double2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.antlr.ExpressionLexer.ASSIGN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.DIVIDE;
import static com.sk89q.worldedit.antlr.ExpressionLexer.DIVIDE_ASSIGN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.EQUAL;
import static com.sk89q.worldedit.antlr.ExpressionLexer.EXCLAMATION_MARK;
import static com.sk89q.worldedit.antlr.ExpressionLexer.GREATER_THAN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.GREATER_THAN_OR_EQUAL;
import static com.sk89q.worldedit.antlr.ExpressionLexer.INCREMENT;
import static com.sk89q.worldedit.antlr.ExpressionLexer.LEFT_SHIFT;
import static com.sk89q.worldedit.antlr.ExpressionLexer.LESS_THAN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.LESS_THAN_OR_EQUAL;
import static com.sk89q.worldedit.antlr.ExpressionLexer.MINUS;
import static com.sk89q.worldedit.antlr.ExpressionLexer.MINUS_ASSIGN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.MODULO;
import static com.sk89q.worldedit.antlr.ExpressionLexer.MODULO_ASSIGN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.NEAR;
import static com.sk89q.worldedit.antlr.ExpressionLexer.NOT_EQUAL;
import static com.sk89q.worldedit.antlr.ExpressionLexer.PLUS;
import static com.sk89q.worldedit.antlr.ExpressionLexer.PLUS_ASSIGN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.POWER_ASSIGN;
import static com.sk89q.worldedit.antlr.ExpressionLexer.RIGHT_SHIFT;
import static com.sk89q.worldedit.antlr.ExpressionLexer.TIMES;
import static com.sk89q.worldedit.antlr.ExpressionLexer.TIMES_ASSIGN;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.WRAPPED_CONSTANT;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.check;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.checkIterations;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.checkTimeout;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.evalException;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.getArgumentHandleName;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.resolveFunction;

class EvaluatingVisitor extends ExpressionBaseVisitor<Double> {

    private final SlotTable slots;
    private final SetMultimap<String, MethodHandle> functions;

    EvaluatingVisitor(SlotTable slots,
                      SetMultimap<String, MethodHandle> functions) {
        this.slots = slots;
        this.functions = functions;
    }

    private LocalSlot.Variable initVariable(String name, ParserRuleContext ctx) {
        return slots.initVariable(name)
            .orElseThrow(() -> evalException(ctx, "Cannot overwrite non-variable '" + name + "'"));
    }

    private Supplier<EvaluationException> varNotInitException(String name, ParserRuleContext ctx) {
        return () -> evalException(ctx, "'" + name + "' is not initialized yet");
    }

    private LocalSlot.Variable getVariable(String name, ParserRuleContext ctx) {
        LocalSlot slot = slots.getSlot(name)
            .orElseThrow(varNotInitException(name, ctx));
        check(slot instanceof LocalSlot.Variable, ctx, "'" + name + "' is not a variable");
        return (LocalSlot.Variable) slot;
    }

    private double getSlotValue(String name, ParserRuleContext ctx) {
        return slots.getSlotValue(name)
            .orElseThrow(varNotInitException(name, ctx));
    }

    private Token extractToken(ParserRuleContext ctx) {
        List<TerminalNode> children = ctx.children.stream()
            .filter(TerminalNode.class::isInstance)
            .map(TerminalNode.class::cast)
            .collect(Collectors.toList());
        check(children.size() == 1, ctx, "Expected exactly one token, got " + children.size());
        return children.get(0).getSymbol();
    }

    private Double evaluate(ParserRuleContext ctx) {
        return ctx.accept(this);
    }

    private double evaluateForValue(ParserRuleContext ctx) {
        Double result = evaluate(ctx);
        check(result != null, ctx, "Invalid expression for a value");
        return result;
    }

    private boolean evaluateBoolean(ParserRuleContext boolExpression) {
        Double bool = evaluate(boolExpression);
        check(bool != null, boolExpression, "Invalid expression for boolean");
        return doubleToBool(bool);
    }

    private boolean doubleToBool(double bool) {
        return bool > 0;
    }

    private double boolToDouble(boolean bool) {
        return bool ? 1 : 0;
    }

    private Double evaluateConditional(ParserRuleContext condition,
                                       ParserRuleContext trueBranch,
                                       ParserRuleContext falseBranch) {
        ParserRuleContext ctx = evaluateBoolean(condition) ? trueBranch : falseBranch;
        return ctx == null ? null : evaluate(ctx);
    }

    @Override
    public Double visitIfStatement(ExpressionParser.IfStatementContext ctx) {
        return evaluateConditional(ctx.condition, ctx.trueBranch, ctx.falseBranch);
    }

    @Override
    public Double visitTernaryExpr(ExpressionParser.TernaryExprContext ctx) {
        return evaluateConditional(ctx.condition, ctx.trueBranch, ctx.falseBranch);
    }

    @Override
    public Double visitWhileStatement(ExpressionParser.WhileStatementContext ctx) {
        Double result = defaultResult();
        int iterations = 0;
        while (evaluateBoolean(ctx.condition)) {
            checkIterations(iterations, ctx.body);
            checkTimeout();
            iterations++;
            try {
                result = evaluate(ctx.body);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Double visitDoStatement(ExpressionParser.DoStatementContext ctx) {
        Double result = defaultResult();
        int iterations = 0;
        do {
            checkIterations(iterations, ctx.body);
            checkTimeout();
            iterations++;
            try {
                result = evaluate(ctx.body);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
        } while (evaluateBoolean(ctx.condition));
        return result;
    }

    @Override
    public Double visitForStatement(ExpressionParser.ForStatementContext ctx) {
        Double result = defaultResult();
        int iterations = 0;
        evaluate(ctx.init);
        while (evaluateBoolean(ctx.condition)) {
            checkIterations(iterations, ctx.body);
            checkTimeout();
            iterations++;
            try {
                result = evaluate(ctx.body);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
            evaluate(ctx.update);
        }
        return result;
    }

    @Override
    public Double visitSimpleForStatement(ExpressionParser.SimpleForStatementContext ctx) {
        Double result = defaultResult();
        int iterations = 0;
        double first = evaluateForValue(ctx.first);
        double last = evaluateForValue(ctx.last);
        String counter = ctx.counter.getText();
        LocalSlot.Variable variable = initVariable(counter, ctx);
        for (double i = first; i <= last; i++) {
            checkIterations(iterations, ctx.body);
            checkTimeout();
            iterations++;
            variable.setValue(i);
            try {
                result = evaluate(ctx.body);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Double visitBreakStatement(ExpressionParser.BreakStatementContext ctx) {
        throw new BreakException(false);
    }

    @Override
    public Double visitContinueStatement(ExpressionParser.ContinueStatementContext ctx) {
        throw new BreakException(true);
    }

    @Override
    public Double visitReturnStatement(ExpressionParser.ReturnStatementContext ctx) {
        if (ctx.value != null) {
            return evaluate(ctx.value);
        }
        return null;
    }

    @Override
    public Double visitSwitchStatement(ExpressionParser.SwitchStatementContext ctx) {
        Double2ObjectMap<ParserRuleContext> cases = new Double2ObjectLinkedOpenHashMap<>(ctx.labels.size());
        ParserRuleContext defaultCase = null;
        for (int i = 0; i < ctx.labels.size(); i++) {
            ExpressionParser.SwitchLabelContext label = ctx.labels.get(i);
            ExpressionParser.StatementsContext body = ctx.bodies.get(i);
            if (label instanceof ExpressionParser.CaseContext) {
                ExpressionParser.CaseContext caseContext = (ExpressionParser.CaseContext) label;
                double key = evaluateForValue(caseContext.constant);
                check(!cases.containsKey(key), body, "Duplicate cases detected.");
                cases.put(key, body);
            } else {
                check(defaultCase == null, body, "Duplicate default cases detected.");
                defaultCase = body;
            }
        }
        double value = evaluateForValue(ctx.target);
        boolean matched = false;
        Double evaluated = null;
        boolean falling = false;
        for (Double2ObjectMap.Entry<ParserRuleContext> entry : Double2ObjectMaps.fastIterable(cases)) {
            if (falling || entry.getDoubleKey() == value) {
                matched = true;
                try {
                    evaluated = evaluate(entry.getValue());
                    falling = true;
                } catch (BreakException brk) {
                    check(!brk.doContinue, entry.getValue(), "Cannot continue in a switch");
                    falling = false;
                    break;
                }
            }
        }
        // This if is like the one in the loop, default's "case" is `!matched` & present
        if ((falling || !matched) && defaultCase != null) {
            try {
                evaluated = evaluate(defaultCase);
            } catch (BreakException brk) {
                check(!brk.doContinue, defaultCase, "Cannot continue in a switch");
            }
        }
        return evaluated;
    }

    @Override
    public Double visitExpressionStatement(ExpressionParser.ExpressionStatementContext ctx) {
        return evaluate(ctx.expression());
    }

    @Override
    public Double visitPostCrementExpr(ExpressionParser.PostCrementExprContext ctx) {
        String target = ctx.target.getText();
        LocalSlot.Variable variable = getVariable(target, ctx);
        double value = variable.getValue();
        if (ctx.op.getType() == INCREMENT) {
            value++;
        } else {
            value--;
        }
        variable.setValue(value);
        return value;
    }

    @Override
    public Double visitPreCrementExpr(ExpressionParser.PreCrementExprContext ctx) {
        String target = ctx.target.getText();
        LocalSlot.Variable variable = getVariable(target, ctx);
        double value = variable.getValue();
        double result = value;
        if (ctx.op.getType() == INCREMENT) {
            value++;
        } else {
            value--;
        }
        variable.setValue(value);
        return result;
    }

    @Override
    public Double visitPlusMinusExpr(ExpressionParser.PlusMinusExprContext ctx) {
        double value = evaluateForValue(ctx.expr);
        switch (ctx.op.getType()) {
            case PLUS:
                return +value;
            case MINUS:
                return -value;
        }
        throw evalException(ctx, "Invalid text for plus/minus expr: " + ctx.op.getText());
    }

    @Override
    public Double visitNotExpr(ExpressionParser.NotExprContext ctx) {
        return boolToDouble(!evaluateBoolean(ctx.expr));
    }

    @Override
    public Double visitComplementExpr(ExpressionParser.ComplementExprContext ctx) {
        return (double) ~(long) evaluateForValue(ctx.expr);
    }

    @Override
    public Double visitConditionalAndExpr(ExpressionParser.ConditionalAndExprContext ctx) {
        if (!evaluateBoolean(ctx.left)) {
            return boolToDouble(false);
        }
        return evaluateForValue(ctx.right);
    }

    @Override
    public Double visitConditionalOrExpr(ExpressionParser.ConditionalOrExprContext ctx) {
        double left = evaluateForValue(ctx.left);
        if (doubleToBool(left)) {
            return left;
        }
        return evaluateForValue(ctx.right);
    }

    private double evaluateBinary(ParserRuleContext left,
                                  ParserRuleContext right,
                                  DoubleBinaryOperator op) {
        return op.applyAsDouble(evaluateForValue(left), evaluateForValue(right));
    }

    @Override
    public Double visitPowerExpr(ExpressionParser.PowerExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, Math::pow);
    }

    @Override
    public Double visitMultiplicativeExpr(ExpressionParser.MultiplicativeExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, (l, r) -> {
            switch (ctx.op.getType()) {
                case TIMES:
                    return l * r;
                case DIVIDE:
                    return l / r;
                case MODULO:
                    return l % r;
            }
            throw evalException(ctx, "Invalid text for multiplicative expr: " + ctx.op.getText());
        });
    }

    @Override
    public Double visitAddExpr(ExpressionParser.AddExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, (l, r) -> {
            switch (ctx.op.getType()) {
                case PLUS:
                    return l + r;
                case MINUS:
                    return l - r;
            }
            throw evalException(ctx, "Invalid text for additive expr: " + ctx.op.getText());
        });
    }

    @Override
    public Double visitShiftExpr(ExpressionParser.ShiftExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, (l, r) -> {
            switch (ctx.op.getType()) {
                case LEFT_SHIFT:
                    return (double) ((long) l << (long) r);
                case RIGHT_SHIFT:
                    return (double) ((long) l >> (long) r);
            }
            throw evalException(ctx, "Invalid text for shift expr: " + ctx.op.getText());
        });
    }

    @Override
    public Double visitRelationalExpr(ExpressionParser.RelationalExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, (l, r) -> {
            switch (ctx.op.getType()) {
                case LESS_THAN:
                    return boolToDouble(l < r);
                case LESS_THAN_OR_EQUAL:
                    return boolToDouble(l <= r);
                case GREATER_THAN:
                    return boolToDouble(l > r);
                case GREATER_THAN_OR_EQUAL:
                    return boolToDouble(l >= r);
            }
            throw evalException(ctx, "Invalid text for relational expr: " + ctx.op.getText());
        });
    }

    @Override
    public Double visitEqualityExpr(ExpressionParser.EqualityExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, (l, r) -> {
            switch (ctx.op.getType()) {
                case EQUAL:
                    return boolToDouble(l == r);
                case NOT_EQUAL:
                    return boolToDouble(l != r);
                case NEAR:
                    return boolToDouble(almostEqual2sComplement(l, r, 450359963L));
                case GREATER_THAN_OR_EQUAL:
                    return boolToDouble(l >= r);
            }
            throw evalException(ctx, "Invalid text for equality expr: " + ctx.op.getText());
        });
    }

    // Usable AlmostEqual function, based on http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm
    private static boolean almostEqual2sComplement(double a, double b, long maxUlps) {
        // Make sure maxUlps is non-negative and small enough that the
        // default NAN won't compare as equal to anything.
        //assert(maxUlps > 0 && maxUlps < 4 * 1024 * 1024); // this is for floats, not doubles

        long aLong = Double.doubleToRawLongBits(a);
        // Make aLong lexicographically ordered as a twos-complement long
        if (aLong < 0) aLong = 0x8000000000000000L - aLong;

        long bLong = Double.doubleToRawLongBits(b);
        // Make bLong lexicographically ordered as a twos-complement long
        if (bLong < 0) bLong = 0x8000000000000000L - bLong;

        final long longDiff = Math.abs(aLong - bLong);
        return longDiff <= maxUlps;
    }

    @Override
    public Double visitPostfixExpr(ExpressionParser.PostfixExprContext ctx) {
        double value = evaluateForValue(ctx.expr);
        if (ctx.op.getType() == EXCLAMATION_MARK) {
            return factorial(value);
        }
        throw evalException(ctx,
            "Invalid text for post-unary expr: " + ctx.op.getText());
    }

    private static final double[] factorials = new double[171];

    static {
        factorials[0] = 1;
        for (int i = 1; i < factorials.length; ++i) {
            factorials[i] = factorials[i - 1] * i;
        }
    }

    private static double factorial(double x) throws EvaluationException {
        final int n = (int) x;

        if (n < 0) {
            return 0;
        }

        if (n >= factorials.length) {
            return Double.POSITIVE_INFINITY;
        }

        return factorials[n];
    }

    @Override
    public Double visitAssignment(ExpressionParser.AssignmentContext ctx) {
        int type = extractToken(ctx.assignmentOperator()).getType();
        String target = ctx.target.getText();
        double value;
        double arg = evaluateForValue(ctx.expression());
        LocalSlot.Variable variable;
        if (type == ASSIGN) {
            variable = initVariable(target, ctx);
            value = arg;
        } else {
            variable = getVariable(target, ctx);
            value = variable.getValue();
            switch (type) {
                case POWER_ASSIGN:
                    value = Math.pow(value, arg);
                    break;
                case TIMES_ASSIGN:
                    value *= arg;
                    break;
                case DIVIDE_ASSIGN:
                    value /= arg;
                    break;
                case MODULO_ASSIGN:
                    value %= arg;
                    break;
                case PLUS_ASSIGN:
                    value += arg;
                    break;
                case MINUS_ASSIGN:
                    value -= arg;
                    break;
                default:
                    throw evalException(ctx, "Invalid text for assign expr: " +
                        ctx.assignmentOperator().getText());
            }
        }
        variable.setValue(value);
        return value;
    }

    @Override
    public Double visitFunctionCall(ExpressionParser.FunctionCallContext ctx) {
        MethodHandle handle = resolveFunction(functions, ctx);
        String fnName = ctx.name.getText();
        Object[] arguments = new Object[ctx.args.size()];
        for (int i = 0; i < arguments.length; i++) {
            ExpressionParser.ExpressionContext arg = ctx.args.get(i);
            Object transformed = getArgument(fnName, handle.type(), i, arg);
            arguments[i] = transformed;
        }
        try {
            // Some methods return other Numbers
            Number number = (Number) handle.invokeWithArguments(arguments);
            return number == null ? null : number.doubleValue();
        } catch (Throwable throwable) {
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }
    }

    private Object getArgument(String fnName, MethodType type, int i, ParserRuleContext arg) {
        // Pass variable handle in for modification?
        String handleName = getArgumentHandleName(fnName, type, i, arg);
        if (handleName == null) {
            return evaluateForValue(arg);
        }
        if (handleName.equals(WRAPPED_CONSTANT)) {
            return new LocalSlot.Constant(evaluateForValue(arg));
        }
        return getVariable(handleName, arg);
    }

    @Override
    public Double visitConstantExpression(ExpressionParser.ConstantExpressionContext ctx) {
        try {
            return Double.parseDouble(ctx.getText());
        } catch (NumberFormatException e) {
            // Rare, but might happen, e.g. if too many digits
            throw evalException(ctx, "Invalid constant: " + e.getMessage());
        }
    }

    @Override
    public Double visitIdExpr(ExpressionParser.IdExprContext ctx) {
        String source = ctx.source.getText();
        return getSlotValue(source, ctx);
    }

    @Override
    public Double visitChildren(RuleNode node) {
        Double result = defaultResult();
        int n = node.getChildCount();
        for (int i = 0; i < n; i++) {
            ParseTree c = node.getChild(i);
            if (c instanceof TerminalNode && ((TerminalNode) c).getSymbol().getType() == Token.EOF) {
                break;
            }

            Double childResult = c.accept(this);
            if (c instanceof ExpressionParser.ReturnStatementContext) {
                return childResult;
            }
            result = aggregateResult(result, childResult);
        }

        return result;
    }

    @Override
    protected Double aggregateResult(Double aggregate, Double nextResult) {
        return Optional.ofNullable(nextResult).orElse(aggregate);
    }
}
