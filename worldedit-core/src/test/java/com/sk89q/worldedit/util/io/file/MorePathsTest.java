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
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MorePathsTest {

    private static List<Path> paths(String... paths) {
        return Stream.of(paths).map(Paths::get).collect(toList());
    }

    @Test
    void testRelative() {
        assertEquals(
            paths("a", "a/b"),
            MorePaths.iterParents(Paths.get("a/b/c")).collect(toList())
        );
    }

    @Test
    void testAbsolute() {
        assertEquals(
            paths("/a", "/a/b"),
            MorePaths.iterParents(Paths.get("/a/b/c")).collect(toList())
        );
    }

    @Test
    void testEmpty() {
        assertEquals(
            paths(),
            MorePaths.iterParents(Paths.get("")).collect(toList())
        );
    }

    @Test
    void testJustFile() {
        assertEquals(
            paths(),
            MorePaths.iterParents(Paths.get("a")).collect(toList())
        );
    }
}
