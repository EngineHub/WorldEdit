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

package com.sk89q.worldedit;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Represents WorldEdit info from the MANIFEST.MF file.
 */
public class WorldEditManifest {

    public static final String WORLD_EDIT_VERSION = "WorldEdit-Version";

    public static WorldEditManifest load() {
        Attributes attributes = readAttributes();
        return new WorldEditManifest(
            readAttribute(attributes, WORLD_EDIT_VERSION, () -> "(unknown)")
        );
    }

    private static @Nullable Attributes readAttributes() {
        Class<WorldEditManifest> clazz = WorldEditManifest.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            return null;
        }

        try {
            URL url = new URL(classPath);
            JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            Manifest manifest = jarConnection.getManifest();
            return manifest.getMainAttributes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readAttribute(@Nullable Attributes attributes, String name,
                                        Supplier<String> defaultAction) {
        if (attributes == null) {
            return defaultAction.get();
        }
        String value = attributes.getValue(name);
        return value != null ? value : defaultAction.get();
    }

    private final String worldEditVersion;

    private WorldEditManifest(String worldEditVersion) {
        this.worldEditVersion = worldEditVersion;
    }

    public String getWorldEditVersion() {
        return worldEditVersion;
    }
}
