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

package com.sk89q.worldedit.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;

/**
 * Utility methods for Google's GSON library.
 */
public final class GsonUtil {

    private GsonUtil() {
    }

    /**
     * Create a standard {@link GsonBuilder} for WorldEdit.
     *
     * @return a builder
     */
    public static GsonBuilder createBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Vector3.class, new VectorAdapter());
        gsonBuilder.registerTypeAdapter(BlockVector3.class, new BlockVectorAdapter());
        return gsonBuilder;
    }

    private static final Gson gson = new Gson();
    public static String stringValue(String s) {
        return gson.toJson(s);
    }
}
