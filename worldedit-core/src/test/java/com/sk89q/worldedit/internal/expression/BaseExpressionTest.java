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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Common setup code for expression tests.
 */
class BaseExpressionTest {

    static double readSlot(Expression expr, String name) {
        return expr.getSlots().getSlotValue(name).orElseThrow(IllegalStateException::new);
    }

    private Platform mockPlat = mock(Platform.class);

    @BeforeEach
    void setup() {
        when(mockPlat.getConfiguration()).thenReturn(new LocalConfiguration() {
            @Override
            public void load() {
            }
        });
        WorldEdit.getInstance().getPlatformManager().register(mockPlat);
        WorldEdit.getInstance().getConfiguration().calculationTimeout = 1_000;
    }

    @AfterEach
    void tearDown() {
        WorldEdit.getInstance().getPlatformManager().unregister(mockPlat);
    }

    void checkTestCase(String expression, double result) {
        checkTestCase(ExpressionTestCase.testCase(expression, result));
    }

    void checkTestCase(ExpressionTestCase testCase) {
        assertEquals(testCase.getResult(), simpleEval(testCase.getExpression()), 0);
    }

    double simpleEval(String expressionString) throws ExpressionException {
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

    Expression compile(String expressionString, String... variableNames) throws ExpressionException {
        final Expression expression = Expression.compile(expressionString, variableNames);
        expression.optimize();
        return expression;
    }
}
