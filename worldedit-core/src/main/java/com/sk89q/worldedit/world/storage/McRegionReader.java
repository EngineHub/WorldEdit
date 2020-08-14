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

// $Id$
/*

 Region File Format

 Concept: The minimum unit of storage on hard drives is 4KB. 90% of Minecraft
 chunks are smaller than 4KB. 99% are smaller than 8KB. Write a simple
 container to store chunks in single files in runs of 4KB sectors.

 Each region file represents a 32x32 group of chunks. The conversion from
 chunk number to region number is floor(coord / 32): a chunk at (30, -3)
 would be in region (0, -1), and one at (70, -30) would be at (3, -1).
 Region files are named "r.x.z.data", where x and z are the region coordinates.

 A region file begins with a 4KB header that describes where chunks are stored
 in the file. A 4-byte big-endian integer represents sector offsets and sector
 counts. The chunk offset for a chunk (x, z) begins at byte 4*(x+z*32) in the
 file. The bottom byte of the chunk offset indicates the number of sectors the
 chunk takes up, and the top 3 bytes represent the sector number of the chunk.
 Given a chunk offset o, the chunk data begins at byte 4096*(o/256) and takes up
 at most 4096*(o%256) bytes. A chunk cannot exceed 1MB in size. If a chunk
 offset is 0, the corresponding chunk is not stored in the region file.

 Chunk data begins with a 4-byte big-endian integer representing the chunk data
 length in bytes, not counting the length field. The length must be smaller than
 4096 times the number of sectors. The next byte is a version field, to allow
 backwards-compatible updates to how chunks are encoded.

 A version of 1 represents a gzipped NBT file. The gzipped data is the chunk
 length - 1.

 A version of 2 represents a deflated (zlib compressed) NBT file. The deflated
 data is the chunk length - 1.

 */

package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.util.io.ForwardSeekableInputStream;
import com.sk89q.worldedit.world.DataException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Reader for a MCRegion file. This reader works on input streams, meaning
 * that it can be used to read files from non-file based sources.
 */
public class McRegionReader {

    protected static final int VERSION_GZIP = 1;
    protected static final int VERSION_DEFLATE = 2;
    protected static final int SECTOR_BYTES = 4096;
    protected static final int SECTOR_INTS = SECTOR_BYTES / 4;
    public static final int CHUNK_HEADER_SIZE = 5;

    protected ForwardSeekableInputStream stream;
    protected DataInputStream dataStream;

    protected int[] offsets;

    /**
     * Construct the reader.
     *
     * @param stream the stream
     * @throws IOException if there is an error getting the region data
     */
    public McRegionReader(InputStream stream) throws IOException {
        this.stream = new ForwardSeekableInputStream(stream);
        this.dataStream = new DataInputStream(this.stream);

        readHeader();
    }

    /**
     * Read the header.
     *
     * @throws IOException if there is an error getting the header data
     */
    private void readHeader() throws IOException {
        offsets = new int[SECTOR_INTS];

        for (int i = 0; i < SECTOR_INTS; ++i) {
            int offset = dataStream.readInt();
            offsets[i] = offset;
        }
    }

    /**
     * Gets the uncompressed data input stream for a chunk.
     *
     * @param position chunk position
     * @return an input stream
     * @throws IOException if there is an error getting the chunk data
     * @throws DataException if there is an error getting the chunk data
     */
    public synchronized InputStream getChunkInputStream(BlockVector2 position) throws IOException, DataException {
        int x = position.getBlockX() & 31;
        int z = position.getBlockZ() & 31;

        int offset = getOffset(x, z);

        // The chunk hasn't been generated
        if (offset == 0) {
            throw new DataException("The chunk at " + x + "," + z + " is not generated");
        }

        int sectorNumber = offset >> 8;
        int numSectors = offset & 0xFF;

        stream.seek((long) sectorNumber * SECTOR_BYTES);
        int length = dataStream.readInt();

        if (length > SECTOR_BYTES * numSectors) {
            throw new DataException("MCRegion chunk at "
                    + x + "," + z + " has an invalid length of " + length);
        }

        byte version = dataStream.readByte();

        if (version == VERSION_GZIP) {
            byte[] data = new byte[length - 1];
            if (dataStream.read(data) < length - 1) {
                throw new DataException("MCRegion file does not contain "
                        + x + "," + z + " in full");
            }
            return new GZIPInputStream(new ByteArrayInputStream(data));
        } else if (version == VERSION_DEFLATE) {
            byte[] data = new byte[length - 1];
            if (dataStream.read(data) < length - 1) {
                throw new DataException("MCRegion file does not contain "
                        + x + "," + z + " in full");
            }
            return new InflaterInputStream(new ByteArrayInputStream(data));
        } else {
            throw new DataException("MCRegion chunk at "
                    + x + "," + z + " has an unsupported version of " + version);
        }
    }

    /**
     * Get the offset for a chunk. May return 0 if it doesn't exist.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @return the offset
     */
    private int getOffset(int x, int z) {
        return offsets[x + z * 32];
    }

    /**
     * Returns whether the file contains a chunk.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @return the offset
     */
    public boolean hasChunk(int x, int z) {
        return getOffset(x, z) != 0;
    }

    /**
     * Close the stream.
     */
    public void close() throws IOException {
        stream.close();
    }
}
