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

import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.ToLinTag;

import javax.annotation.Nonnull;

/**
 * Represents a NBT tag.
 *
 * @deprecated JNBT is being removed for lin-bus in WorldEdit 8, use {@link LinTag} instead
 */
@Deprecated
public abstract class Tag<V, LT extends LinTag<? extends V>> implements ToLinTag<LT> {
    protected final LT linTag;

    protected Tag(LT linTag) {
        this.linTag = linTag;
    }

    /**
     * Gets the value of this tag.
     *
     * @return the value
     */
    public V getValue() {
        return linTag.value();
    }

    @Override
    public String toString() {
        return toLinTag().toString();
    }

    @Override
    @Nonnull
    public LT toLinTag() {
        return linTag;
    }
}
