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

import com.sk89q.worldedit.util.nbt.ShortBinaryTag;

/**
 * The {@code TAG_Short} tag.
 *
 * @deprecated Use {@link ShortBinaryTag}.
 */
@Deprecated
public final class ShortTag extends Tag {

    private final ShortBinaryTag innerTag;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ShortTag(short value) {
        super();
        this.innerTag = ShortBinaryTag.of(value);
    }

    public ShortTag(ShortBinaryTag adventureTag) {
        super();
        this.innerTag = adventureTag;
    }

    @Override
    public ShortBinaryTag asBinaryTag() {
        return this.innerTag;
    }

    @Override
    public Short getValue() {
        return innerTag.value();
    }

}
