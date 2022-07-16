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

package com.sk89q.jnbt;

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps create list tags.
 *
 * @deprecated Use {@link LinListTag.Builder}.
 */
@Deprecated
public class ListTagBuilder<V, LT extends LinTag<V>> {

    private final LinListTag.Builder<LT> builder;

    /**
     * Create a new instance.
     *
     * @param type of tag contained in this list
     */
    @SuppressWarnings("unchecked")
    ListTagBuilder(Class<? extends Tag<V, LT>> type) {
        checkNotNull(type);
        this.builder = (LinListTag.Builder<LT>) LinListTag.builder(LinTagType.fromId(LinTagId.fromId(
            NBTUtils.getTypeCode(type)
        )));
    }

    /**
     * Add the given tag.
     *
     * @param value the tag
     * @return this object
     */
    public ListTagBuilder<V, LT> add(Tag<V, LT> value) {
        checkNotNull(value);
        builder.add(value.toLinTag());
        return this;
    }

    /**
     * Add all the tags in the given list.
     *
     * @param value a list of tags
     * @return this object
     */
    public ListTagBuilder<V, LT> addAll(Collection<? extends Tag<V, LT>> value) {
        checkNotNull(value);
        for (Tag<V, LT> v : value) {
            add(v);
        }
        return this;
    }

    /**
     * Build an unnamed list tag with this builder's entries.
     *
     * @return the new list tag
     */
    public ListTag<V, LT> build() {
        return new ListTag<>(this.builder.build());
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static <V, LT extends LinTag<V>> ListTagBuilder<V, LT> create(Class<? extends Tag<V, LT>> type) {
        return new ListTagBuilder<>(type);
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    @SafeVarargs
    public static <V, LT extends LinTag<V>> ListTagBuilder<V, LT> createWith(Tag<V, LT>... entries) {
        checkNotNull(entries);

        if (entries.length == 0) {
            throw new IllegalArgumentException("This method needs an array of at least one entry");
        }

        @SuppressWarnings("unchecked")
        Class<? extends Tag<V, LT>> type = (Class<? extends Tag<V, LT>>) entries[0].getClass();
        ListTagBuilder<V, LT> builder = new ListTagBuilder<>(type);
        for (Tag<V, LT> entry : entries) {
            if (!type.isInstance(entry)) {
                throw new IllegalArgumentException("An array of different tag types was provided");
            }
            builder.add(entry);
        }

        return builder;
    }

}
