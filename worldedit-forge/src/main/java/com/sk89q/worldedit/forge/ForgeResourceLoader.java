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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.WorldEditResourceLoader;

import java.io.IOException;
import java.net.URL;

public class ForgeResourceLoader extends WorldEditResourceLoader  {

    public ForgeResourceLoader(WorldEdit worldEdit) {
        super(worldEdit);
    }

    private static URL getResourceForgeHack(String location) throws IOException {
        try {
            URL url = new URL("modjar://worldedit/" + location);
            try {
                url.openStream();
            } catch (IOException e) {
                // doesn't actually exist
                return null;
            }
            return url;
        } catch (Exception e) {
            throw new IOException("Could not find " + location);
        }
    }

    @Override
    public URL getRootResource(String pathName) throws IOException {
        URL url = super.getRootResource(pathName);
        if (url == null) {
            return getResourceForgeHack(pathName);
        }
        return url;
    }

}
