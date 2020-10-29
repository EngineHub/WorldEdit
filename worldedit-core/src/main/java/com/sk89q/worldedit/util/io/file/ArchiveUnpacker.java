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

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ArchiveUnpacker {

    private static final String UNPACK_FINISHED = ".unpack_finished";

    private static final Lock lock = new ReentrantLock();

    private final Path unpackDir;

    public ArchiveUnpacker(Path unpackDir) throws IOException {
        this.unpackDir = unpackDir;
        Files.createDirectories(unpackDir);
    }

    public Path unpackArchive(URL archiveUrl) throws IOException {
        String hash;
        try (InputStream data = archiveUrl.openStream()) {
            hash = ByteStreams.readBytes(data, new ByteProcessor<String>() {
                private final Hasher hasher = Hashing.crc32c().newHasher();

                @Override
                public boolean processBytes(byte[] buf, int off, int len) {
                    hasher.putBytes(buf, off, len);
                    return true;
                }

                @Override
                public String getResult() {
                    return hasher.hash().toString();
                }
            });
        }
        Path dest = unpackDir.resolve(hash);
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
                        Files.createDirectories(
                            resolved,
                            SafeFiles.getOwnerOnlyFileAttributes(AttributeTarget.DIRECTORY)
                        );
                    } else {
                        try (SeekableByteChannel channel = Files.newByteChannel(
                            resolved,
                            ImmutableSet.of(
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE,
                                StandardOpenOption.TRUNCATE_EXISTING
                            ),
                            SafeFiles.getOwnerOnlyFileAttributes(AttributeTarget.FILE)
                        )) {
                            ByteStreams.copy(
                                Channels.newChannel(zipReader),
                                channel
                            );
                        }
                    }
                }
            }
            Files.createFile(dest.resolve(UNPACK_FINISHED));
            return dest;
        } finally {
            lock.unlock();
        }
    }

}
