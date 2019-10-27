package com.sk89q.worldedit.internal.expression;

/**
 * Represents a "compiled" expression.
 */
public interface CompiledExpression {

    Double execute(ExecutionData executionData);

}
