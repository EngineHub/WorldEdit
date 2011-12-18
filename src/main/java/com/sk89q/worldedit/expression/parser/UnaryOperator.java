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

import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;

/**
 * The parser uses this pseudo-token to mark operators as unary operators.
 *
 * @author TomyLobo
 */
public class UnaryOperator extends PseudoToken {
    final String operator;

    public UnaryOperator(OperatorToken operatorToken) {
        this(operatorToken.getPosition(), operatorToken.operator);
    }

    public UnaryOperator(int position, String operator) {
        super(position);
        this.operator = operator;
    }

    @Override
    public char id() {
        return 'p';
    }

    @Override
    public String toString() {
        return "UnaryOperator(" + operator + ")";
    }
}
