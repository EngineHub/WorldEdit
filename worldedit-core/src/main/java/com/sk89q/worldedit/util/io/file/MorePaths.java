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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Spliterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MorePaths {

    /**
     * Starting with the first path element, add elements until reaching this path.
     */
    public static Stream<Path> iterPaths(Path path) {
        Deque<Path> parents = new ArrayDeque<>(path.getNameCount());
        // Push parents to the front of the stack, so the "root" is at the front
        Path next = path;
        while (next != null) {
            parents.addFirst(next);
            next = next.getParent();
        }
        // now just iterate straight over them
        return ImmutableList.copyOf(parents).stream();
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

    private MorePaths() {
    }
}
