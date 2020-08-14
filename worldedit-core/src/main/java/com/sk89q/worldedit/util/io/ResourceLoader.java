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

package com.sk89q.worldedit.util.io;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An abstract loader that handles loading resources from bundled URLs or local
 * files.
 */
public interface ResourceLoader {

    /**
     * Gets the bundled resource URL by name, relative to the provided class.
     *
     * <p>
     * The path name <b>must not</b> start with {@code /}.
     * </p>
     *
     * @param clazz The class to search relative to
     * @param pathName The path name
     * @return The URL to this bundled resource
     * @throws IOException if an IO issue occurs
     */
    @Nullable
    default URL getResource(Class<?> clazz, String pathName) throws IOException {
        checkArgument(!pathName.startsWith("/"), "pathName must not start with /");
        String qualifiedName = clazz.getName().substring(0, clazz.getName().lastIndexOf('.'))
            .replace(".", "/") + "/" + pathName;
        return getRootResource(qualifiedName);
    }

    /**
     * Gets the bundled resource URL by name.
     *
     * <p>
     * The path name <b>must not</b> start with {@code /}. It is always
     * absolute.
     * </p>
     *
     * @param pathName The path name
     * @return The URL to this bundled resource
     * @throws IOException if an IO issue occurs
     */
    @Nullable
    default URL getRootResource(String pathName) throws IOException {
        checkArgument(!pathName.startsWith("/"), "pathName must not start with /");
        return this.getClass().getClassLoader().getResource(pathName);
    }

    /**
     * Gets the {@link Path} reference to this local resource. The file may not
     * exist.
     *
     * @param pathName The path name
     * @return The path reference
     */
    Path getLocalResource(String pathName);
}
