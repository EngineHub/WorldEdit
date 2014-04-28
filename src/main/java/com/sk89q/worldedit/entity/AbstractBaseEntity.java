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

package com.sk89q.worldedit.entity;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.world.DataException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract implementation of {@link BaseEntity} that implementations can
 * subclass to simplify implementation.
 */
public abstract class AbstractBaseEntity implements BaseEntity {

    private CompoundTag nbtData;

    @Override
    public boolean hasNbtData() {
        return getNbtData() != null;
    }

    @Override
    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public void setNbtData(CompoundTag nbtData) throws DataException {
        checkNotNull(nbtData);
        this.nbtData = nbtData;
    }

}
