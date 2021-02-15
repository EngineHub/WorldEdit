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

import com.google.common.collect.Maps;
import com.sk89q.worldedit.util.nbt.BinaryTagIO;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class writes <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * {@code Tag} objects to an underlying {@code OutputStream}.
 *
 * <p>
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="https://minecraft.gamepedia.com/NBT_format">
 * https://minecraft.gamepedia.com/NBT_format</a>.
 * </p>
 *
 * @deprecated JNBT is being removed for adventure-nbt in WorldEdit 8.
 */
@Deprecated
public final class NBTOutputStream implements Closeable {

    /**
     * The output stream.
     */
    private final DataOutputStream os;

    /**
     * Creates a new {@code NBTOutputStream}, which will write data to the
     * specified underlying output stream.
     *
     * @param os
     *            The output stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTOutputStream(OutputStream os) throws IOException {
        this.os = new DataOutputStream(os);
    }

    /**
     * Writes a tag.
     *
     * @param tag
     *            The tag to write.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeNamedTag(String name, Tag tag) throws IOException {
        BinaryTagIO.writer().writeNamed(
            Maps.immutableEntry(name, (CompoundBinaryTag) tag.asBinaryTag()),
            (DataOutput) this.os
        );
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

}
