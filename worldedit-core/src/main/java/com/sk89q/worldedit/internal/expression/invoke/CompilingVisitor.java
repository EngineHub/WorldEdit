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
import com.sk89q.worldedit.antlr.ExpressionBaseVisitor;
import com.sk89q.worldedit.antlr.ExpressionParser;
import com.sk89q.worldedit.internal.expression.BreakException;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.ExecutionData;
import com.sk89q.worldedit.internal.expression.ExpressionHelper;
import com.sk89q.worldedit.internal.expression.LocalSlot;
import it.unimi.dsi.fastutil.doubles.Double2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
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
import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.CALL_BINARY_OP;
import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.DOUBLE_TO_BOOL;
import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.IS_NULL;
import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.NEW_LS_CONSTANT;
import static com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles.NULL_DOUBLE;
import static java.lang.invoke.MethodType.methodType;

/**
 * The brains of {@link ExpressionCompiler}.
 */
class CompilingVisitor extends ExpressionBaseVisitor<MethodHandle> {

    /*
     * General idea is that we don't need to pass around variables, they're all in ExecutionData.
     * We do need to pass that around, so most MethodHandles will be of the type
     * (ExecutionData)Double, with a few as (ExecutionData,Double)Double where it needs an existing
     * value passed in. EVERY handle returned from an overriden method must be of the first type.
     */
    private final SetMultimap<String, MethodHandle> functions;

    CompilingVisitor(SetMultimap<String, MethodHandle> functions) {
        this.functions = functions;
    }

    private Token extractToken(ParserRuleContext ctx) {
        List<TerminalNode> children = ctx.children.stream()
            .filter(TerminalNode.class::isInstance)
            .map(TerminalNode.class::cast)
            .collect(Collectors.toList());
        ExpressionHelper.check(children.size() == 1, ctx, "Expected exactly one token, got " + children.size());
        return children.get(0).getSymbol();
    }

    private ExecNode evaluate(ParserRuleContext ctx) {
        MethodHandle mh = ctx.accept(this);
        if (ctx.parent instanceof ParserRuleContext) {
            checkHandle(mh, (ParserRuleContext) ctx.parent);
        }
        return new ExecNode(ctx, mh);
    }

    private void checkHandle(MethodHandle mh, ParserRuleContext ctx) {
        ExpressionHelper.check(mh.type().equals(ExpressionHandles.COMPILED_EXPRESSION_SIG), ctx,
            "Incorrect type returned from handler for " + ctx.getClass());
    }

    private MethodHandle evaluateForNamedValue(ParserRuleContext ctx, String name) {
        MethodHandle guard = MethodHandles.guardWithTest(
            // if result is null
            IS_NULL.asType(methodType(boolean.class, Double.class)),
            // throw appropriate exception, dropping `result` argument
            MethodHandles.dropArguments(
                ExpressionHandles.throwEvalException(ctx, "Invalid expression for " + name), 0, Double.class
            ),
            // else return the argument we were passed
            MethodHandles.identity(Double.class)
        );
        // now pass `result` into `guard`
        MethodHandle result = evaluate(ctx).handle;
        return MethodHandles.collectArguments(guard, 0, result);
    }

    private MethodHandle evaluateForValue(ParserRuleContext ctx) {
        return evaluateForNamedValue(ctx, "a value");
    }

    private MethodHandle evaluateBoolean(ParserRuleContext boolExpression) {
        MethodHandle value = evaluateForNamedValue(boolExpression, "a boolean");
        value = value.asType(value.type().unwrap());
        // Pass `value` into converter, returns (ExecutionData)boolean;
        return MethodHandles.collectArguments(
            DOUBLE_TO_BOOL, 0, value
        );
    }

    private MethodHandle evaluateConditional(ParserRuleContext condition,
                                             ParserRuleContext trueBranch,
                                             ParserRuleContext falseBranch) {
        // easiest one of the bunch
        return MethodHandles.guardWithTest(
            evaluateBoolean(condition),
            trueBranch == null ? NULL_DOUBLE : evaluate(trueBranch).handle,
            falseBranch == null ? NULL_DOUBLE : evaluate(falseBranch).handle
        );
    }

    @Override
    public MethodHandle visitIfStatement(ExpressionParser.IfStatementContext ctx) {
        return evaluateConditional(ctx.condition, ctx.trueBranch, ctx.falseBranch);
    }

