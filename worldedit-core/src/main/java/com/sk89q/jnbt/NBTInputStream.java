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

import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinRootEntry;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
 * @deprecated JNBT is being removed for lin-bus in WorldEdit 8, use {@link LinBinaryIO} instead
 */
@Deprecated
public final class NBTInputStream implements Closeable {

    final DataInputStream is;

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
     */
    public NamedTag readNamedTag() throws IOException {
        LinRootEntry named = LinBinaryIO.readUsing(is, LinRootEntry::readFrom);
        return new NamedTag(named.name(), new CompoundTag(named.value()));
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

}
