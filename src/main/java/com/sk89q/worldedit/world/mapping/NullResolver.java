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

package com.sk89q.worldedit.world.mapping;

import javax.annotation.Nullable;

/**
 * An implementation of a {@link Resolver} that knows nothing and returns
 * {@code null} in all cases.
 *
 * @param <E> the object to resolve
 */
public class NullResolver<E> implements Resolver<E> {

    @Nullable
    @Override
    public E create(String id) {
        return null;
    }

    @Nullable
    @Override
    public String getId(E object) {
        return null;
    }

}