    @Override
    public MethodHandle visitTernaryExpr(ExpressionParser.TernaryExprContext ctx) {
        return evaluateConditional(ctx.condition, ctx.trueBranch, ctx.falseBranch);
    }

    @Override
    public MethodHandle visitWhileStatement(ExpressionParser.WhileStatementContext ctx) {
        return ExpressionHandles.whileLoop(
            evaluateBoolean(ctx.condition),
            evaluate(ctx.body)
        );
    }

    @Override
    public MethodHandle visitDoStatement(ExpressionParser.DoStatementContext ctx) {
        return ExpressionHandles.doWhileLoop(
            evaluateBoolean(ctx.condition),
            evaluate(ctx.body)
        );
    }

    @Override
    public MethodHandle visitForStatement(ExpressionParser.ForStatementContext ctx) {
        return ExpressionHandles.forLoop(
            evaluate(ctx.init).handle,
            evaluateBoolean(ctx.condition),
            evaluate(ctx.body),
            evaluate(ctx.update).handle
        );
    }

    @Override
    public MethodHandle visitSimpleForStatement(ExpressionParser.SimpleForStatementContext ctx) {
        return ExpressionHandles.simpleForLoop(
            evaluateForValue(ctx.first),
            evaluateForValue(ctx.last),
            ctx.counter,
            evaluate(ctx.body)
        );
    }

    private static final MethodHandle BREAK_STATEMENT =
        ExpressionHandles.dropData(MethodHandles.throwException(Double.class, BreakException.class)
            .bindTo(BreakException.BREAK));
    private static final MethodHandle CONTINUE_STATEMENT =
        ExpressionHandles.dropData(MethodHandles.throwException(Double.class, BreakException.class)
            .bindTo(BreakException.CONTINUE));

    @Override
    public MethodHandle visitBreakStatement(ExpressionParser.BreakStatementContext ctx) {
        return BREAK_STATEMENT;
    }

    @Override
    public MethodHandle visitContinueStatement(ExpressionParser.ContinueStatementContext ctx) {
        return CONTINUE_STATEMENT;
    }

    @Override
    public MethodHandle visitReturnStatement(ExpressionParser.ReturnStatementContext ctx) {
        if (ctx.value != null) {
            return evaluate(ctx.value).handle;
        }
        return defaultResult();
    }

    @Override
    public MethodHandle visitSwitchStatement(ExpressionParser.SwitchStatementContext ctx) {
        Double2ObjectMap<ExecNode> cases = new Double2ObjectLinkedOpenHashMap<>(ctx.labels.size());
        ExecNode defaultCase = null;
        for (int i = 0; i < ctx.labels.size(); i++) {
            ExpressionParser.SwitchLabelContext label = ctx.labels.get(i);
            ExpressionParser.StatementsContext body = ctx.bodies.get(i);
            ExecNode node = evaluate(body);
            if (label instanceof ExpressionParser.CaseContext) {
                ExpressionParser.CaseContext caseContext = (ExpressionParser.CaseContext) label;
                double key = (double) ExpressionHandles.constantInvoke(evaluateForValue(caseContext.constant));
                ExpressionHelper.check(!cases.containsKey(key), body, "Duplicate cases detected.");
                cases.put(key, node);
            } else {
                ExpressionHelper.check(defaultCase == null, body, "Duplicate default cases detected.");
                defaultCase = node;
            }
        }
        return ExpressionHandles.switchStatement(cases, evaluateForValue(ctx.target), defaultCase);
    }

    @Override
    public MethodHandle visitExpressionStatement(ExpressionParser.ExpressionStatementContext ctx) {
        return evaluate(ctx.expression()).handle;
    }

    @Override
    public MethodHandle visitPostCrementExpr(ExpressionParser.PostCrementExprContext ctx) {
        Token target = ctx.target;
        int opType = ctx.op.getType();
        return ExpressionHandles.call(data -> {
            LocalSlot.Variable variable = ExpressionHandles.getVariable(data, target);
            double value = variable.getValue();
            if (opType == INCREMENT) {
                value++;
            } else {
                value--;
            }
            variable.setValue(value);
            return value;
        });
    }

