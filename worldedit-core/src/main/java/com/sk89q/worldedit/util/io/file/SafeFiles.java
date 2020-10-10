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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class SafeFiles {

    /**
     * A version of {@link Files#list(Path)} that won't leak resources.
     *
     * <p>
     * Instead, it immediately consumes the entire listing into a {@link List} and
     * calls {@link List#stream()}.
     * </p>
     *
     * @param dir the directory to list
     * @return an I/O-resource-free stream of the files in the directory
     * @throws IOException if an I/O error occurs
     */
    public static Stream<Path> noLeakFileList(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.collect(Collectors.toList()).stream();
        }
    }

    /**
     * {@link Path#getFileName()} includes a slash sometimes for some reason.
     * This will get rid of it.
     *
     * @param path the path to get the file name for
     * @return the file name of the given path
     */
    public static String canonicalFileName(Path path) {
        return dropSlash(path.getFileName().toString());
    }

    private static String dropSlash(String name) {
        if (name.isEmpty() || name.codePointBefore(name.length()) != '/') {
            return name;
        }
        return name.substring(0, name.length() - 1);
    }

    /**
     * Recursively uses {@link #tryHardToDelete(Path)} to cleanup directories before deleting them.
     *
     * @param directory the directory to delete
     * @throws IOException if an error occurs trying to delete the directory
     */
    public static void tryHardToDeleteDir(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            if (!Files.exists(directory)) {
                return;
            }

            throw new IOException(directory + " is not a directory");
        }
        try (Stream<Path> files = Files.list(directory)) {
            for (Iterator<Path> iter = files.iterator(); iter.hasNext(); ) {
                Path next = iter.next();
                if (Files.isDirectory(next)) {
                    tryHardToDeleteDir(next);
                } else {
                    tryHardToDelete(next);
                }
            }
        }
        tryHardToDelete(directory);
    }

    /**
     * Tries to delete a path. If it fails the first time, uses an implementation detail to try
     * and make it possible to delete the path, and then tries again. If that fails, throws an
     * {@link IOException} with both errors.
     *
     * @param path the path to delete
     * @throws IOException if the path could not be deleted after multiple attempts
     */
    public static void tryHardToDelete(Path path) throws IOException {
        IOException suppressed = tryDelete(path);
        if (suppressed == null) {
            return;
        }

        // This is copied from Ant (see org.apache.tools.ant.util.FileUtils.tryHardToDelete).
        // It mentions that there is a bug in the Windows JDK implementations that this is a valid
        // workaround for. I've been unable to find a definitive reference to this bug.
        // The thinking is that if this is good enough for Ant, it's good enough for us.
        System.gc();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        IOException suppressed2 = tryDelete(path);
        if (suppressed2 == null) {
            return;
        }
        IOException ex = new IOException("Failed to delete " + path, suppressed2);
        ex.addSuppressed(suppressed);
        throw ex;
    }

    @Nullable
    private static IOException tryDelete(Path path) {
        try {
            Files.deleteIfExists(path);
            if (Files.exists(path)) {
                return new IOException(path + " still exists after deleting");
            }
            return null;
        } catch (IOException e) {
            return e;
        }
    }

    private static final FileAttribute<?>[] OWNER_ONLY_FILE_ATTRS;
    private static final FileAttribute<?>[] OWNER_ONLY_DIR_ATTRS;

    static {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            OWNER_ONLY_FILE_ATTRS = new FileAttribute<?>[] {
                PosixFilePermissions.asFileAttribute(
                    ImmutableSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                    )
                )
            };
            OWNER_ONLY_DIR_ATTRS = new FileAttribute<?>[] {
                PosixFilePermissions.asFileAttribute(
                    ImmutableSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE
                    )
                )
            };
        } else {
            OWNER_ONLY_FILE_ATTRS = new FileAttribute<?>[0];
            OWNER_ONLY_DIR_ATTRS = new FileAttribute<?>[0];
        }
    }

    /**
     * Get a set of file attributes for file creation with owner-only access, if possible.
     *
     * <p>
     * On POSIX, this returns o+rw (and o+x if directory), on Windows it returns nothing.
     * </p>
     *
     * @return the owner-only file attributes
     */
    public static FileAttribute<?>[] getOwnerOnlyFileAttributes(AttributeTarget attributeTarget) {
        switch (attributeTarget) {
            case FILE:
                return OWNER_ONLY_FILE_ATTRS;
            case DIRECTORY:
                return OWNER_ONLY_DIR_ATTRS;
            default:
                throw new IllegalStateException("Unknown attribute target " + attributeTarget);
        }
    }

    private SafeFiles() {
    }
}
