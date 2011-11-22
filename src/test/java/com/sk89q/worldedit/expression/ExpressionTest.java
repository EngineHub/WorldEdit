package com.sk89q.worldedit.expression;

import static org.junit.Assert.*;
import static java.lang.Math.*;

import org.junit.*;

import com.sk89q.worldedit.expression.lexer.LexerException;
import com.sk89q.worldedit.expression.parser.ParserException;

public class ExpressionTest {
    @Test
    public void testEvaluate() throws ExpressionException {
        // check 
        assertEquals(1-2+3, simpleEval("1-2+3"), 0);

        // check unary ops
        assertEquals(2+ +4, simpleEval("2+ +4"), 0);
        assertEquals(2- -4, simpleEval("2- -4"), 0);
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
            Expression.compile("#");
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

    @Test
    public void testAssign() throws ExpressionException {
        Expression foo = Expression.compile("{a=x} b=y; c=z", "x", "y", "z", "a", "b", "c");
        foo.evaluate(2, 3, 5);
        assertEquals(2, foo.getVariable("a").getValue(), 0);
        assertEquals(3, foo.getVariable("b").getValue(), 0);
        assertEquals(5, foo.getVariable("c").getValue(), 0);
    }

    @Test
    public void testIf() throws ExpressionException {
        assertEquals(40, simpleEval("if (1) x=4; else y=5; x*10+y;"), 0);
        assertEquals(5, simpleEval("if (0) x=4; else y=5; x*10+y;"), 0);

        // test 'dangling else'
        final Expression expression1 = Expression.compile("if (1) if (0) x=4; else y=5;", "x", "y");
        expression1.evaluate(1, 2);
        assertEquals(1, expression1.getVariable("x").getValue(), 0);
        assertEquals(5, expression1.getVariable("y").getValue(), 0);

        // test if the if construct is correctly recognized as a statement
        final Expression expression2 = Expression.compile("if (0) if (1) x=5; y=4;", "x", "y");
        expression2.evaluate(1, 2);
        assertEquals(4, expression2.getVariable("y").getValue(), 0);
    }

    @Test
    public void testWhile() throws ExpressionException {
        assertEquals(5, simpleEval("c=5; a=0; while (c > 0) { ++a; --c; } a"), 0);
        assertEquals(5, simpleEval("c=5; a=0; do { ++a; --c; } while (c > 0) a"), 0);
    }

    @Test
    public void testFor() throws ExpressionException {
        assertEquals(5, simpleEval("a=0; for (i=0; i<5; ++i) { ++a; } a"), 0);
    }

    private double simpleEval(String expression) throws ExpressionException {
        return Expression.compile(expression).evaluate();
    }
}
