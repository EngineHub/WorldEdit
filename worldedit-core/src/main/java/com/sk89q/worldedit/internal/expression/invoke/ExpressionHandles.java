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

import com.google.common.base.Throwables;
import com.sk89q.worldedit.internal.expression.CompiledExpression;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.ExecutionData;
import com.sk89q.worldedit.internal.expression.ExpressionHelper;
import com.sk89q.worldedit.internal.expression.LocalSlot;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.internal.expression.ExpressionHelper.check;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.checkIterations;
import static com.sk89q.worldedit.internal.expression.ExpressionHelper.getErrorPosition;
import static java.lang.invoke.MethodHandles.collectArguments;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodHandles.throwException;
import static java.lang.invoke.MethodType.methodType;

class ExpressionHandles {

    static final MethodType COMPILED_EXPRESSION_SIG = methodType(Double.class, ExecutionData.class);

    private static final MethodHandle EVAL_EXCEPTION_CONSTR;
    private static final MethodHandle CALL_EXPRESSION;
    private static final MethodHandle GET_VARIABLE;
    private static final MethodHandle WHILE_FOR_LOOP_IMPL;
    private static final MethodHandle DO_WHILE_LOOP_IMPL;
    private static final MethodHandle SIMPLE_FOR_LOOP_IMPL;
    private static final MethodHandle SWITCH_IMPL;

    // (Object)boolean;
    static final MethodHandle IS_NULL;
    // (Double)boolean;
    static final MethodHandle DOUBLE_TO_BOOL;
    // (double, double)Double;
    static final MethodHandle CALL_BINARY_OP;
    static final MethodHandle NEW_LS_CONSTANT;
    // (Double)ReturnException;
    static final MethodHandle NEW_RETURN_EXCEPTION;
    // (ReturnException)Double;
    static final MethodHandle RETURN_EXCEPTION_GET_RESULT;

    static final MethodHandle NULL_DOUBLE = dropData(constant(Double.class, null));

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            EVAL_EXCEPTION_CONSTR = lookup.findConstructor(
                EvaluationException.class, methodType(void.class, int.class, String.class));
            CALL_EXPRESSION = lookup.findVirtual(
                CompiledExpression.class, "execute",
                methodType(Double.class, ExecutionData.class));
            GET_VARIABLE = lookup.findStatic(ExpressionHandles.class, "getVariable",
                methodType(LocalSlot.Variable.class, ExecutionData.class, Token.class));
            WHILE_FOR_LOOP_IMPL = lookup.findStatic(ExpressionHandles.class,
                "whileForLoopImpl",
                methodType(Double.class, ExecutionData.class, MethodHandle.class,
                    MethodHandle.class, ExecNode.class, MethodHandle.class));
            DO_WHILE_LOOP_IMPL = lookup.findStatic(ExpressionHandles.class, "doWhileLoopImpl",
                methodType(Double.class, ExecutionData.class, MethodHandle.class, ExecNode.class));
            SIMPLE_FOR_LOOP_IMPL = lookup.findStatic(ExpressionHandles.class, "simpleForLoopImpl",
                methodType(Double.class, ExecutionData.class, MethodHandle.class,
                    MethodHandle.class, Token.class, ExecNode.class));
            SWITCH_IMPL = lookup.findStatic(ExpressionHandles.class, "switchImpl",
                methodType(Double.class, ExecutionData.class, Double2ObjectMap.class,
                    MethodHandle.class, ExecNode.class));

