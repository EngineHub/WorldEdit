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
