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

import java.util.List;

import javax.annotation.Nullable;

public class EnumProperty extends AbstractProperty<String> {

    public EnumProperty(final String name, final List<String> values) {
        super(name, values);
    }

    @Nullable
    @Override
    public String getValueFor(String string) {
        if (!getValues().contains(string)) {
            throw new IllegalArgumentException("Invalid value: " + string + ". Must be in " + getValues().toString());
        }
        return string;
    }
}
