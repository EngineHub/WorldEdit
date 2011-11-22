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
import com.sk89q.worldedit.expression.lexer.tokens.IdentifierToken;
import com.sk89q.worldedit.expression.lexer.tokens.KeywordToken;
import com.sk89q.worldedit.expression.lexer.tokens.NumberToken;
import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import com.sk89q.worldedit.expression.runtime.Conditional;
import com.sk89q.worldedit.expression.runtime.Constant;
import com.sk89q.worldedit.expression.runtime.For;
import com.sk89q.worldedit.expression.runtime.Functions;
import com.sk89q.worldedit.expression.runtime.RValue;
import com.sk89q.worldedit.expression.runtime.Sequence;
import com.sk89q.worldedit.expression.runtime.Variable;
import com.sk89q.worldedit.expression.runtime.While;

/**
 * Processes a list of tokens into an executable tree.
 *
 * Tokens can be numbers, identifiers, operators and assorted other characters.
 *
 * @author TomyLobo
 */
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
    private Map<String, RValue> variables;

    private Parser(List<Token> tokens, Map<String, RValue> variables) {
        this.tokens = tokens;
        this.variables = variables;
    }

    public static final RValue parse(List<Token> tokens, Map<String, RValue> variables) throws ParserException {
        return new Parser(tokens, variables).parse();
    }

    private RValue parse() throws ParserException {
        final RValue ret = parseStatements(false);
        if (position < tokens.size()) {
            final Token token = peek();
            throw new ParserException(token.getPosition(), "Extra token at the end of the input: " + token);
        }
        return ret;
    }

    private RValue parseStatements(boolean singleStatement) throws ParserException {
        List<RValue> statements = new ArrayList<RValue>();
        loop: while (true) {
            if (position >= tokens.size()) {
                break;
            }

            final Token current = peek();
            switch (current.id()) {
            case ';':
                ++position;

                if (singleStatement) {
                    break loop;
                }
                break;

            case '{':
                statements.add(parseBlock());

                if (singleStatement) {
                    break loop;
                }
                break;

            case '}':
                break loop;

            case 'k':
                final String keyword = ((KeywordToken) current).value;
                switch (keyword.charAt(0)) {
                case 'i': { // if
                    ++position;
                    final RValue condition = parseBracket();
                    final RValue truePart = parseStatements(true);
                    final RValue falsePart;

                    if (hasKeyword("else")) {
                        ++position;
                        falsePart = parseStatements(true);
                    } else {
                        falsePart = null;
                    }

                    statements.add(new Conditional(current.getPosition(), condition, truePart, falsePart));
                    break;
                }

                case 'w': { // while
                    ++position;
                    final RValue condition = parseBracket();
                    final RValue body = parseStatements(true);

                    statements.add(new While(current.getPosition(), condition, body, false));
                    break;
                }

                case 'd': { // do
                    ++position;
                    final RValue body = parseStatements(true);

                    consumeKeyword("while");

                    final RValue condition = parseBracket();

                    statements.add(new While(current.getPosition(), condition, body, true));
                    break;
                }

                case 'f': { // for
                    ++position;
                    consumeCharacter('(');
                    final RValue init = parseExpression();
                    consumeCharacter(';');
                    final RValue condition = parseExpression();
                    consumeCharacter(';');
                    final RValue increment = parseExpression();
                    consumeCharacter(')');
                    final RValue body = parseStatements(true);

                    statements.add(new For(current.getPosition(), init, condition, increment, body));
                    break;
                }

                default:
                    throw new ParserException(current.getPosition(), "Unimplemented keyword '" + keyword + "'");
                }

                if (singleStatement) {
                    break loop;
                }
                break;

            default:
                statements.add(parseExpression());
                
                if (peek().id() == ';') {
                    ++position;
                    if (singleStatement) {
                        break loop;
                    }
                    break;
                }
                else {
                    break loop;
                }
            }
        }

        switch (statements.size()) {
        case 0:
            throw new ParserException(-1, "No statement found.");

        case 1:
            return statements.get(0);

        default:
            return new Sequence(position, statements.toArray(new RValue[statements.size()]));
        }
    }

    private final RValue parseExpression() throws ParserException {
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
                } else {
                    RValue variable = variables.get(identifierToken.value);
                    if (variable == null) {
                        if (next instanceof OperatorToken && ((OperatorToken)next).operator.equals("=")) {
                            // Ugly hack to make temporary variables work while not sacrificing error reporting.
                            variables.put(identifierToken.value, variable = new Variable(0));
                        } else {
                            throw new ParserException(current.getPosition(), "Variable '" + identifierToken.value + "' not found");
                        }
                    }
                    halfProcessed.add(variable);
                }
                expressionStart = false;
                break;

            case '(':
                halfProcessed.add(parseBracket());
                expressionStart = false;
                break;

            case ',':
            case ')':
            case '}':
            case ';':
                break loop;

            case 'o':
                if (expressionStart) {
                    // Preprocess prefix operators into unary operators
                    halfProcessed.add(new UnaryOperator((OperatorToken) current));
                } else {
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

        return ParserProcessors.processExpression(halfProcessed);
    }


    private Token peek() {
        if (position >= tokens.size()) {
            return new NullToken(tokens.get(tokens.size() - 1).getPosition() + 1);
        }

        return tokens.get(position);
    }

    private Identifiable parseFunctionCall(IdentifierToken identifierToken) throws ParserException {
        consumeCharacter('(');

        try {
            if (peek().id() == ')') {
                return Functions.getFunction(identifierToken.getPosition(), identifierToken.value);
            }

            List<RValue> args = new ArrayList<RValue>();

            loop: while (true) {
                args.add(parseExpression());

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

            return Functions.getFunction(identifierToken.getPosition(), identifierToken.value, args.toArray(new RValue[args.size()]));
        } catch (NoSuchMethodException e) {
            throw new ParserException(identifierToken.getPosition(), "Function not found", e);
        }
    }

    private final RValue parseBracket() throws ParserException {
        consumeCharacter('(');

        final RValue ret = parseExpression();

        consumeCharacter(')');

        return ret;
    }

    private final RValue parseBlock() throws ParserException {
        consumeCharacter('{');

        if (peek().id() == '}') {
            return new Sequence(peek().getPosition());
        }

        final RValue ret = parseStatements(false);

        consumeCharacter('}');

        return ret;
    }

    private boolean hasKeyword(String keyword) {
        final Token next = peek();
        if (!(next instanceof KeywordToken)) {
            return false;
        }
        return ((KeywordToken) next).value.equals(keyword);
    }

    private void assertCharacter(char character) throws ParserException {
        final Token next = peek();
        if (next.id() != character) {
            throw new ParserException(next.getPosition(), "Expected '" + character + "'");
        }
    }

    private void assertKeyword(String keyword) throws ParserException {
        if (!hasKeyword(keyword)) {
            throw new ParserException(peek().getPosition(), "Expected '" + keyword + "'");
        }
    }

    private void consumeCharacter(char character) throws ParserException {
        assertCharacter(character);
        ++position;
    }

    private void consumeKeyword(String keyword) throws ParserException {
        assertKeyword(keyword);
        ++position;
    }
}
