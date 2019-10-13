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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Iterables;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;

import java.util.Collection;
import java.util.List;

/**
 * Executes a delegete operation, but returns to another operation upon
 * completing the delegate.
 */
public class DelegateOperation implements Operation {

    private final Operation original;
    private Operation delegate;

    /**
     * Create a new operation delegate.
     *
     * @param original the operation to return to
     * @param delegate the delegate operation to complete before returning
     */
    public DelegateOperation(Operation original, Operation delegate) {
        checkNotNull(original);
        checkNotNull(delegate);
        this.original = original;
        this.delegate = delegate;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        delegate = delegate.resume(run);
        return delegate != null ? this : original;
    }

    @Override
    public void cancel() {
        delegate.cancel();
        original.cancel();
    }

    @Override
    public Iterable<Component> getStatusMessages() {
        return Iterables.concat(original.getStatusMessages(), delegate.getStatusMessages());
    }

}
