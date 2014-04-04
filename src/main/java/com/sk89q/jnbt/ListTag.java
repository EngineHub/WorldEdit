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

package com.sk89q.jnbt;

import java.util.Collections;
import java.util.List;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;

/**
 * The <code>TAG_List</code> tag.
 *
 * @author Graham Edgecombe
 *
 */
public final class ListTag extends Tag {

    /**
     * The type.
     */
    private final Class<? extends Tag> type;

    /**
     * The value.
     */
    private final List<Tag> value;

    /**
     * Creates the tag.
     *
     * @param name
     *            The name.
     * @param type
     *            The type of item in the list.
     * @param value
     *            The value.
     */
    public ListTag(String name, Class<? extends Tag> type, List<? extends Tag> value) {
        super(name);
        this.type = type;
        this.value = Collections.unmodifiableList(value);
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    public Class<? extends Tag> getType() {
        return type;
    }

    @Override
    public List<Tag> getValue() {
        return value;
    }

    @Override
    public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_List" + append + ": " + value.size()
                + " entries of type " + NBTUtils.getTypeName(type)
                + "\r\n{\r\n");
        for (Tag t : value) {
            bldr.append("   " + t.toString().replaceAll("\r\n", "\r\n   ")
                    + "\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

}