    @Override
    public MethodHandle visitPreCrementExpr(ExpressionParser.PreCrementExprContext ctx) {
        Token target = ctx.target;
        int opType = ctx.op.getType();
        return ExpressionHandles.call(data -> {
            LocalSlot.Variable variable = ExpressionHandles.getVariable(data, target);
            double value = variable.getValue();
            double result = value;
            if (opType == INCREMENT) {
                value++;
            } else {
                value--;
            }
            variable.setValue(value);
            return result;
        });
    }

    @Override
    public MethodHandle visitPlusMinusExpr(ExpressionParser.PlusMinusExprContext ctx) {
        MethodHandle value = evaluateForValue(ctx.expr);
        switch (ctx.op.getType()) {
            case PLUS:
                return value;
            case MINUS:
                return ExpressionHandles.call(data ->
                    -(double) ExpressionHandles.standardInvoke(value, data)
                );
        }
        throw ExpressionHelper.evalException(ctx, "Invalid text for plus/minus expr: " + ctx.op.getText());
    }

    @Override
    public MethodHandle visitNotExpr(ExpressionParser.NotExprContext ctx) {
        MethodHandle expr = evaluateBoolean(ctx.expr);
        return ExpressionHandles.call(data ->
            ExpressionHandles.boolToDouble(!(boolean) ExpressionHandles.standardInvoke(expr, data))
        );
    }

    @Override
    public MethodHandle visitComplementExpr(ExpressionParser.ComplementExprContext ctx) {
        MethodHandle expr = evaluateForValue(ctx.expr);
        // Looks weird. In order:
        // - Convert back to double from following long
        // - Convert to long from double value
        // - Convert from Object to Double to double.
        return ExpressionHandles.call(data ->
            (double) ~(long) (double) ExpressionHandles.standardInvoke(expr, data)
        );
    }

    @Override
    public MethodHandle visitConditionalAndExpr(ExpressionParser.ConditionalAndExprContext ctx) {
        MethodHandle left = evaluateBoolean(ctx.left);
        MethodHandle right = evaluateForValue(ctx.right);
        return MethodHandles.guardWithTest(
            left,
            right,
            ExpressionHandles.dropData(
                MethodHandles.constant(Double.class, ExpressionHandles.boolToDouble(false))
            )
        );
    }

    @Override
    public MethodHandle visitConditionalOrExpr(ExpressionParser.ConditionalOrExprContext ctx) {
        MethodHandle left = evaluateForValue(ctx.left);
        MethodHandle right = evaluateForValue(ctx.right);
        // Inject left as primary condition, on failure take right with data parameter
        // logic = (Double,ExecutionData)Double
        MethodHandle logic = MethodHandles.guardWithTest(
            // data arg dropped implicitly
            DOUBLE_TO_BOOL,
            // drop data arg
            MethodHandles.dropArguments(
                MethodHandles.identity(Double.class), 1, ExecutionData.class
            ),
            // drop left arg, call right
            MethodHandles.dropArguments(
                right, 0, Double.class
            )
        );
        // mixed = (ExecutionData,ExecutionData)Double
        MethodHandle mixed = MethodHandles.collectArguments(
            logic, 0, left
        );
        // Deduplicate ExecutionData
        return ExpressionHandles.dedupData(mixed);
    }

    private MethodHandle evaluateBinary(ParserRuleContext left,
                                        ParserRuleContext right,
                                        DoubleBinaryOperator op) {
        MethodHandle mhLeft = evaluateForValue(left);
        MethodHandle mhRight = evaluateForValue(right);
        // Map two data args to two double args, then evaluate op
        MethodHandle doubleData = MethodHandles.filterArguments(
            CALL_BINARY_OP.bindTo(op), 0,
            mhLeft.asType(mhLeft.type().unwrap()), mhRight.asType(mhRight.type().unwrap())
        );
        doubleData = doubleData.asType(doubleData.type().wrap());
        return ExpressionHandles.dedupData(doubleData);
    }

    private MethodHandle evaluateBinary(ParserRuleContext left,
                                        ParserRuleContext right,
                                        Supplier<DoubleBinaryOperator> op) {
        return evaluateBinary(left, right, op.get());
    }

