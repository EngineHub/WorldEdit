/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.expression;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static com.sk89q.worldedit.internal.expression.ExpressionTestCase.testCase;
import static java.lang.Math.atan2;
import static java.lang.Math.sin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ExpressionTest extends BaseExpressionTest {

    @TestFactory
    public Stream<DynamicNode> testEvaluate() throws ExpressionException {
        List<ExpressionTestCase> testCases = ImmutableList.of(
            // basic arithmetic
            testCase("1 - 2 + 3", 2),
            // unary ops
            testCase("2 + +4", 6),
            testCase("2 - -4", 6),
            testCase("2 * -4", -8),
            // check functions
            testCase("sin(5)", sin(5)),
            testCase("atan2(3, 4)", atan2(3, 4)),
            testCase("min(1, 2)", 1),
            testCase("max(1, 2)", 2),
            testCase("max(1, 2, 3, 4, 5)", 5),
            // check conditionals
            testCase("0 || 5", 5),
            testCase("2 || 5", 2),
            testCase("2 && 5", 5),
            testCase("5 && 0", 0),
            // check ternaries
            testCase("false ? 1 : 2", 2),
            testCase("true ? 1 : 2", 1),
            // - ternary binds inside
            testCase("true ? true ? 1 : 2 : 3", 1),
            testCase("true ? false ? 1 : 2 : 3", 2),
            testCase("false ? true ? 1 : 2 : 3", 3),
            testCase("false ? false ? 1 : 2 : 3", 3),
            // check return
            testCase("return 1; 0", 1)
        );
        return testCases.stream()
            .map(testCase -> dynamicTest(
                testCase.getExpression(),
                () -> checkTestCase(testCase)
            ));
    }

    @Test
    void testVariables() {
        assertEquals(8, compile("foo+bar", "foo", "bar").evaluate(5D, 3D), 0);

        // variables need to be assigned first
        EvaluationException ex = assertThrows(EvaluationException.class,
            () -> simpleEval("a*=5"));
        assertTrue(ex.getMessage().contains("not initialized yet"));

        // can't modify e, pi, true, false

    }

    @TestFactory
    Stream<DynamicNode> testModifyConstants() {
        return Stream.of("e", "pi", "true", "false").map(constant ->
            dynamicTest(constant, () -> {
                EvaluationException ex = assertThrows(EvaluationException.class, () ->
                    simpleEval(constant + "++"));
                assertTrue(ex.getMessage().endsWith("'" + constant + "' is not a variable"));
            }));
    }

    @Test
    void testTightTokenization() {
        checkTestCase("3+1", 4);
    }

    @Test
    void testPostPreOps() {
        checkTestCase("a=0; b=a++; a+b", 1);
        checkTestCase("a=0; b=++a; a+b", 2);
        checkTestCase("a=0; b=a--; a+b", -1);
        checkTestCase("a=0; b=--a; a+b", -2);
    }

    @Test
    void testNot() {
        checkTestCase("!0", 1);
        checkTestCase("!1", 0);
        checkTestCase("!2", 0);
        checkTestCase("!-1", 0);
        checkTestCase("!-2", 0);
    }

    @Test
    void testComplement() {
        checkTestCase("~0", ~0);
        checkTestCase("~1", ~1);
        checkTestCase("~-1", ~-1);
        checkTestCase("~-2", ~-2);
        // it drops the decimal!
        checkTestCase("~0.1", ~0);
        checkTestCase("~0.5", ~0);
        checkTestCase("~1.9", ~1);
    }

    @Test
    void testShift() {
        checkTestCase("1<<4", 1 << 4);
        // drops both decimals
        checkTestCase("1.1<<4.1", 1 << 4);
        checkTestCase("16>>2", 16 >> 2);
        checkTestCase("16.9>>2.1", 16 >> 2);
    }

    @Test
    void testComparisonOps() {
        checkTestCase("1>=0", 1);
        checkTestCase("1>0", 1);

        checkTestCase("0>=0", 1);
        checkTestCase("0>0", 0);

        checkTestCase("0<=1", 1);
        checkTestCase("0<1", 1);

        checkTestCase("0<=0", 1);
        checkTestCase("0<0", 0);

        checkTestCase("1>=2", 0);
        checkTestCase("1>2", 0);

        checkTestCase("0>=1", 0);
        checkTestCase("0>1", 0);

        checkTestCase("2<=1", 0);
        checkTestCase("2<1", 0);

        checkTestCase("1<=0", 0);
        checkTestCase("1<0", 0);
    }

    @Test
    void testEqualityOps() {
        checkTestCase("1==1", 1);
        checkTestCase("0==1", 0);
        checkTestCase("1==0", 0);

        checkTestCase("1!=1", 0);
        checkTestCase("0!=1", 1);
        checkTestCase("1!=0", 1);

        checkTestCase("1.1==1.1", 1);
        // These aren't normally equal
        checkTestCase("1!=0.999999999", 1);
        // But they are _almost_ equal!
        checkTestCase("1~=0.999999999", 1);
        // On the other hand, these aren't
        checkTestCase("1~=0.9", 0);
    }

    @Test
    void testPostfixOps() {
        checkTestCase("(-1)!", 0);
        checkTestCase("0!", 1);
        checkTestCase("1!", 1);
        checkTestCase("2!", 2);
        checkTestCase("2000!", Double.POSITIVE_INFINITY);
        // it truncates
        checkTestCase("2.9!", 2);
    }

    @Test
    void testAssignOps() {
        checkTestCase("a=2; a^=3; a", 8);
        checkTestCase("a=2; a*=3; a", 6);
        checkTestCase("a=2; a/=2; a", 1);
        checkTestCase("a=2; a%=3; a", 2);
        checkTestCase("a=2; a+=3; a", 5);
        checkTestCase("a=2; a-=3; a", -1);
    }

    @Test
    public void testErrors() {
        // test lexer errors
        {
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("#"));
            assertEquals(0, e.getPosition(), "Error position");
        }
        // test parser errors
        {
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("x"));
            assertEquals(0, e.getPosition(), "Error position");
        }
        {
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("x()"));
            assertEquals(0, e.getPosition(), "Error position");
        }
        {
            // verify that you must return a value
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("return"));
            assertEquals(6, e.getPosition(), "Error position");
        }
        assertThrows(ExpressionException.class,
            () -> compile("("));
        assertThrows(ExpressionException.class,
            () -> compile("x("));
        // test overloader errors
        {
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("atan2(1)"));
            assertEquals(0, e.getPosition(), "Error position");
        }
        {
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("atan2(1, 2, 3)"));
            assertEquals(0, e.getPosition(), "Error position");
        }
        {
            ExpressionException e = assertThrows(ExpressionException.class,
                () -> compile("rotate(1, 2, 3)"));
            assertEquals(7, e.getPosition(), "Error position");
        }

    }

    @Test
    public void testAssign() throws ExpressionException {
        Expression foo = compile("{a=x} b=y; c=z", "x", "y", "z", "a", "b", "c");
        foo.evaluate(2D, 3D, 5D);
        assertEquals(2, foo.getSlots().getSlotValue("a").orElse(-1), 0);
        assertEquals(3, foo.getSlots().getSlotValue("b").orElse(-1), 0);
        assertEquals(5, foo.getSlots().getSlotValue("c").orElse(-1), 0);
    }

    @Test
    public void testIf() throws ExpressionException {
        checkTestCase("y=0; if (1) x=4; else y=5; x*10+y;", 40);
        checkTestCase("x=0; if (0) x=4; else y=5; x*10+y;", 5);

        // test 'dangling else'
        final Expression expression1 = compile("if (1) if (0) x=4; else y=5;", "x", "y");
        expression1.evaluate(1D, 2D);
        assertEquals(1, expression1.getSlots().getSlotValue("x").orElse(-1), 0);
        assertEquals(5, expression1.getSlots().getSlotValue("y").orElse(-1), 0);

        // test if the if construct is correctly recognized as a statement
        final Expression expression2 = compile("if (0) if (1) x=5; y=4;", "x", "y");
        expression2.evaluate(1D, 2D);
        assertEquals(4, expression2.getSlots().getSlotValue("y").orElse(-1), 0);
    }

    @Test
    public void testWhile() throws ExpressionException {
        checkTestCase("c=5; a=0; while (c > 0) { ++a; --c; } a", 5);
        checkTestCase("c=5; a=0; do { ++a; --c; } while (c > 0); a", 5);
        checkTestCase(""
            + "c=5;"
            + "a=0;"
            + "while (c > 0) {"
            + "   ++a;"
            + "   --c;"
            + "   if (c == 1) break;"
            + "}"
            + "a", 4);
        checkTestCase(""
            + "c=5;"
            + "a=0;"
            + "while (c > 0) {"
            + "   ++a;"
            + "   if (a < 5) continue;"
            + "   --c;"
            + "}"
            + "a", 9);
        checkTestCase(""
            + "c=5;"
            + "a=0;"
            + "do {"
            + "   ++a;"
            + "   --c;"
            + "   if (c == 1) break;"
            + "} while (c > 0);"
            + "a", 4);
        checkTestCase(""
            + "c=5;"
            + "a=0;"
            + "do {"
            + "   ++a;"
            + "   if (a < 5) continue;"
            + "   --c;"
            + "} while (c > 0);"
            + "a", 9);
    }

    @Test
    public void testFor() throws ExpressionException {
        checkTestCase("a=0; for (i=0; i<5; ++i) { ++a; } a", 5);
        checkTestCase("y=0; for (i=1,5) { y *= 10; y += i; } y", 12345);
        checkTestCase(""
            + "a=0;"
            + "for (c = 5; c > 0; c--) {"
            + "   ++a;"
            + "   if (c == 2) break;"
            + "}"
            + "a", 4);
        checkTestCase(""
            + "a=0;"
            + "for (c = 5; c > 0; c--) {"
            + "   if (a > 1) continue;"
            + "   ++a;"
            + "}"
            + "a", 2);
        checkTestCase(""
            + "a=0;"
            + "for (c = 1,5) {"
            + "   ++a;"
            + "   if (c == 4) break;"
            + "}"
            + "a", 4);
        checkTestCase(""
            + "a=0;"
            + "for (c = 1,5) {"
            + "   if (a > 1) continue;"
            + "   ++a;"
            + "}"
            + "a", 2);
    }

    @Test
    public void testSwitch() throws ExpressionException {
        checkTestCase("x=1;y=2;z=3;switch (1) { case 1: x=5; break; case 2: y=6; break; default: z=7; break } x*100+y*10+z", 523);
        checkTestCase("x=1;y=2;z=3;switch (2) { case 1: x=5; break; case 2: y=6; break; default: z=7; break } x*100+y*10+z", 163);
        checkTestCase("x=1;y=2;z=3;switch (3) { case 1: x=5; break; case 2: y=6; break; default: z=7; break } x*100+y*10+z", 127);

        checkTestCase("x=1;y=2;z=3;switch (1) { case 1: x=5; case 2: y=6; default: z=7 } x*100+y*10+z", 567);
        checkTestCase("x=1;y=2;z=3;switch (2) { case 1: x=5; case 2: y=6; default: z=7 } x*100+y*10+z", 167);
        checkTestCase("x=1;y=2;z=3;switch (3) { case 1: x=5; case 2: y=6; default: z=7 } x*100+y*10+z", 127);

        checkTestCase("x=1;y=2;z=3;switch (1) { case 1: x=5; case 2: y=6 } x*100+y*10+z", 563);

        // try to continue in a switch :P
        {
            EvaluationException ex = assertThrows(EvaluationException.class, () -> simpleEval(""
                + "switch(1) {"
                + "   case 1: continue;"
                + "}"));
            assertTrue(ex.getMessage().contains("continue in a switch"));
        }
        {
            EvaluationException ex = assertThrows(EvaluationException.class, () -> simpleEval(""
                + "switch(1) {"
                + "   default: continue;"
                + "}"));
            assertTrue(ex.getMessage().contains("continue in a switch"));
        }
        // duplicate case checks
        {
            EvaluationException ex = assertThrows(EvaluationException.class, () -> simpleEval(""
                + "switch(1) {"
                + "   case 1: 1;"
                + "   case 1: 1;"
                + "}"));
            assertTrue(ex.getMessage().contains("Duplicate cases"));
        }
        {
            EvaluationException ex = assertThrows(EvaluationException.class, () -> simpleEval(""
                + "switch(1) {"
                + "   default: 1;"
                + "   default: 1;"
                + "}"));
            assertTrue(ex.getMessage().contains("Duplicate default cases"));
        }
    }

    @Test
    public void testQuery() {
        checkTestCase("a=1;b=2;query(3,4,5,a,b); a==3 && b==4", 1);
        checkTestCase("a=1;b=2;queryAbs(3,4,5,a*1,b*1); a==1 && b==2", 1);
        checkTestCase("a=1;b=2;queryRel(3,4,5,(a),(b)); a==300 && b==400", 1);
        checkTestCase("query(3,4,5,3,4)", 1);
        checkTestCase("!query(3,4,5,3,2)", 1);
        checkTestCase("!queryAbs(3,4,5,10,40)", 1);
        checkTestCase("!queryRel(3,4,5,100,200)", 1);
    }

    @Test
    public void testTimeout() {
        ExpressionTimeoutException e = assertTimeoutPreemptively(Duration.ofSeconds(10), () ->
            assertThrows(ExpressionTimeoutException.class,
                () -> simpleEval("for(i=0;i<256;i++){for(j=0;j<256;j++){for(k=0;k<256;k++){for(l=0;l<256;l++){ln(pi)}}}}"),
                "Loop was not stopped.")
        );
        assertTrue(e.getMessage().contains("Calculations exceeded time limit"));
    }

}
