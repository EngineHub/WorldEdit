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

package com.sk89q.worldedit.internal.expression.parser;

import com.sk89q.worldedit.internal.expression.Identifiable;
import com.sk89q.worldedit.internal.expression.lexer.tokens.OperatorToken;
import com.sk89q.worldedit.internal.expression.lexer.tokens.Token;
import com.sk89q.worldedit.internal.expression.runtime.Conditional;
import com.sk89q.worldedit.internal.expression.runtime.Operators;
import com.sk89q.worldedit.internal.expression.runtime.RValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Helper classfor Parser. Contains processors for statements and operators.
 */
public final class ParserProcessors {

    private static final Map<String, String> unaryOpMap = new HashMap<>();

    private static final Map<String, String>[] binaryOpMapsLA;
    private static final Map<String, String>[] binaryOpMapsRA;

    static {
        unaryOpMap.put("-", "neg");
        unaryOpMap.put("!", "not");
        unaryOpMap.put("~", "inv");
        unaryOpMap.put("++", "inc");
        unaryOpMap.put("--", "dec");
        unaryOpMap.put("x++", "postinc");
        unaryOpMap.put("x--", "postdec");
        unaryOpMap.put("x!", "fac");

        final Object[][][] binaryOpsLA = {
                {
                        { "^", "pow" },
                        { "**", "pow" },
                },
                {
                        { "*", "mul" },
                        { "/", "div" },
                        { "%", "mod" },
                },
                {
                        { "+", "add" },
                        { "-", "sub" },
                },
                {
                        { "<<", "shl" },
                        { ">>", "shr" },
                },
                {
                        { "<", "lth" },
                        { ">", "gth" },
                        { "<=", "leq" },
                        { ">=", "geq" },
                },
                {
                        { "==", "equ" },
                        { "!=", "neq" },
                        { "~=", "near" },
                },
                {
                    { "&&", "and" },
                },
                {
                    { "||", "or" },
                },
        };
        final Object[][][] binaryOpsRA = {
                {
                        { "=", "ass" },
                        { "+=", "aadd" },
                        { "-=", "asub" },
                        { "*=", "amul" },
                        { "/=", "adiv" },
                        { "%=", "amod" },
                        { "^=", "aexp" },
                },
        };

        @SuppressWarnings("unchecked")
        final Map<String, String>[] lBinaryOpMapsLA = binaryOpMapsLA = new Map[binaryOpsLA.length];
        for (int i = 0; i < binaryOpsLA.length; ++i) {
            final Object[][] a = binaryOpsLA[i];
            switch (a.length) {
            case 0:
                lBinaryOpMapsLA[i] = Collections.emptyMap();
                break;

            case 1:
                final Object[] first = a[0];
                lBinaryOpMapsLA[i] = Collections.singletonMap((String) first[0], (String) first[1]);
                break;

            default:
                Map<String, String> m = lBinaryOpMapsLA[i] = new HashMap<>();
                for (final Object[] element : a) {
                    m.put((String) element[0], (String) element[1]);
                }
            }
        }

        @SuppressWarnings("unchecked")
        final Map<String, String>[] lBinaryOpMapsRA = binaryOpMapsRA = new Map[binaryOpsRA.length];
        for (int i = 0; i < binaryOpsRA.length; ++i) {
            final Object[][] a = binaryOpsRA[i];
            switch (a.length) {
            case 0:
                lBinaryOpMapsRA[i] = Collections.emptyMap();
                break;

            case 1:
                final Object[] first = a[0];
                lBinaryOpMapsRA[i] = Collections.singletonMap((String) first[0], (String) first[1]);
                break;

            default:
                Map<String, String> m = lBinaryOpMapsRA[i] = new HashMap<>();
                for (final Object[] element : a) {
                    m.put((String) element[0], (String) element[1]);
                }
            }
        }
    }

    private ParserProcessors() {
    }

    static RValue processExpression(LinkedList<Identifiable> input) throws ParserException {
        return processBinaryOpsRA(input, binaryOpMapsRA.length - 1);
    }

    private static RValue processBinaryOpsLA(LinkedList<Identifiable> input, int level) throws ParserException {
        if (level < 0) {
            return processUnaryOps(input);
        }

        LinkedList<Identifiable> lhs = new LinkedList<>();
        LinkedList<Identifiable> rhs = new LinkedList<>();
        String operator = null;

        for (Iterator<Identifiable> it = input.descendingIterator(); it.hasNext();) {
            Identifiable identifiable = it.next();
            if (operator == null) {
                rhs.addFirst(identifiable);

                if (!(identifiable instanceof OperatorToken)) {
                    continue;
                }

                operator = binaryOpMapsLA[level].get(((OperatorToken) identifiable).operator);
                if (operator == null) {
                    continue;
                }

                rhs.removeFirst();
            } else {
                lhs.addFirst(identifiable);
            }
        }

        RValue rhsInvokable = processBinaryOpsLA(rhs, level - 1);
        if (operator == null) return rhsInvokable;

        RValue lhsInvokable = processBinaryOpsLA(lhs, level);

        try {
            return Operators.getOperator(input.get(0).getPosition(), operator, lhsInvokable, rhsInvokable);
        } catch (NoSuchMethodException e) {
            final Token operatorToken = (Token) input.get(lhs.size());
            throw new ParserException(operatorToken.getPosition(), "Couldn't find operator '" + operator + "'");
        }
    }

