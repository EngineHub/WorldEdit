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
import com.sk89q.worldedit.util.task.progress.Progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Interleaves two or more operations so they run sequentially in the same
 * pass, giving more preference to the first-most operations.
 */
public class UnfairOperationInterleave extends AbstractOperation {

    private final List<Operation> operations = new ArrayList<Operation>();
    private final Queue<Operation> queue = new LinkedBlockingQueue<Operation>();

    /**
     * Create a new instance.
     *
     * @param operations a collection of operations to interleave
     */
    public UnfairOperationInterleave(Collection<Operation> operations) {
        checkNotNull(operations);
        operations.addAll(operations);
        queue.addAll(operations);
    }

    /**
     * Create a new instance.
     *
     * @param operation an array of operations to interleave
     */
    public UnfairOperationInterleave(Operation... operation) {
        this(Arrays.asList(checkNotNull(operation)));
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        Operation next = queue.poll();
        if (next != null) {
            next = next.resume(run);
            if (next != null) {
                queue.offer(next);
            }

            boolean onlyOpportunistic = true;
            for (Operation operation : queue) {
                if (!operation.isOpportunistic()) {
                    onlyOpportunistic = false;
                }
            }

            if (onlyOpportunistic) {
                return null;
            }

            return this;
        } else {
            return null;
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public Progress getProgress() {
        return Progress.splitObservables(operations);
    }

}
