// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.expression.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.expression.Identifiable;
import com.sk89q.worldedit.expression.lexer.tokens.CharacterToken;
import com.sk89q.worldedit.expression.lexer.tokens.IdentifierToken;
import com.sk89q.worldedit.expression.lexer.tokens.NumberToken;
import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import com.sk89q.worldedit.expression.runtime.Constant;
import com.sk89q.worldedit.expression.runtime.Functions;
import com.sk89q.worldedit.expression.runtime.Invokable;
import com.sk89q.worldedit.expression.runtime.Operators;
import com.sk89q.worldedit.expression.runtime.Sequence;

public class Parser {
    private final class NullToken extends Token {
        private NullToken(int position) {
            super(position);
        }

        public char id() {
            return '\0';
        }

        public String toString() {
            return "NullToken";
        }
    }

    private final List<Token> tokens;
    private int position = 0;
    private Map<String, Invokable> variables;

    private Parser(List<Token> tokens, Map<String, Invokable> variables) {
        this.tokens = tokens;
        this.variables = variables;
    }

    public static final Invokable parse(List<Token> tokens, Map<String, Invokable> variables) throws ParserException {
        return new Parser(tokens, variables).parse();
    }

    private Invokable parse() throws ParserException {
        final Invokable ret = parseInternal(true);
        if (position < tokens.size()) {
            final Token token = peek();
            throw new ParserException(token.getPosition(), "Extra token at the end of the input: " + token);
        }
        return ret;
    }

    private final Invokable parseInternal(boolean isStatement) throws ParserException {
        LinkedList<Identifiable> halfProcessed = new LinkedList<Identifiable>();

        // process brackets, numbers, functions, variables and detect prefix operators
        boolean expressionStart = true;
        loop: while (position < tokens.size()) {
            final Token current = peek();

            switch (current.id()) {
            case '0':
                halfProcessed.add(new Constant(current.getPosition(), ((NumberToken) current).value));
                ++position;
                expressionStart = false;
                break;

            case 'i':
                final IdentifierToken identifierToken = (IdentifierToken) current;
                ++position;

                final Token next = peek();
                if (next.id() == '(') {
                    halfProcessed.add(parseFunctionCall(identifierToken));
                }
                else {
                    Invokable variable = variables.get(identifierToken.value);
                    if (variable == null) {
                        throw new ParserException(current.getPosition(), "Variable '" + identifierToken.value + "' not found");
                    }
                    halfProcessed.add(variable);
                }
                expressionStart = false;
                break;

            case '(':
                halfProcessed.add(parseBracket());
                expressionStart = false;
                break;

            case '{':
                halfProcessed.add(parseBlock());
                halfProcessed.add(new CharacterToken(-1, ';'));
                expressionStart = false;
                break;

            case ',':
            case ')':
            case '}':
                break loop;

            case 'o':
                if (expressionStart) {
                    halfProcessed.add(new PrefixOperator((OperatorToken) current));
                }
                else {
                    halfProcessed.add(current);
                }
                ++position;
                expressionStart = true;
                break;

            default:
                halfProcessed.add(current);
                ++position;
                expressionStart = false;
                break;
            }
        }

        if (isStatement) {
            return processStatement(halfProcessed);
        }
        else {
            // process binary operators
            return processExpression(halfProcessed);
        }
    }

    private static Invokable processStatement(LinkedList<Identifiable> input) throws ParserException {
        LinkedList<Identifiable> lhs = new LinkedList<Identifiable>();
        LinkedList<Identifiable> rhs = new LinkedList<Identifiable>();
        boolean semicolonFound = false;

        for (Identifiable identifiable : input) {
            if (semicolonFound) {
                rhs.addLast(identifiable);
            }
            else {
                if (identifiable.id() == ';') {
                    semicolonFound = true;
                }
                else {
                    lhs.addLast(identifiable);
                }
            }
        }

        if (rhs.isEmpty()) {
            if (lhs.isEmpty()) {
                return new Sequence(semicolonFound ? input.get(0).getPosition() : -1);
            }

            return processExpression(lhs);
        }
        else if (lhs.isEmpty()) {
            return processStatement(rhs);
        }
        else {
            assert(semicolonFound);

            Invokable lhsInvokable = processExpression(lhs);
            Invokable rhsInvokable = processStatement(rhs);

            return new Sequence(lhsInvokable.getPosition(), lhsInvokable, rhsInvokable);
        }
    }

    private static Invokable processExpression(LinkedList<Identifiable> input) throws ParserException {
        return processBinaryOpsRA(input, binaryOpMapsRA.length - 1);
    }

    private static final Map<String, String>[] binaryOpMapsLA;
    private static final Map<String, String>[] binaryOpMapsRA;

    private static final Map<String, String> unaryOpMap = new HashMap<String, String>();
    static {
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
                Map<String, String> m = lBinaryOpMapsLA[i] = new HashMap<String, String>();
                for (int j = 0; j < a.length; ++j) {
                    final Object[] element = a[j];
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
                Map<String, String> m = lBinaryOpMapsRA[i] = new HashMap<String, String>();
                for (int j = 0; j < a.length; ++j) {
                    final Object[] element = a[j];
                    m.put((String) element[0], (String) element[1]);
                }
            }
        }

