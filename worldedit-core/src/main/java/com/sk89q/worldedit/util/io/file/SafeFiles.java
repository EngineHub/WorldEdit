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

import com.google.common.collect.Streams;
import com.sk89q.worldedit.util.collection.MoreSets;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
     * Resolve {@code path} against {@code dir}.
     *
     * <p>
     * If file types are provided and the path has an extension (content after {@code '.'},
     * {@code "file."} has no extension), then the extension must match one of the file type's
     * extensions.
     * </p>
     *
     * <p>
     * If file types are provided and the path has no extension, then the default or first file
     * type's primary extension will be added.
     * </p>
     *
     * <p>
     * If no file types are provided, any path will be used without modification.
     * </p>
     *
     * <p>
     * If the result of {@code dir.resolve(path)} lies outside of {@code dir}
     * <strong>WITHOUT</strong> resolving symlinks, then it is rejected. Symlinks that cause the
     * resulting filename to reside outside of {@code dir} are <strong>NOT</strong> considered.
     * </p>
     *
     * <p>
     * Note: this method actually resolves {@code "./" + path} against dir, meaning it is safe to
     * pass it absolute paths. This should always result in a relative path, but in rare cases may
     * result in an exception rejecting an absolute path.
     * </p>
     *
     * @param dir the directory to resolve against
     * @param path the path to use
     * @param defaultFileType the default file type to use if no extension is provided
     * @param fileTypes the other file types to accept (may contain default)
     * @return the resolved path
     * @throws InvalidFilenameException if there is a problem with the filename
     */
    public static Path resolveSafePathWithFileType(Path dir,
                                                   String path,
                                                   @Nullable FileType defaultFileType,
                                                   Set<FileType> fileTypes) throws InvalidFilenameException {
        if (path.isEmpty()) {
            throw new InvalidFilenameException(path, TranslatableComponent.of("worldedit.error.invalid-filename.empty"));
        }

        // Canonicalize
        fileTypes = MoreSets.ensureFirst(defaultFileType, fileTypes);

        String extension = getFileExtension(path);
        if (extension == null) {
            return resolveSafePath(
                dir, path + "." + fileTypes.iterator().next().getPrimaryExtension()
            );
        }
        // if not accepting all (empty) AND extension rejected, fail
        if (!fileTypes.isEmpty() && fileTypes.stream()
            .noneMatch(ft -> ft.getExtensions().contains(extension))) {
            throw new InvalidFilenameException(path, TranslatableComponent.of("worldedit.error.invalid-filename.bad.extension"));
        }
        return resolveSafePath(dir, path);
    }

    private static Path resolveSafePath(Path dir, String path) throws InvalidFilenameException {
        Path relative;
        try {
            // Force a relative path via string concat
            relative = Paths.get("./" + path);
        } catch (InvalidPathException e) {
            throw new InvalidFilenameException(path, TranslatableComponent.of("worldedit.error.invalid-filename.invalid-characters"));
        }
        // paranoid
        if (relative.isAbsolute()) {
            throw new InvalidFilenameException(path, TranslatableComponent.of("worldedit.error.invalid-filename.absolute"));
        }
        // Protect against '..'
        if (Streams.stream(relative).anyMatch(it -> it.toString().equals(".."))) {
            throw new InvalidFilenameException(path, TranslatableComponent.of("worldedit.error.invalid-filename.invalid-characters"));
        }
        // Everything else should be legal, any directory escapes were introduced by the server
        // administrator (WorldEdit does not create symlinks), so they are intended
        return dir.resolve(relative);
    }

    @Nullable
    private static String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > 0 && dot != filename.length() - 1) ? filename.substring(dot + 1) : null;
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

    private SafeFiles() {
    }
}
