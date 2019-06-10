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

package com.sk89q.worldedit.internal.anvil;

import com.sk89q.worldedit.math.BlockVector2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * Internal class. Subject to changes.
 */
class RegionAccess implements AutoCloseable {

    private RandomAccessFile raf;

    RegionAccess(Path file) throws IOException {
        raf = new RandomAccessFile(file.toFile(), "rw");
    }

    int getModificationTime(BlockVector2 pos) throws IOException {
        int x = pos.getBlockX() & 31;
        int z = pos.getBlockZ() & 31;
        raf.seek((x + z * 32) * 4 + 4096);
        return raf.readInt();
    }

    int getChunkSize(BlockVector2 pos) throws IOException {
        int x = pos.getBlockX() & 31;
        int z = pos.getBlockZ() & 31;
        raf.seek((x + z * 32) * 4);
        // 3 bytes for offset
        raf.read();
        raf.read();
        raf.read();
        // one byte for size
        return raf.read();
    }

    void deleteChunk(BlockVector2 pos) throws IOException {
        int x = pos.getBlockX() & 31;
        int z = pos.getBlockZ() & 31;
        raf.seek((x + z * 32) * 4);
        raf.writeInt(0);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
