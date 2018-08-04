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

package com.sk89q.worldedit.internal.expression.lexer;

import com.sk89q.worldedit.internal.expression.lexer.tokens.CharacterToken;
import com.sk89q.worldedit.internal.expression.lexer.tokens.IdentifierToken;
import com.sk89q.worldedit.internal.expression.lexer.tokens.KeywordToken;
import com.sk89q.worldedit.internal.expression.lexer.tokens.NumberToken;
import com.sk89q.worldedit.internal.expression.lexer.tokens.OperatorToken;
import com.sk89q.worldedit.internal.expression.lexer.tokens.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes a string into a list of tokens.
 *
 * <p>Tokens can be numbers, identifiers, operators and assorted other
 * characters.</p>
 */
public class Lexer {

    private final String expression;
    private int position = 0;

    private Lexer(String expression) {
        this.expression = expression;
    }

    public static List<Token> tokenize(String expression) throws LexerException {
        return new Lexer(expression).tokenize();
    }

    private final DecisionTree operatorTree = new DecisionTree(null,
        '+', new DecisionTree("+",
            '=', new DecisionTree("+="),
            '+', new DecisionTree("++")
        ),
        '-', new DecisionTree("-",
            '=', new DecisionTree("-="),
            '-', new DecisionTree("--")
        ),
        '*', new DecisionTree("*",
            '=', new DecisionTree("*="),
            '*', new DecisionTree("**")
        ),
        '/', new DecisionTree("/",
            '=', new DecisionTree("/=")
        ),
        '%', new DecisionTree("%",
            '=', new DecisionTree("%=")
        ),
        '^', new DecisionTree("^",
            '=', new DecisionTree("^=")
        ),
        '=', new DecisionTree("=",
            '=', new DecisionTree("==")
        ),
        '!', new DecisionTree("!",
            '=', new DecisionTree("!=")
        ),
        '<', new DecisionTree("<",
            '<', new DecisionTree("<<"),
            '=', new DecisionTree("<=")
        ),
        '>', new DecisionTree(">",
            '>', new DecisionTree(">>"),
            '=', new DecisionTree(">=")
        ),
        '&', new DecisionTree(null, // not implemented
            '&', new DecisionTree("&&")
        ),
        '|', new DecisionTree(null, // not implemented
            '|', new DecisionTree("||")
        ),
        '~', new DecisionTree("~",
            '=', new DecisionTree("~=")
        )
    );

    private static final Set<Character> characterTokens = new HashSet<>();
    static {
        characterTokens.add(',');
        characterTokens.add('(');
        characterTokens.add(')');
        characterTokens.add('{');
        characterTokens.add('}');
        characterTokens.add(';');
        characterTokens.add('?');
        characterTokens.add(':');
    }

    private static final Set<String> keywords =
            new HashSet<>(Arrays.asList("if", "else", "while", "do", "for", "break", "continue", "return", "switch", "case", "default"));

    private static final Pattern numberPattern = Pattern.compile("^([0-9]*(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)");
    private static final Pattern identifierPattern = Pattern.compile("^([A-Za-z][0-9A-Za-z_]*)");

    private List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<>();

        do {
            skipWhitespace();
            if (position >= expression.length()) {
                break;
            }

            Token token = operatorTree.evaluate(position);
            if (token != null) {
                tokens.add(token);
                continue;
            }

            final char ch = peek();

            if (characterTokens.contains(ch)) {
                tokens.add(new CharacterToken(position++, ch));
                continue;
            }

            final Matcher numberMatcher = numberPattern.matcher(expression.substring(position));
            if (numberMatcher.lookingAt()) {
                String numberPart = numberMatcher.group(1);
                if (!numberPart.isEmpty()) {
                    try {
                        tokens.add(new NumberToken(position, Double.parseDouble(numberPart)));
                    } catch (NumberFormatException e) {
                        throw new LexerException(position, "Number parsing failed", e);
                    }

                    position += numberPart.length();
                    continue;
                }
            }

            final Matcher identifierMatcher = identifierPattern.matcher(expression.substring(position));
            if (identifierMatcher.lookingAt()) {
                String identifierPart = identifierMatcher.group(1);
                if (!identifierPart.isEmpty()) {
                    if (keywords.contains(identifierPart)) {
                        tokens.add(new KeywordToken(position, identifierPart));
                    } else {
                        tokens.add(new IdentifierToken(position, identifierPart));
                    }

                    position += identifierPart.length();
                    continue;
                }
            }

            throw new LexerException(position, "Unknown character '" + ch + "'");
        } while (position < expression.length());

        return tokens;
    }

    private char peek() {
        return expression.charAt(position);
    }

    private void skipWhitespace() {
        while (position < expression.length() && Character.isWhitespace(peek())) {
            ++position;
        }
    }

    public class DecisionTree {
        private final String tokenName;
        private final Map<Character, DecisionTree> subTrees = new HashMap<>();

        private DecisionTree(String tokenName, Object... args) {
            this.tokenName = tokenName;

            if (args.length % 2 != 0) {
                throw new UnsupportedOperationException("You need to pass an even number of arguments.");
            }

            for (int i = 0; i < args.length; i += 2) {
                if (!(args[i] instanceof Character)) {
                    throw new UnsupportedOperationException("Argument #" + i + " expected to be 'Character', not '" + args[i].getClass().getName() + "'.");
                }
                if (!(args[i + 1] instanceof DecisionTree)) {
                    throw new UnsupportedOperationException("Argument #" + (i + 1) + " expected to be 'DecisionTree', not '" + args[i + 1].getClass().getName() + "'.");
                }

                Character next = (Character) args[i];
                DecisionTree subTree = (DecisionTree) args[i + 1];

                subTrees.put(next, subTree);
            }
        }

        private Token evaluate(int startPosition) throws LexerException {
            if (position < expression.length()) {
                final char next = peek();

                final DecisionTree subTree = subTrees.get(next);
                if (subTree != null) {
                    ++position;
                    final Token subTreeResult = subTree.evaluate(startPosition);
                    if (subTreeResult != null) {
                        return subTreeResult;
                    }
                    --position;
                }
            }

            if (tokenName == null) {
                return null;
            }

            return new OperatorToken(startPosition, tokenName);
        }
    }

}
