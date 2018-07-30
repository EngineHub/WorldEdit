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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;

import java.util.Iterator;
import java.util.List;

/**
 * Visits entities as provided by an {@code Iterator}.
 */
public class EntityVisitor implements Operation {

    private final Iterator<? extends Entity> iterator;
    private final EntityFunction function;
    private int affected = 0;

    /**
     * Create a new instance.
     *
     * @param iterator the iterator
     * @param function the function
     */
    public EntityVisitor(Iterator<? extends Entity> iterator, EntityFunction function) {
        checkNotNull(iterator);
        checkNotNull(function);
        this.iterator = iterator;
        this.function = function;
    }

    /**
     * Get the number of affected objects.
     *
     * @return the number of affected
     */
    public int getAffected() {
        return affected;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        while (iterator.hasNext()) {
            if (function.apply(iterator.next())) {
                affected++;
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addStatusMessages(List<String> messages) {
        messages.add(getAffected() + " entities affected");
    }

}
