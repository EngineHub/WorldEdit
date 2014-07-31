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

package com.sk89q.worldedit.util.io;

import java.io.IOException;
import java.io.InputStream;

public class ForwardSeekableInputStream extends InputStream {

    protected InputStream parent;
    protected long position = 0;

    public ForwardSeekableInputStream(InputStream parent) {
        this.parent = parent;
    }

    @Override
    public int read() throws IOException {
        int ret = parent.read();
        ++position;
        return ret;
    }

    @Override
    public int available() throws IOException {
        return parent.available();
    }

    @Override
    public void close() throws IOException {
        parent.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        parent.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return parent.markSupported();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        position += read;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = parent.read(b);
        position += read;
        return read;
    }

    @Override
    public synchronized void reset() throws IOException {
        parent.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = parent.skip(n);
        position += skipped;
        return skipped;
    }

    public void seek(long n) throws IOException {
        long diff = n - position;

        if (diff < 0) {
            throw new IOException("Can't seek backwards");
        }

        if (diff == 0) {
            return;
        }

        if (skip(diff) < diff) {
            throw new IOException("Failed to seek " + diff + " bytes");
        }
    }
}