        unaryOpMap.put("-", "neg");
        unaryOpMap.put("!", "not");
        unaryOpMap.put("~", "inv");
        unaryOpMap.put("++", "inc");
        unaryOpMap.put("--", "dec");
    }

    private static Invokable processBinaryOpsLA(LinkedList<Identifiable> input, int level) throws ParserException {
        if (level < 0) {
            return processUnaryOps(input);
        }

        LinkedList<Identifiable> lhs = new LinkedList<Identifiable>();
        LinkedList<Identifiable> rhs = new LinkedList<Identifiable>();
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
            }
            else {
                lhs.addFirst(identifiable);
            }
        }

        Invokable rhsInvokable = processBinaryOpsLA(rhs, level - 1);
        if (operator == null) return rhsInvokable;

        Invokable lhsInvokable = processBinaryOpsLA(lhs, level);

        try {
            return Operators.getOperator(input.get(0).getPosition(), operator, lhsInvokable, rhsInvokable);
        }
        catch (NoSuchMethodException e) {
            final Token operatorToken = (Token) input.get(lhs.size());
            throw new ParserException(operatorToken.getPosition(), "Couldn't find operator '" + operator + "'");
        }
    }

    private static Invokable processBinaryOpsRA(LinkedList<Identifiable> input, int level) throws ParserException {
        if (level < 0) {
            return processTernaryOps(input);
        }

        LinkedList<Identifiable> lhs = new LinkedList<Identifiable>();
        LinkedList<Identifiable> rhs = new LinkedList<Identifiable>();
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
            }
            else {
                rhs.addLast(identifiable);
            }
        }

        Invokable lhsInvokable = processBinaryOpsRA(lhs, level - 1);
        if (operator == null) return lhsInvokable;

        Invokable rhsInvokable = processBinaryOpsRA(rhs, level);

        try {
            return Operators.getOperator(input.get(0).getPosition(), operator, lhsInvokable, rhsInvokable);
        }
        catch (NoSuchMethodException e) {
            final Token operatorToken = (Token) input.get(lhs.size());
            throw new ParserException(operatorToken.getPosition(), "Couldn't find operator '" + operator + "'");
        }
    }

    private static Invokable processTernaryOps(LinkedList<Identifiable> input) throws ParserException {
        return processBinaryOpsLA(input, binaryOpMapsLA.length - 1);
    }

    private static Invokable processUnaryOps(LinkedList<Identifiable> input) throws ParserException {
        if (input.isEmpty()) {
            throw new ParserException(-1, "Expression missing.");
        }

        Invokable ret = (Invokable) input.removeLast();
        while (!input.isEmpty()) {
            final Identifiable last = input.removeLast();
            final int lastPosition = last.getPosition();
            if (last instanceof PrefixOperator) {
                final String operator = ((PrefixOperator) last).operator;
                if (operator.equals("+")) {
                    continue;
                }

                String opName = unaryOpMap.get(operator);
                if (opName != null) {
                    try {
                        ret = Operators.getOperator(lastPosition, opName, ret);
                        continue;
                    }
                    catch (NoSuchMethodException e) {
                        throw new ParserException(lastPosition, "No such prefix operator: " + operator);
                    }
                }
            }
            if (last instanceof Token) {
                throw new ParserException(lastPosition, "Extra token found in expression: " + last);
            }
            else if (last instanceof Invokable) {
                throw new ParserException(lastPosition, "Extra expression found: " + last);
            }
            else {
                throw new ParserException(lastPosition, "Extra element found: " + last);
            }
        }
        return ret;
    }

    private Token peek() {
        if (position >= tokens.size()) {
            return new NullToken(tokens.get(tokens.size() - 1).getPosition() + 1);
        }

        return tokens.get(position);
    }

    private Identifiable parseFunctionCall(IdentifierToken identifierToken) throws ParserException {
        if (peek().id() != '(') {
            throw new ParserException(peek().getPosition(), "Unexpected character in parseFunctionCall");
        }
        ++position;

        try {
            if (peek().id() == ')') {
                return Functions.getFunction(identifierToken.getPosition(), identifierToken.value);
            }

            List<Invokable> args = new ArrayList<Invokable>();

            loop: while (true) {
                args.add(parseInternal(false));

                final Token current = peek();
                ++position;

                switch (current.id()) {
                case ',':
                    continue;

                case ')':
                    break loop;

                default:
                    throw new ParserException(current.getPosition(), "Unmatched opening bracket");
                }
            }

            return Functions.getFunction(identifierToken.getPosition(), identifierToken.value, args.toArray(new Invokable[args.size()]));
        }
        catch (NoSuchMethodException e) {
            throw new ParserException(identifierToken.getPosition(), "Function not found", e);
        }
    }

    private final Invokable parseBracket() throws ParserException {
        if (peek().id() != '(') {
            throw new ParserException(peek().getPosition(), "Unexpected character in parseBracket");
        }
        ++position;

        final Invokable ret = parseInternal(false);

        if (peek().id() != ')') {
            throw new ParserException(peek().getPosition(), "Unmatched opening bracket");
        }
        ++position;

        return ret;
    }

    private final Invokable parseBlock() throws ParserException {
        if (peek().id() != '{') {
            throw new ParserException(peek().getPosition(), "Unexpected character in parseBlock");
        }
        ++position;

        final Invokable ret = parseInternal(true);

        if (peek().id() != '}') {
            throw new ParserException(peek().getPosition(), "Unmatched opening brace");
        }
        ++position;

        return ret;
    }
}
