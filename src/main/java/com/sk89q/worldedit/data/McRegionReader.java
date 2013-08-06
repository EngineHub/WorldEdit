// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

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

package com.sk89q.worldedit.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.sk89q.worldedit.Vector2D;

/**
 * Reader for a MCRegion file. This reader works on input streams, meaning
 * that it can be used to read files from non-file based sources.
 *
 * @author sk89q
 */
public class McRegionReader {

    protected static final int VERSION_GZIP = 1;
    protected static final int VERSION_DEFLATE = 2;
    protected static final int SECTOR_BYTES = 4096;
    protected static final int SECTOR_INTS = SECTOR_BYTES / 4;
    public static final int CHUNK_HEADER_SIZE = 5;

    protected ForwardSeekableInputStream stream;
    protected DataInputStream dataStream;

    protected int offsets[];

    /**
     * Construct the reader.
     * 
     * @param stream
     * @throws DataException
     * @throws IOException
     */
    public McRegionReader(InputStream stream) throws DataException, IOException {
        this.stream = new ForwardSeekableInputStream(stream);
        this.dataStream = new DataInputStream(this.stream);

        readHeader();
    }

    /**
     * Read the header.
     * 
     * @throws DataException
     * @throws IOException
     */
    private void readHeader() throws DataException, IOException {
        offsets = new int[SECTOR_INTS];

        for (int i = 0; i < SECTOR_INTS; ++i) {
            int offset = dataStream.readInt();
            offsets[i] = offset;
        }
    }

    /**
     * Gets the uncompressed data input stream for a chunk.
     * 
     * @param pos
     * @return
     * @throws IOException
     * @throws DataException
     */
    public synchronized InputStream getChunkInputStream(Vector2D pos)
            throws IOException, DataException {

        int x = pos.getBlockX() & 31;
        int z = pos.getBlockZ() & 31;

        if (x < 0 || x >= 32 || z < 0 || z >= 32) {
            throw new DataException("MCRegion file does not contain " + x + "," + z);
        }

        int offset = getOffset(x, z);

        // The chunk hasn't been generated
        if (offset == 0) {
            throw new DataException("The chunk at " + x + "," + z + " is not generated");
        }

        int sectorNumber = offset >> 8;
        int numSectors = offset & 0xFF;

        stream.seek(sectorNumber * SECTOR_BYTES);
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
     * @param x
     * @param z
     * @return
     */
    private int getOffset(int x, int z) {
        return offsets[x + z * 32];
    }

    /**
     * Returns whether the file contains a chunk.
     * 
     * @param x
     * @param z
     * @return
     */
    public boolean hasChunk(int x, int z) {
        return getOffset(x, z) != 0;
    }

    /**
     * Close the stream.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        stream.close();
    }
}
