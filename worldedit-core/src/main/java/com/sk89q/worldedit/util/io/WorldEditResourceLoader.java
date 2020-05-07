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

import com.sk89q.worldedit.WorldEdit;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WorldEditResourceLoader implements ResourceLoader {

    private final WorldEdit worldEdit;

    public WorldEditResourceLoader(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    @Override
    public URL getResource(Class<?> clazz, String pathname) throws IOException {
        return clazz.getResource(pathname);
    }

    @Override
    public URL getRootResource(String pathname) throws IOException {
        return WorldEditResourceLoader.class.getResource("/" + pathname);
    }

    @Override
    public File getLocalResource(String pathname) {
        return this.worldEdit.getWorkingDirectoryFile(pathname);
    }
}
