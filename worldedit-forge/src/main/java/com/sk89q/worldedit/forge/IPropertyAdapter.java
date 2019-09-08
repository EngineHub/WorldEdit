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

package com.sk89q.worldedit.forge;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.registry.state.Property;
import net.minecraft.state.IProperty;

import java.util.List;
import java.util.Optional;

class IPropertyAdapter<T extends Comparable<T>> implements Property<T> {

    private final IProperty<T> property;
    private final List<T> values;

    public IPropertyAdapter(IProperty<T> property) {
        this.property = property;
        this.values = ImmutableList.copyOf(property.getAllowedValues());
    }

    @Override
    public String getName() {
        return property.getName();
    }

    @Override
    public List<T> getValues() {
        return values;
    }

    @Override
    public T getValueFor(String string) throws IllegalArgumentException {
        Optional<T> val = property.parseValue(string);
        checkArgument(val.isPresent(), "%s has no value for %s", getName(), string);
        return val.get();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Property)) {
            return false;
        }
        return getName().equals(((Property<?>) obj).getName());
    }

}
