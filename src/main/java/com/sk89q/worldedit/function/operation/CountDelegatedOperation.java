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

package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.WorldEditException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A wrapper class that delegates methods of both Operation and AffectedCounter,
 * so that a CountDelegatedOperation can be used where desired.
 */
public class CountDelegatedOperation implements CountingOperation {
    private Operation operation;
    private AffectedCounter counter;

    public CountDelegatedOperation(Operation op, AffectedCounter counter) {
        checkArgument(op != counter);
        this.operation = op;
        this.counter = counter;
    }

    @Override
    public int getAffected() {
        return counter.getAffected();
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        operation = operation.resume(run);
        return (operation == null) ? null : this;
    }

    @Override
    public void cancel() {
        operation.cancel();
    }
}
