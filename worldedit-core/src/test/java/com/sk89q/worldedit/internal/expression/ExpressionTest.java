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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.expression.lexer.LexerException;
import com.sk89q.worldedit.internal.expression.parser.ParserException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.internal.expression.runtime.ExpressionEnvironment;
import com.sk89q.worldedit.internal.expression.runtime.ExpressionTimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Math.atan2;
import static java.lang.Math.sin;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExpressionTest {

    private Platform mockPlat = mock(Platform.class);

    @BeforeEach
    public void setup() {
        when(mockPlat.getConfiguration()).thenReturn(new LocalConfiguration() {
            @Override
            public void load() {
            }
        });
        WorldEdit.getInstance().getPlatformManager().register(mockPlat);
    }

    @AfterEach
    public void tearDown() {
        WorldEdit.getInstance().getPlatformManager().unregister(mockPlat);
    }

    @Test
    public void testEvaluate() throws ExpressionException {
        // check
        assertEquals(1 - 2 + 3, simpleEval("1 - 2 + 3"), 0);

        // check unary ops
        assertEquals(2 + +4, simpleEval("2 + +4"), 0);
        assertEquals(2 - -4, simpleEval("2 - -4"), 0);
        assertEquals(2 * -4, simpleEval("2 * -4"), 0);

        // check functions
        assertEquals(sin(5), simpleEval("sin(5)"), 0);
        assertEquals(atan2(3, 4), simpleEval("atan2(3, 4)"), 0);

        // check variables
        assertEquals(8, compile("foo+bar", "foo", "bar").evaluate(5D, 3D), 0);
    }

    @Test
    public void testErrors() {
        assertAll(
            // test lexer errors
            () -> {
                LexerException e = assertThrows(LexerException.class,
                    () -> compile("#"));
                assertEquals(0, e.getPosition(), "Error position");
            },
            // test parser errors
            () -> {
                ParserException e = assertThrows(ParserException.class,
                    () -> compile("x"));
                assertEquals(0, e.getPosition(), "Error position");
            },
            () -> {
                ParserException e = assertThrows(ParserException.class,
                    () -> compile("x()"));
                assertEquals(0, e.getPosition(), "Error position");
            },
            () -> assertThrows(ParserException.class,
                () -> compile("(")),
            () -> assertThrows(ParserException.class,
                () -> compile("x(")),
            // test overloader errors
            () -> {
                ParserException e = assertThrows(ParserException.class,
                    () -> compile("atan2(1)"));
                assertEquals(0, e.getPosition(), "Error position");
            },
            () -> {
                ParserException e = assertThrows(ParserException.class,
                    () -> compile("atan2(1, 2, 3)"));
                assertEquals(0, e.getPosition(), "Error position");
            },
            () -> {
                ParserException e = assertThrows(ParserException.class,
                    () -> compile("rotate(1, 2, 3)"));
                assertEquals(0, e.getPosition(), "Error position");
            }
        );
    }

    @Test
    public void testAssign() throws ExpressionException {
        Expression foo = compile("{a=x} b=y; c=z", "x", "y", "z", "a", "b", "c");
        foo.evaluate(2D, 3D, 5D);
        assertEquals(2, foo.getVariable("a", false).getValue(), 0);
        assertEquals(3, foo.getVariable("b", false).getValue(), 0);
        assertEquals(5, foo.getVariable("c", false).getValue(), 0);
    }

    @Test
    public void testIf() throws ExpressionException {
        assertEquals(40, simpleEval("if (1) x=4; else y=5; x*10+y;"), 0);
        assertEquals(5, simpleEval("if (0) x=4; else y=5; x*10+y;"), 0);

        // test 'dangling else'
        final Expression expression1 = compile("if (1) if (0) x=4; else y=5;", "x", "y");
        expression1.evaluate(1D, 2D);
        assertEquals(1, expression1.getVariable("x", false).getValue(), 0);
        assertEquals(5, expression1.getVariable("y", false).getValue(), 0);

        // test if the if construct is correctly recognized as a statement
        final Expression expression2 = compile("if (0) if (1) x=5; y=4;", "x", "y");
        expression2.evaluate(1D, 2D);
        assertEquals(4, expression2.getVariable("y", false).getValue(), 0);
    }

    @Test
    public void testWhile() throws ExpressionException {
        assertEquals(5, simpleEval("c=5; a=0; while (c > 0) { ++a; --c; } a"), 0);
        assertEquals(5, simpleEval("c=5; a=0; do { ++a; --c; } while (c > 0); a"), 0);
    }

    @Test
    public void testFor() throws ExpressionException {
        assertEquals(5, simpleEval("a=0; for (i=0; i<5; ++i) { ++a; } a"), 0);
        assertEquals(12345, simpleEval("y=0; for (i=1,5) { y *= 10; y += i; } y"), 0);
    }

    @Test
    public void testSwitch() throws ExpressionException {
        assertEquals(523, simpleEval("x=1;y=2;z=3;switch (1) { case 1: x=5; break; case 2: y=6; break; default: z=7 } x*100+y*10+z"), 0);
        assertEquals(163, simpleEval("x=1;y=2;z=3;switch (2) { case 1: x=5; break; case 2: y=6; break; default: z=7 } x*100+y*10+z"), 0);
        assertEquals(127, simpleEval("x=1;y=2;z=3;switch (3) { case 1: x=5; break; case 2: y=6; break; default: z=7 } x*100+y*10+z"), 0);

        assertEquals(567, simpleEval("x=1;y=2;z=3;switch (1) { case 1: x=5; case 2: y=6; default: z=7 } x*100+y*10+z"), 0);
        assertEquals(167, simpleEval("x=1;y=2;z=3;switch (2) { case 1: x=5; case 2: y=6; default: z=7 } x*100+y*10+z"), 0);
        assertEquals(127, simpleEval("x=1;y=2;z=3;switch (3) { case 1: x=5; case 2: y=6; default: z=7 } x*100+y*10+z"), 0);
    }

    @Test
    public void testQuery() throws Exception {
        assertEquals(1, simpleEval("a=1;b=2;query(3,4,5,a,b); a==3 && b==4"), 0);
        assertEquals(1, simpleEval("a=1;b=2;queryAbs(3,4,5,a*1,b*1); a==1 && b==2"), 0);
        assertEquals(1, simpleEval("a=1;b=2;queryRel(3,4,5,(a),(b)); a==300 && b==400"), 0);
        assertEquals(1, simpleEval("query(3,4,5,3,4)"), 0);
        assertEquals(1, simpleEval("!query(3,4,5,3,2)"), 0);
        assertEquals(1, simpleEval("!queryAbs(3,4,5,10,40)"), 0);
        assertEquals(1, simpleEval("!queryRel(3,4,5,100,200)"), 0);
    }

    @Test
    public void testTimeout() {
        ExpressionTimeoutException e = assertThrows(ExpressionTimeoutException.class,
            () -> simpleEval("for(i=0;i<256;i++){for(j=0;j<256;j++){for(k=0;k<256;k++){for(l=0;l<256;l++){ln(pi)}}}}"),
            "Loop was not stopped.");
        assertTrue(e.getMessage().contains("Calculations exceeded time limit"));
    }

    private double simpleEval(String expressionString) throws ExpressionException {
        final Expression expression = compile(expressionString);

        expression.setEnvironment(new ExpressionEnvironment() {
            @Override
            public int getBlockType(double x, double y, double z) {
                return (int) x;
            }

            @Override
            public int getBlockData(double x, double y, double z) {
                return (int) y;
            }

            @Override
            public int getBlockTypeAbs(double x, double y, double z) {
                return (int) x*10;
            }

            @Override
            public int getBlockDataAbs(double x, double y, double z) {
                return (int) y*10;
            }

            @Override
            public int getBlockTypeRel(double x, double y, double z) {
                return (int) x*100;
            }

            @Override
            public int getBlockDataRel(double x, double y, double z) {
                return (int) y*100;
            }
        });

        return expression.evaluate();
    }

    private Expression compile(String expressionString, String... variableNames) throws ExpressionException, EvaluationException {
        final Expression expression = Expression.compile(expressionString, variableNames);
        expression.optimize();
        return expression;
    }
}
