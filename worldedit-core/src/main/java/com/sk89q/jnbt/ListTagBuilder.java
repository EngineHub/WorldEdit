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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Helps create list tags.
 */
public class ListTagBuilder {

    private final Class<? extends Tag> type;
    private final List<Tag> entries;

    /**
     * Create a new instance.
     *
     * @param type of tag contained in this list
     */
    ListTagBuilder(Class<? extends Tag> type) {
        checkNotNull(type);
        this.type = type;
        this.entries = new ArrayList<>();
    }

    /**
     * Add the given tag.
     *
     * @param value the tag
     * @return this object
     */
    public ListTagBuilder add(Tag value) {
        checkNotNull(value);
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(value.getClass().getCanonicalName() + " is not of expected type " + type.getCanonicalName());
        }
        entries.add(value);
        return this;
    }

    /**
     * Add all the tags in the given list.
     *
     * @param value a list of tags
     * @return this object
     */
    public ListTagBuilder addAll(Collection<? extends Tag> value) {
        checkNotNull(value);
        for (Tag v : value) {
            add(v);
        }
        return this;
    }

    /**
     * Build an unnamed list tag with this builder's entries.
     *
     * @return the new list tag
     */
    public ListTag build() {
        return new ListTag(type, new ArrayList<>(entries));
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static ListTagBuilder create(Class<? extends Tag> type) {
        return new ListTagBuilder(type);
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static ListTagBuilder createWith(Tag... entries) {
        checkNotNull(entries);

        if (entries.length == 0) {
            throw new IllegalArgumentException("This method needs an array of at least one entry");
        }

        Class<? extends Tag> type = entries[0].getClass();
        for (int i = 1; i < entries.length; i++) {
            if (!type.isInstance(entries[i])) {
                throw new IllegalArgumentException("An array of different tag types was provided");
            }
        }

        ListTagBuilder builder = new ListTagBuilder(type);
        builder.addAll(Arrays.asList(entries));
        return builder;
    }

}
