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

import com.sk89q.worldedit.util.nbt.BinaryTagIO;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class reads <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * streams, and produces an object graph of subclasses of the {@code Tag}
 * object.
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
public final class NBTInputStream implements Closeable {

    private final DataInputStream is;

    /**
     * Creates a new {@code NBTInputStream}, which will source its data
     * from the specified input stream.
     *
     * @param is the input stream
     */
    public NBTInputStream(InputStream is) {
        this.is = new DataInputStream(is);
    }

    /**
     * Reads an NBT tag from the stream.
     *
     * @return The tag that was read.
     * @throws IOException if an I/O error occurs.
     */
    public NamedTag readNamedTag() throws IOException {
        Map.Entry<String, CompoundBinaryTag> named = BinaryTagIO.reader().readNamed(
            (DataInput) this.is
        );
        return new NamedTag(named.getKey(), new CompoundTag(named.getValue()));
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

}
