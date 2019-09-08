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

package com.sk89q.worldedit.registry.state;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

public abstract class AbstractProperty<T> implements Property<T> {

    private String name;
    private List<T> values;

    public AbstractProperty(final String name, final List<T> values) {
        this.name = name;
        this.values = values;
    }

    @Override
    public List<T> getValues() {
        return this.values;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Internal method for name setting post-deserialise. Do not use.
     */
    public void setName(final String name) {
        checkState(this.name == null, "name already set");
        this.name = name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name=" + name + "}";
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Property)) {
            return false;
        }
        return getName().equals(((Property<?>) obj).getName());
    }
}
