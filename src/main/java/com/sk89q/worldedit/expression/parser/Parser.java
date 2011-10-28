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
            return ParserProcessors.processStatement(halfProcessed);
        }
        else {
            return ParserProcessors.processExpression(halfProcessed);
        }
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
