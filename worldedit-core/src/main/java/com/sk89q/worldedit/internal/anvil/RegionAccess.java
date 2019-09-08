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
    private int[] offsets;
    private int[] timestamps;

    RegionAccess(Path file) throws IOException {
        this(file, false);
    }

    RegionAccess(Path file, boolean preload) throws IOException {
        raf = new RandomAccessFile(file.toFile(), "rw");
        if (preload) {
            readHeaders();
        }
    }

    private void readHeaders() throws IOException {
        offsets = new int[1024];
        timestamps = new int[1024];
        for (int idx = 0; idx < 1024; ++idx) {
            offsets[idx] = raf.readInt();
        }
        for (int idx = 0; idx < 1024; ++idx) {
            timestamps[idx] = raf.readInt();
        }
    }

    private static int indexChunk(BlockVector2 pos) {
        int x = pos.getBlockX() & 31;
        int z = pos.getBlockZ() & 31;
        return x + z * 32;
    }

    int getModificationTime(BlockVector2 pos) throws IOException {
        int idx = indexChunk(pos);
        if (timestamps != null) {
            return timestamps[idx];
        }
        raf.seek(idx * 4L + 4096);
        return raf.readInt();
    }

    int getChunkSize(BlockVector2 pos) throws IOException {
        int idx = indexChunk(pos);
        if (offsets != null) {
            return offsets[idx] & 0xFF;
        }
        raf.seek(idx * 4L);
        // 3 bytes for offset
        raf.read();
        raf.read();
        raf.read();
        // one byte for size - note, yes, could do raf.readInt() & 0xFF but that does extra checks
        return raf.read();
    }

    void deleteChunk(BlockVector2 pos) throws IOException {
        int idx = indexChunk(pos);
        raf.seek(idx * 4L);
        raf.writeInt(0);
        if (offsets != null) {
            offsets[idx] = 0;
        }
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
