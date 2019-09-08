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

import java.util.Locale;

/**
 * The {@code TAG_Byte_Array} tag.
 */
public final class ByteArrayTag extends Tag {

    private final byte[] value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ByteArrayTag(byte[] value) {
        super();
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (byte b : value) {
            String hexDigits = Integer.toHexString(b).toUpperCase(Locale.ROOT);
            if (hexDigits.length() == 1) {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        return "TAG_Byte_Array(" + hex + ")";
    }

}
