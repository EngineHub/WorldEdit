package com.sk89q.worldedit.expression;

import static org.junit.Assert.*;
import static java.lang.Math.*;

import org.junit.*;

import com.sk89q.worldedit.expression.lexer.LexerException;
import com.sk89q.worldedit.expression.parser.ParserException;

public class ExpressionTest {
    @Test
    public void testEvaluate() throws Exception {
        // check 
        assertEquals(1-2+3, simpleEval("1-2+3"), 0);

        // check unary ops
        assertEquals(2+ +4, simpleEval("2++4"), 0);
        assertEquals(2- -4, simpleEval("2--4"), 0);
        assertEquals(2*-4, simpleEval("2*-4"), 0);

        // check functions
        assertEquals(sin(5), simpleEval("sin(5)"), 0);
        assertEquals(atan2(3,4), simpleEval("atan2(3,4)"), 0);

        // check variables
        assertEquals(8, Expression.compile("foo+bar", "foo", "bar").evaluate(5, 3), 0);
    }

    @Test
    public void testErrors() throws ExpressionException {
        // test lexer errors
        try {
            Expression.compile("{");
            fail("Error expected");
        } catch (LexerException e) {
            assertEquals("Error position", 0, e.getPosition());
        }

        // test parser errors
        try {
            Expression.compile("x");
            fail("Error expected");
        } catch (ParserException e) {
            assertEquals("Error position", 0, e.getPosition());
        }
        try {
            Expression.compile("x()");
            fail("Error expected");
        } catch (ParserException e) {
            assertEquals("Error position", 0, e.getPosition());
        }
        try {
            Expression.compile("(");
            fail("Error expected");
        } catch (ParserException e) {}
        try {
            Expression.compile("x(");
            fail("Error expected");
        } catch (ParserException e) {}
    }

    private double simpleEval(String expression) throws Exception {
        return Expression.compile(expression).evaluate();
    }
}
