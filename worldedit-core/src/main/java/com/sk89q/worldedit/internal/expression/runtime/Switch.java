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

package com.sk89q.worldedit.internal.expression.runtime;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.parser.ParserException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A switch/case construct.
 */
public class Switch extends Node implements RValue {

    private RValue parameter;
    private final Map<Double, Integer> valueMap;
    private final RValue[] caseStatements;
    private RValue defaultCase;

    public Switch(int position, RValue parameter, List<Double> values, List<RValue> caseStatements, RValue defaultCase) {
        this(position, parameter, invertList(values), caseStatements, defaultCase);

    }

    private static Map<Double, Integer> invertList(List<Double> values) {
        Map<Double, Integer> valueMap = new HashMap<>();
        for (int i = 0; i < values.size(); ++i) {
            valueMap.put(values.get(i), i);
        }
        return valueMap;
    }

    private Switch(int position, RValue parameter, Map<Double, Integer> valueMap, List<RValue> caseStatements, RValue defaultCase) {
        super(position);

        this.parameter = parameter;
        this.valueMap = valueMap;
        this.caseStatements = caseStatements.toArray(new RValue[caseStatements.size()]);
        this.defaultCase = defaultCase;
    }

    @Override
    public char id() {
        return 'W';
    }

    @Override
    public double getValue() throws EvaluationException {
        final double parameter = this.parameter.getValue();

        try {
            double ret = 0.0;

            final Integer index = valueMap.get(parameter);
            if (index != null) {
                for (int i = index; i < caseStatements.length; ++i) {
                    ret = caseStatements[i].getValue();
                }
            }

            return defaultCase == null ? ret : defaultCase.getValue();
        } catch (BreakException e) {
            if (e.doContinue) throw e;

            return 0.0;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("switch (");
        sb.append(parameter);
        sb.append(") { ");

        for (int i = 0; i < caseStatements.length; ++i) {
            RValue caseStatement = caseStatements[i];
            sb.append("case ");
            for (Entry<Double, Integer> entry : valueMap.entrySet()) {
                if (entry.getValue() == i) {
                    sb.append(entry.getKey());
                    break;
                }
            }
            sb.append(": ");
            sb.append(caseStatement);
            sb.append(' ');
        }

        if (defaultCase != null) {
            sb.append("default: ");
            sb.append(defaultCase);
            sb.append(' ');
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public RValue optimize() throws EvaluationException {
        final RValue optimizedParameter = parameter.optimize();
        final List<RValue> newSequence = new ArrayList<>();

        if (optimizedParameter instanceof Constant) {
            final double parameter = optimizedParameter.getValue();

            final Integer index = valueMap.get(parameter);
            if (index == null) {
                return defaultCase == null ? new Constant(getPosition(), 0.0) : defaultCase.optimize();
            }

            boolean breakDetected = false;
            for (int i = index; i < caseStatements.length && !breakDetected; ++i) {
                final RValue invokable = caseStatements[i].optimize();

                if (invokable instanceof Sequence) {
                    for (RValue subInvokable : ((Sequence) invokable).sequence) {
                        if (subInvokable instanceof Break) {
                            breakDetected = true;
                            break;
                        }

                        newSequence.add(subInvokable);
                    }
                } else {
                    newSequence.add(invokable);
                }
            }

            if (defaultCase != null && !breakDetected) {
                final RValue invokable = defaultCase.optimize();

                if (invokable instanceof Sequence) {
                    Collections.addAll(newSequence, ((Sequence) invokable).sequence);
                } else {
                    newSequence.add(invokable);
                }
            }

            return new Switch(getPosition(), optimizedParameter, Collections.singletonMap(parameter, 0), newSequence, null);
        }

        final Map<Double, Integer> newValueMap = new HashMap<>();

        Map<Integer, Double> backMap = new HashMap<>();
        for (Entry<Double, Integer> entry : valueMap.entrySet()) {
            backMap.put(entry.getValue(), entry.getKey());
        }

        for (int i = 0; i < caseStatements.length; ++i) {
            final RValue invokable = caseStatements[i].optimize();

            final Double caseValue = backMap.get(i);
            if (caseValue != null) {
                newValueMap.put(caseValue, newSequence.size());
            }

            if (invokable instanceof Sequence) {
                Collections.addAll(newSequence, ((Sequence) invokable).sequence);
            } else {
                newSequence.add(invokable);
            }
        }

        return new Switch(getPosition(), optimizedParameter, newValueMap, newSequence, defaultCase.optimize());
    }

    @Override
    public RValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        parameter = parameter.bindVariables(expression, false);

        for (int i = 0; i < caseStatements.length; ++i) {
            caseStatements[i] = caseStatements[i].bindVariables(expression, false);
        }

        defaultCase = defaultCase.bindVariables(expression, false);

        return this;
    }

}
