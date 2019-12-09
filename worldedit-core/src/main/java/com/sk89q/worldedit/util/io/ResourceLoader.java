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

import java.io.IOException;
import java.net.URL;

public class ResourceLoader {

    private ResourceLoader() {
    }

    private static URL getResourceForgeHack(String location) throws IOException {
        try {
            return new URL("modjar://worldedit/" + location);
        } catch (Exception e) {
            throw new IOException("Could not find " + location);
        }
    }

    public static URL getResource(Class clazz, String name) throws IOException {
        URL url = clazz.getResource(name);
        if (url == null) {
            return getResourceForgeHack(clazz.getName().substring(0, clazz.getName().lastIndexOf('.')).replace(".", "/")
                    + "/" + name);
        }
        return url;
    }

    public static URL getResourceRoot(String name) throws IOException {
        URL url = ResourceLoader.class.getResource("/" + name);
        if (url == null) {
            return getResourceForgeHack(name);
        }
        return url;
    }
}
