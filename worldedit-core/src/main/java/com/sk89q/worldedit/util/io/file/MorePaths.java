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

package com.sk89q.worldedit.util.io.file;

import com.google.common.collect.Streams;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class MorePaths {

    /**
     * Starting with the first path element, add elements until reaching this path.
     */
    public static Stream<Path> iterPaths(Path path) {
        return IntStream.range(1, path.getNameCount() + 1)
            .mapToObj(end -> {
                Path subPath = path.subpath(0, end);
                if (path.isAbsolute()) {
                    subPath = subPath.getFileSystem().getPath("/").resolve(subPath);
                }
                return subPath;
            })
            .filter(p -> !p.toString().isEmpty());
    }

    /**
     * Create an efficiently-splittable spliterator for the given path elements.
     *
     * <p>
     * Since paths are so small, this is only useful for preventing heavy computations
     * on later parts of the stream from occurring when using
     * {@link Streams#findLast(IntStream)}, and not for parallelism.
     * </p>
     *
     * @param path the path to create a spliterator for
     * @return the spliterator
     */
    public static Spliterator<Path> optimizedSpliterator(Path path) {
        return Arrays.spliterator(Streams.stream(path).toArray(Path[]::new));
    }

    /**
     * Convert the path to a URI. Unlike {@link Path#toUri()}, does not convert the path to an
     * absolute path.
     *
     * @param path the path to convert
     * @return the URI
     */
    public static URI toRelativeUri(Path path) {
        if (path.isAbsolute()) {
            return path.toUri();
        }
        // Probably has some issues with non-default FS paths
        checkArgument(path.getFileSystem() == FileSystems.getDefault(),
            "Non-default path support not implemented");
        URI base = Paths.get("").toUri();
        URI extended = path.toUri();
        // Construct relative URI by removing the base URI from the path URI
        // Pretend the host of this file is "relative" -- so the full path is the URI path
        return URI.create("file://relative/" + extended.toString().substring(base.toString().length()));
    }

    private MorePaths() {
    }
}
