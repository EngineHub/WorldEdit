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
            paths("a", "a/b", "a/b/c"),
            MorePaths.iterPaths(Paths.get("a/b/c")).collect(toList())
        );
    }

    @Test
    void testAbsolute() {
        assertEquals(
            paths("/", "/a", "/a/b", "/a/b/c"),
            MorePaths.iterPaths(Paths.get("/a/b/c")).collect(toList())
        );
    }

    @Test
    void testEmpty() {
        assertEquals(
            paths(""),
            MorePaths.iterPaths(Paths.get("")).collect(toList())
        );
    }

    @Test
    void testJustFile() {
        assertEquals(
            paths("a"),
            MorePaths.iterPaths(Paths.get("a")).collect(toList())
        );
    }
}
