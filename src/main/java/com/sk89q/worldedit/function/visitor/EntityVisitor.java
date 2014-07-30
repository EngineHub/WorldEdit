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

package com.sk89q.worldedit.function.visitor;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.function.operation.AbstractOperation;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.util.AffectedCounter;
import com.sk89q.worldedit.util.task.progress.Progress;
import com.sk89q.worldedit.util.task.progress.ProgressIterator;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Visits entities as provided by an {@code Iterator}.
 */
public class EntityVisitor extends AbstractOperation implements AffectedCounter {

    private final ProgressIterator<? extends Entity> iterator;
    private final EntityFunction function;
    private int affected = 0;

    /**
     * Create a new instance.
     *
     * @param entities a list of entities
     * @param function the function
     */
    public EntityVisitor(List<? extends Entity> entities, EntityFunction function) {
        checkNotNull(entities);
        checkNotNull(function);
        this.iterator = ProgressIterator.create(entities);
        this.function = function;
    }

    @Override
    public int getAffected() {
        return affected;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (function.apply(entity)) {
                affected++;
            }

            if (!run.shouldContinue()) {
                return this;
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Progress getProgress() {
        return iterator.getProgress();
    }

}
