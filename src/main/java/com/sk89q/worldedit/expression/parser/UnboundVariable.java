package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.runtime.EvaluationException;
import com.sk89q.worldedit.expression.runtime.LValue;
import com.sk89q.worldedit.expression.runtime.RValue;

public class UnboundVariable extends PseudoToken implements LValue {
    public final String name;

    public UnboundVariable(int position, String name) {
        super(position);
        this.name = name;
        // TODO Auto-generated constructor stub
    }

    @Override
    public char id() {
        // TODO Auto-generated method stub
        return 'V';
    }

    @Override
    public String toString() {
        return "UnboundVariable(" + name + ")";
    }

    @Override
    public double getValue() throws EvaluationException {
        throw new EvaluationException(getPosition(), "Tried to evaluate unbound variable!");
    }

    @Override
    public LValue optimize() throws EvaluationException {
        throw new EvaluationException(getPosition(), "Tried to optimize unbound variable!");
    }

    @Override
    public double assign(double value) throws EvaluationException {
        throw new EvaluationException(getPosition(), "Tried to assign unbound variable!");
    }

    public RValue bind(Expression expression, boolean isLValue) throws ParserException {
        final RValue variable = expression.getVariable(name, isLValue);
        if (variable == null) {
            throw new ParserException(getPosition(), "Variable '" + name + "' not found");
        }

        return variable;
    }

    @Override
    public LValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        final RValue variable = expression.getVariable(name, preferLValue);
        if (variable == null) {
            throw new ParserException(getPosition(), "Variable '" + name + "' not found");
        }

        return (LValue) variable;
    }
}