    private static RValue processBinaryOpsRA(LinkedList<Identifiable> input, int level) throws ParserException {
        if (level < 0) {
            return processTernaryOps(input);
        }

        LinkedList<Identifiable> lhs = new LinkedList<>();
        LinkedList<Identifiable> rhs = new LinkedList<>();
        String operator = null;

        for (Identifiable identifiable : input) {
            if (operator == null) {
                lhs.addLast(identifiable);

                if (!(identifiable instanceof OperatorToken)) {
                    continue;
                }

                operator = binaryOpMapsRA[level].get(((OperatorToken) identifiable).operator);
                if (operator == null) {
                    continue;
                }

                lhs.removeLast();
            } else {
                rhs.addLast(identifiable);
            }
        }

        RValue lhsInvokable = processBinaryOpsRA(lhs, level - 1);
        if (operator == null) return lhsInvokable;

        RValue rhsInvokable = processBinaryOpsRA(rhs, level);

        try {
            return Operators.getOperator(input.get(0).getPosition(), operator, lhsInvokable, rhsInvokable);
        } catch (NoSuchMethodException e) {
            final Token operatorToken = (Token) input.get(lhs.size());
            throw new ParserException(operatorToken.getPosition(), "Couldn't find operator '" + operator + "'");
        }
    }

    private static RValue processTernaryOps(LinkedList<Identifiable> input) throws ParserException {
        LinkedList<Identifiable> lhs = new LinkedList<>();
        LinkedList<Identifiable> mhs = new LinkedList<>();
        LinkedList<Identifiable> rhs = new LinkedList<>();

        int partsFound = 0;
        int conditionalsFound = 0;

        for (Identifiable identifiable : input) {
            final char character = identifiable.id();
            switch (character) {
            case '?':
                ++conditionalsFound;
                break;
            case ':':
                --conditionalsFound;
                break;
            }

            if (conditionalsFound < 0) {
                throw new ParserException(identifiable.getPosition(), "Unexpected ':'");
            }

            switch (partsFound) {
            case 0:
                if (character == '?') {
                    partsFound = 1;
                } else {
                    lhs.addLast(identifiable);
                }
                break;

            case 1:
                if (conditionalsFound == 0 && character == ':') {
                    partsFound = 2;
                } else {
                    mhs.addLast(identifiable);
                }
                break;

            case 2:
                rhs.addLast(identifiable);
            }
        }

        if (partsFound < 2) {
            return processBinaryOpsLA(input, binaryOpMapsLA.length - 1);
        }

        RValue lhsInvokable = processBinaryOpsLA(lhs, binaryOpMapsLA.length - 1);
        RValue mhsInvokable = processTernaryOps(mhs);
        RValue rhsInvokable = processTernaryOps(rhs);

        return new Conditional(input.get(lhs.size()).getPosition(), lhsInvokable, mhsInvokable, rhsInvokable);
    }

    private static RValue processUnaryOps(LinkedList<Identifiable> input) throws ParserException {
        // Preprocess postfix operators into unary operators
        final Identifiable center;
        LinkedList<UnaryOperator> postfixes = new LinkedList<>();
        do {
            if (input.isEmpty()) {
                throw new ParserException(-1, "Expression missing.");
            }

            final Identifiable last = input.removeLast();
            if (last instanceof OperatorToken) {
                postfixes.addLast(new UnaryOperator(last.getPosition(), "x" + ((OperatorToken) last).operator));
            } else if (last instanceof UnaryOperator) {
                postfixes.addLast(new UnaryOperator(last.getPosition(), "x" + ((UnaryOperator) last).operator));
            } else {
                center = last;
                break;
            }
        } while (true);

        if (!(center instanceof RValue)) {
            throw new ParserException(center.getPosition(), "Expected expression, found " + center);
        }

        input.addAll(postfixes);

        RValue ret = (RValue) center;
        while (!input.isEmpty()) {
            final Identifiable last = input.removeLast();
            final int lastPosition = last.getPosition();
            if (last instanceof UnaryOperator) {
                final String operator = ((UnaryOperator) last).operator;
                if (operator.equals("+")) {
                    continue;
                }

                String opName = unaryOpMap.get(operator);
                if (opName != null) {
                    try {
                        ret = Operators.getOperator(lastPosition, opName, ret);
                        continue;
                    } catch (NoSuchMethodException e) {
                        throw new ParserException(lastPosition, "No such prefix operator: " + operator);
                    }
                }
            }

            if (last instanceof Token) {
                throw new ParserException(lastPosition, "Extra token found in expression: " + last);
            } else if (last instanceof RValue) {
                throw new ParserException(lastPosition, "Extra expression found: " + last);
            } else {
                throw new ParserException(lastPosition, "Extra element found: " + last);
            }
        }
        return ret;
    }

}
