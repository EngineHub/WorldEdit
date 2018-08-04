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

package com.sk89q.worldedit.history.change;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.util.Location;

/**
 * Logs the creation of an entity and removes the entity upon undo.
 */
public class EntityCreate implements Change {

    private final Location location;
    private final BaseEntity state;
    private Entity entity;

    /**
     * Create a new instance.
     *
     * @param location the location
     * @param state the state of the created entity
     * @param entity the entity that was created
     */
    public EntityCreate(Location location, BaseEntity state, Entity entity) {
        checkNotNull(location);
        checkNotNull(state);
        checkNotNull(entity);
        this.location = location;
        this.state = state;
        this.entity = entity;
    }

    @Override
    public void undo(UndoContext context) throws WorldEditException {
        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }

    @Override
    public void redo(UndoContext context) throws WorldEditException {
        entity = checkNotNull(context.getExtent()).createEntity(location, state);
    }

}
