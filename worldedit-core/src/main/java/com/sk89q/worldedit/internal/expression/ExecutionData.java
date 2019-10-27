package com.sk89q.worldedit.internal.expression;

import com.google.common.collect.SetMultimap;

import java.lang.invoke.MethodHandle;

import static java.util.Objects.requireNonNull;

public class ExecutionData {

    /**
     * Special execution context for evaluating constant values. As long as no variables are used,
     * it can be considered constant.
     */
    public static final ExecutionData CONSTANT_EVALUATOR = new ExecutionData(null, null);

    private final SlotTable slots;
    private final SetMultimap<String, MethodHandle> functions;

    public ExecutionData(SlotTable slots, SetMultimap<String, MethodHandle> functions) {
        this.slots = slots;
        this.functions = functions;
    }

    public SlotTable getSlots() {
        return requireNonNull(slots, "Cannot use variables in a constant");
    }

    public SetMultimap<String, MethodHandle> getFunctions() {
        return requireNonNull(functions, "Cannot use functions in a constant");
    }
}
