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

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * An abstract loader that handles loading resources
 * from bundled URLs or local files.
 */
public interface ResourceLoader {

    /**
     * Gets the bundled resource URL by name,
     * relative to the provided class.
     *
     * @param clazz The class to search relative to
     * @param pathname The pathname
     * @return The URL to this bundled resource
     * @throws IOException if an IO issue occurs
     */
    URL getResource(Class<?> clazz, String pathname) throws IOException;

    /**
     * Gets the bundled resource URL by name.
     *
     * @param pathname The pathname
     * @return The URL to this bundled resource
     * @throws IOException if an IO issue occurs
     */
    default URL getRootResource(String pathname) throws IOException {
        return getResource(this.getClass(), "/" + pathname);
    }

    /**
     * Gets the {@link File} reference to this
     * local resource. The file may not exist.
     *
     * @param pathname The pathname
     * @return The file reference
     */
    File getLocalResource(String pathname);
}
