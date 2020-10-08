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

package com.sk89q.worldedit.util.io.file;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveUnpacker {

    private static final String UNPACK_FINISHED = ".unpack_finished";
    private static final Path TEMP_DIR;

    static {
        try {
            TEMP_DIR = Paths.get(
                System.getProperty("java.io.tmpdir"),
                "worldedit-unpack-dir-for-" + System.getProperty("user.name")
            );
            Files.createDirectories(TEMP_DIR);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final Lock lock = new ReentrantLock();

    public static Path unpackArchive(URL archiveUrl) throws IOException {
        String hash;
        try (InputStream data = archiveUrl.openStream()) {
            hash = ByteStreams.readBytes(data, new ByteProcessor<String>() {
                private final Hasher hasher = Hashing.crc32c().newHasher();

                @Override
                public boolean processBytes(byte[] buf, int off, int len) throws IOException {
                    hasher.putBytes(buf, off, len);
                    return true;
                }

                @Override
                public String getResult() {
                    return hasher.hash().toString();
                }
            });
        }
        Path dest = TEMP_DIR.resolve(hash);
        if (Files.exists(dest.resolve(UNPACK_FINISHED))) {
            // trust this, no other option :)
            return dest;
        }
        lock.lock();
        try {
            // check again after exclusive acquire
            if (Files.exists(dest.resolve(UNPACK_FINISHED))) {
                return dest;
            }
            try (InputStream in = archiveUrl.openStream();
                 ZipInputStream zipReader = new ZipInputStream(in)) {
                ZipEntry next;
                while ((next = zipReader.getNextEntry()) != null) {
                    Path resolved = dest.resolve(next.getName());
                    if (!resolved.startsWith(dest)) {
                        // bad entry
                        continue;
                    }
                    if (next.isDirectory()) {
                        Files.createDirectories(resolved);
                    } else {
                        Files.copy(zipReader, resolved, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            Files.createFile(dest.resolve(UNPACK_FINISHED));
            return dest;
        } finally {
            lock.unlock();
        }
    }

    private ArchiveUnpacker() {
    }

}