            IS_NULL = lookup.findStatic(Objects.class, "isNull",
                methodType(boolean.class, Object.class));
            DOUBLE_TO_BOOL = boxDoubles(lookup.findStatic(ExpressionHandles.class, "doubleToBool",
                methodType(boolean.class, double.class)));
            CALL_BINARY_OP = lookup.findVirtual(DoubleBinaryOperator.class, "applyAsDouble",
                methodType(double.class, double.class, double.class))
                .asType(methodType(Double.class, DoubleBinaryOperator.class, double.class, double.class));
            NEW_LS_CONSTANT = lookup.findConstructor(LocalSlot.Constant.class,
                methodType(void.class, double.class));
            NEW_RETURN_EXCEPTION = lookup.findConstructor(ReturnException.class,
                methodType(void.class, Double.class));
            RETURN_EXCEPTION_GET_RESULT = lookup.findVirtual(ReturnException.class,
                "getResult", methodType(Double.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    static MethodHandle boxDoubles(MethodHandle handle) {
        MethodType type = handle.type();
        type = methodType(
            boxIfPrimitiveDouble(type.returnType()),
            type.parameterList().stream().map(ExpressionHandles::boxIfPrimitiveDouble)
                .collect(Collectors.toList())
        );
        return handle.asType(type);
    }

    private static Class<?> boxIfPrimitiveDouble(Class<?> clazz) {
        return clazz == double.class ? Double.class : clazz;
    }

    static MethodHandle unboxDoubles(MethodHandle handle) {
        MethodType type = handle.type();
        type = methodType(
            unboxIfDouble(type.returnType()),
            type.parameterList().stream().map(ExpressionHandles::unboxIfDouble)
                .collect(Collectors.toList())
        );
        return handle.asType(type);
    }

    private static Class<?> unboxIfDouble(Class<?> clazz) {
        return clazz == Double.class ? double.class : clazz;
    }

    @FunctionalInterface
    interface Invokable {
        Object invoke(MethodHandle handle) throws Throwable;
    }

    static Object safeInvoke(MethodHandle handle, Invokable invokable) {
        try {
            return invokable.invoke(handle);
        } catch (Throwable t) {
            Throwables.throwIfUnchecked(t);
            throw new RuntimeException(t);
        }
    }

    static Object standardInvoke(MethodHandle handle, ExecutionData data) {
        return safeInvoke(handle, h -> h.invoke(data));
    }

    static Object constantInvoke(MethodHandle handle) {
        return standardInvoke(handle, ExecutionData.CONSTANT_EVALUATOR);
    }

    static MethodHandle dropData(MethodHandle handle) {
        return dropArguments(handle, 0, ExecutionData.class);
    }

    static MethodHandle dedupData(MethodHandle doubleData) {
        return permuteArguments(
            doubleData, COMPILED_EXPRESSION_SIG,
            0, 0
        );
    }

    static LocalSlot.Variable initVariable(ExecutionData data, Token nameToken) {
        String name = nameToken.getText();
        return data.getSlots().initVariable(name)
            .orElseThrow(() -> ExpressionHelper.evalException(
                nameToken, "Cannot overwrite non-variable '" + name + "'"
            ));
    }

    private static Supplier<EvaluationException> varNotInitException(Token nameToken) {
        return () -> ExpressionHelper.evalException(
            nameToken, "'" + nameToken.getText() + "' is not initialized yet"
        );
    }

    static MethodHandle mhGetVariable(Token nameToken) {
        return insertArguments(GET_VARIABLE, 1, nameToken);
    }

    static LocalSlot.Variable getVariable(ExecutionData data, Token nameToken) {
        String name = nameToken.getText();
        LocalSlot slot = data.getSlots().getSlot(name)
            .orElseThrow(varNotInitException(nameToken));
        if (!(slot instanceof LocalSlot.Variable)) {
            throw ExpressionHelper.evalException(
                nameToken, "'" + name + "' is not a variable"
            );
        }
        return (LocalSlot.Variable) slot;
    }

    static double getSlotValue(ExecutionData data, Token nameToken) {
        String name = nameToken.getText();
        return data.getSlots().getSlotValue(name)
            .orElseThrow(varNotInitException(nameToken));
    }

    /**
     * Returns a method handle that calls
     * {@link EvaluationException#EvaluationException(int, String)} with the supplied arguments.
     */
    private static MethodHandle evalException(ParserRuleContext ctx, String message) {
        return insertArguments(EVAL_EXCEPTION_CONSTR, 0,
            getErrorPosition(ctx.start), message);
    }

    /**
     * Returns a method handle that takes no arguments, and throws the result of
     * {@link #evalException(ParserRuleContext, String)}. It will additionally return Double.
     */
    static MethodHandle throwEvalException(ParserRuleContext ctx, String message) {
        // replace arg0 of `throw` with `evalException`
        return collectArguments(
            throwException(Double.class, EvaluationException.class),
            0,
            evalException(ctx, message)
        );
    }

    private static boolean doubleToBool(double bool) {
        return bool != 0;
    }

    static double boolToDouble(boolean bool) {
        return bool ? 1 : 0;
    }

    /**
     * Encapsulate the given code into a MethodHandle.
     */
    static MethodHandle call(CompiledExpression runnable) {
        return CALL_EXPRESSION.bindTo(runnable).asType(COMPILED_EXPRESSION_SIG);
    }

    static MethodHandle whileLoop(MethodHandle condition, ExecNode body) {
        return insertArguments(WHILE_FOR_LOOP_IMPL, 1,
            null, condition, body, null);
    }

    static MethodHandle forLoop(MethodHandle init,
                                MethodHandle condition,
                                ExecNode body,
                                MethodHandle update) {
        return insertArguments(WHILE_FOR_LOOP_IMPL, 1,
            init, condition, body, update);
    }

    private static Double whileForLoopImpl(ExecutionData data,
                                           @Nullable MethodHandle init,
                                           MethodHandle condition,
                                           ExecNode body,
                                           @Nullable MethodHandle update) {
        Double result = null;
        int iterations = 0;
        if (init != null) {
            standardInvoke(init, data);
        }
        while ((boolean) standardInvoke(condition, data)) {
            checkIterations(iterations, body.ctx);
            data.checkDeadline();
            iterations++;
            try {
                result = (Double) standardInvoke(body.handle, data);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
            if (update != null) {
                standardInvoke(update, data);
            }
        }
        return result;
    }

    static MethodHandle doWhileLoop(MethodHandle condition, ExecNode body) {
        return insertArguments(DO_WHILE_LOOP_IMPL, 1, condition, body);
    }

    private static Double doWhileLoopImpl(ExecutionData data,
                                          MethodHandle condition,
                                          ExecNode body) {
        Double result = null;
        int iterations = 0;
        do {
            checkIterations(iterations, body.ctx);
            data.checkDeadline();
            iterations++;
            try {
                result = (Double) standardInvoke(body.handle, data);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
        } while ((boolean) standardInvoke(condition, data));
        return result;
    }

    static MethodHandle simpleForLoop(MethodHandle first,
                                      MethodHandle last,
                                      Token counter,
                                      ExecNode body) {
        return insertArguments(SIMPLE_FOR_LOOP_IMPL, 1,
            first, last, counter, body);
    }

    private static Double simpleForLoopImpl(ExecutionData data,
                                            MethodHandle getFirst,
                                            MethodHandle getLast,
                                            Token counterToken,
                                            ExecNode body) {
        Double result = null;
        int iterations = 0;
        double first = (double) standardInvoke(getFirst, data);
        double last = (double) standardInvoke(getLast, data);
        LocalSlot.Variable variable = initVariable(data, counterToken);
        for (double i = first; i <= last; i++) {
            checkIterations(iterations, body.ctx);
            data.checkDeadline();
            iterations++;
            variable.setValue(i);
            try {
                result = (Double) standardInvoke(body.handle, data);
            } catch (BreakException ex) {
                if (!ex.doContinue) {
                    break;
                }
            }
        }
        return result;
    }

    static MethodHandle switchStatement(Double2ObjectMap<ExecNode> cases,
                                        MethodHandle getValue,
                                        @Nullable ExecNode defaultCase) {
        return insertArguments(SWITCH_IMPL, 1, cases, getValue, defaultCase);
    }

    private static Double switchImpl(ExecutionData data,
                                     Double2ObjectMap<ExecNode> cases,
                                     MethodHandle getValue,
                                     @Nullable ExecNode defaultCase) {
        double value = (double) standardInvoke(getValue, data);
        boolean matched = false;
        Double evaluated = null;
        boolean falling = false;
        for (Double2ObjectMap.Entry<ExecNode> entry : Double2ObjectMaps.fastIterable(cases)) {
            if (falling || entry.getDoubleKey() == value) {
                matched = true;
                try {
                    evaluated = (Double) standardInvoke(entry.getValue().handle, data);
                    falling = true;
                } catch (BreakException brk) {
                    check(!brk.doContinue, entry.getValue().ctx, "Cannot continue in a switch");
                    falling = false;
                    break;
                }
            }
        }
        // This if is like the one in the loop, default's "case" is `!matched` & present
        if ((falling || !matched) && defaultCase != null) {
            try {
                evaluated = (Double) standardInvoke(defaultCase.handle, data);
            } catch (BreakException brk) {
                check(!brk.doContinue, defaultCase.ctx, "Cannot continue in a switch");
            }
        }
        return evaluated;
    }

    private ExpressionHandles() {
    }

}