    @Override
    public MethodHandle visitPowerExpr(ExpressionParser.PowerExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, Math::pow);
    }

    @Override
    public MethodHandle visitMultiplicativeExpr(ExpressionParser.MultiplicativeExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, () -> {
            switch (ctx.op.getType()) {
                case TIMES:
                    return (l, r) -> l * r;
                case DIVIDE:
                    return (l, r) -> l / r;
                case MODULO:
                    return (l, r) -> l % r;
            }
            throw ExpressionHelper.evalException(ctx, "Invalid text for multiplicative expr: " + ctx.op.getText());
        });
    }

    @Override
    public MethodHandle visitAddExpr(ExpressionParser.AddExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, () -> {
            switch (ctx.op.getType()) {
                case PLUS:
                    return Double::sum;
                case MINUS:
                    return (l, r) -> l - r;
            }
            throw ExpressionHelper.evalException(ctx, "Invalid text for additive expr: " + ctx.op.getText());
        });
    }

    @Override
    public MethodHandle visitShiftExpr(ExpressionParser.ShiftExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, () -> {
            switch (ctx.op.getType()) {
                case LEFT_SHIFT:
                    return (l, r) -> (double) ((long) l << (long) r);
                case RIGHT_SHIFT:
                    return (l, r) -> (double) ((long) l >> (long) r);
            }
            throw ExpressionHelper.evalException(ctx, "Invalid text for shift expr: " + ctx.op.getText());
        });
    }

    @Override
    public MethodHandle visitRelationalExpr(ExpressionParser.RelationalExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, () -> {
            switch (ctx.op.getType()) {
                case LESS_THAN:
                    return (l, r) -> ExpressionHandles.boolToDouble(l < r);
                case LESS_THAN_OR_EQUAL:
                    return (l, r) -> ExpressionHandles.boolToDouble(l <= r);
                case GREATER_THAN:
                    return (l, r) -> ExpressionHandles.boolToDouble(l > r);
                case GREATER_THAN_OR_EQUAL:
                    return (l, r) -> ExpressionHandles.boolToDouble(l >= r);
            }
            throw ExpressionHelper.evalException(ctx, "Invalid text for relational expr: " + ctx.op.getText());
        });
    }

    @Override
    public MethodHandle visitEqualityExpr(ExpressionParser.EqualityExprContext ctx) {
        return evaluateBinary(ctx.left, ctx.right, () -> {
            switch (ctx.op.getType()) {
                case EQUAL:
                    return (l, r) -> ExpressionHandles.boolToDouble(l == r);
                case NOT_EQUAL:
                    return (l, r) -> ExpressionHandles.boolToDouble(l != r);
                case NEAR:
                    return (l, r) -> ExpressionHandles.boolToDouble(almostEqual2sComplement(l, r, 450359963L));
                case GREATER_THAN_OR_EQUAL:
                    return (l, r) -> ExpressionHandles.boolToDouble(l >= r);
            }
            throw ExpressionHelper.evalException(ctx, "Invalid text for equality expr: " + ctx.op.getText());
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
    public MethodHandle visitPostfixExpr(ExpressionParser.PostfixExprContext ctx) {
        MethodHandle value = evaluateForValue(ctx.expr);
        if (ctx.op.getType() == EXCLAMATION_MARK) {
            return ExpressionHandles.call(data ->
                factorial((double) ExpressionHandles.standardInvoke(value, data))
            );
        }
        throw ExpressionHelper.evalException(ctx,
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
    public MethodHandle visitAssignment(ExpressionParser.AssignmentContext ctx) {
        int type = extractToken(ctx.assignmentOperator()).getType();
        Token target = ctx.target;
        MethodHandle getArg = evaluateForValue(ctx.expression());
        return ExpressionHandles.call(data -> {
            double value;
            double arg = (double) ExpressionHandles.standardInvoke(getArg, data);
            LocalSlot.Variable variable;
            if (type == ASSIGN) {
                variable = ExpressionHandles.initVariable(data, target);
                value = arg;
            } else {
                variable = ExpressionHandles.getVariable(data, target);
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
                        throw ExpressionHelper.evalException(ctx, "Invalid text for assign expr: " +
                            ctx.assignmentOperator().getText());
                }
            }
            variable.setValue(value);
            return value;
        });
    }

    @Override
    public MethodHandle visitFunctionCall(ExpressionParser.FunctionCallContext ctx) {
        MethodHandle handle = ExpressionHelper.resolveFunction(functions, ctx);
        String fnName = ctx.name.getText();
        MethodHandle[] arguments = new MethodHandle[ctx.args.size()];
        for (int i = 0; i < arguments.length; i++) {
            ExpressionParser.ExpressionContext arg = ctx.args.get(i);
            MethodHandle transformed = getArgument(fnName, handle.type(), i, arg);
            Class<?> ptype = handle.type().parameterType(i);
            Class<?> rtype = transformed.type().returnType();
            if (ptype != rtype && ptype.isAssignableFrom(rtype)) {
                // need to upcast
                transformed = transformed.asType(transformed.type().changeReturnType(ptype));
            }
            arguments[i] = transformed;
        }
        // Take each of our data accepting arguments, apply them over the source method
        MethodHandle manyData = MethodHandles.filterArguments(handle, 0, arguments);
        // Collapse every data into one argument
        int[] permutation = new int[arguments.length];
        return MethodHandles.permuteArguments(
            manyData, ExpressionHandles.COMPILED_EXPRESSION_SIG, permutation
        );
    }

    // MH: (ExecutionData)T; (depends on target)
    private MethodHandle getArgument(String fnName, MethodType type, int i, ParserRuleContext arg) {
        // Pass variable handle in for modification?
        String handleName = ExpressionHelper.getArgumentHandleName(fnName, type, i, arg);
        if (handleName == null) {
            return evaluateForValue(arg);
        }
        if (handleName.equals(WRAPPED_CONSTANT)) {
            // pass arg into new LocalSlot.Constant
            MethodHandle filter = evaluateForValue(arg);
            filter = filter.asType(filter.type().unwrap());
            return MethodHandles.collectArguments(
                NEW_LS_CONSTANT, 0, filter
            );
        }
        // small hack
        CommonToken fake = new CommonToken(arg.start);
        fake.setText(handleName);
        return ExpressionHandles.mhGetVariable(fake);
    }

    @Override
    public MethodHandle visitConstantExpression(ExpressionParser.ConstantExpressionContext ctx) {
        try {
            return ExpressionHandles.dropData(
                MethodHandles.constant(Double.class, Double.parseDouble(ctx.getText()))
            );
        } catch (NumberFormatException e) {
            // Rare, but might happen, e.g. if too many digits
            throw ExpressionHelper.evalException(ctx, "Invalid constant: " + e.getMessage());
        }
    }

    @Override
    public MethodHandle visitIdExpr(ExpressionParser.IdExprContext ctx) {
        Token source = ctx.source;
        return ExpressionHandles.call(data -> ExpressionHandles.getSlotValue(data, source));
    }

    /**
     * Method handle (ExecutionData)Double, returns null.
     */
    private static final MethodHandle DEFAULT_RESULT =
        ExpressionHandles.dropData(MethodHandles.constant(Double.class, null));

    @Override
    protected MethodHandle defaultResult() {
        return DEFAULT_RESULT;
    }

    @Override
    public MethodHandle visitChildren(RuleNode node) {
        MethodHandle result = defaultResult();
        int n = node.getChildCount();
        for (int i = 0; i < n; i++) {
            ParseTree c = node.getChild(i);
            if (c instanceof TerminalNode && ((TerminalNode) c).getSymbol().getType() == Token.EOF) {
                break;
            }

            MethodHandle childResult = c.accept(this);
            if (c instanceof ParserRuleContext) {
                checkHandle(childResult, (ParserRuleContext) c);
            }

            boolean returning = c instanceof ExpressionParser.ReturnStatementContext;
            result = aggregateResult(result, childResult, returning);
            if (returning) {
                return result;
            }
        }

        return result;
    }

    @Override
    protected MethodHandle aggregateResult(MethodHandle aggregate, MethodHandle nextResult) {
        throw new UnsupportedOperationException();
    }

    private MethodHandle aggregateResult(MethodHandle oldResult, MethodHandle result,
                                         boolean keepDefault) {
        // Execute `oldResult` but ignore its return value, then execute result and return that.
        // If `oldResult` (the old value) is `defaultResult`, it's bogus, so just skip it
        if (oldResult == DEFAULT_RESULT) {
            return result;
        }
        if (result == DEFAULT_RESULT && !keepDefault) {
            return oldResult;
        }
        // Add a dummy Double parameter to the end
        MethodHandle dummyDouble = MethodHandles.dropArguments(
            result, 1, Double.class
        );
        // Have oldResult turn it from data->Double
        MethodHandle doubledData = MethodHandles.collectArguments(
            dummyDouble, 1, oldResult
        );
        // Deduplicate the `data` parameter
        return ExpressionHandles.dedupData(doubledData);
    }
}
