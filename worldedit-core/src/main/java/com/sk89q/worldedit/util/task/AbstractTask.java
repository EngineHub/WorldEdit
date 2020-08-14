/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.task;

import com.google.common.util.concurrent.AbstractFuture;

import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract task that stores a name and owner.
 *
 * @param <V> the type returned
 */
public abstract class AbstractTask<V> extends AbstractFuture<V> implements Task<V> {

    private final UUID uniqueId = UUID.randomUUID();
    private final String name;
    private final Object owner;
    private final Date creationDate = new Date();

    /**
     * Create a new instance.
     *
     * @param name the name
     * @param owner the owner
     */
    protected AbstractTask(String name, @Nullable Object owner) {
        checkNotNull(name);
        this.name = name;
        this.owner = owner;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Object getOwner() {
        return owner;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }
}
