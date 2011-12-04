package com.sk89q.worldedit.expression.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Switch extends Node implements RValue {
    private final RValue parameter;
    private final Map<Double, Integer> valueMap = new HashMap<Double, Integer>();
    private final RValue[] caseStatements;
    private final RValue defaultCase;

    public Switch(int position, RValue parameter, List<Double> values, List<RValue> caseStatements, RValue defaultCase) {
        super(position);
        this.parameter = parameter;

        assert(values.size() == caseStatements.size());

        for (int i = 0; i < values.size(); ++i) {
            valueMap.put(values.get(i), i);
        }

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
}
