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

package com.sk89q.worldedit.session.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.util.gson.GsonUtil;
import com.sk89q.worldedit.util.io.Closer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores sessions as JSON files in a directory.
 *
 * <p>Currently, this implementation doesn't handle thread safety very well.</p>
 */
public class JsonFileSessionStore implements SessionStore {

    private static final Logger log = Logger.getLogger(JsonFileSessionStore.class.getCanonicalName());
    private final Gson gson;
    private final File dir;

    /**
     * Create a new session store.
     *
     * @param dir the directory
     */
    public JsonFileSessionStore(File dir) {
        checkNotNull(dir);

        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                log.log(Level.WARNING, "Failed to create directory '" + dir.getPath() + "' for sessions");
            }
        }

        this.dir = dir;

        GsonBuilder builder = GsonUtil.createBuilder();
        gson = builder.create();
    }

    /**
     * Get the path for the given UUID.
     *
     * @param id the ID
     * @return the file
     */
    private File getPath(UUID id) {
        checkNotNull(id);
        return new File(dir, id + ".json");
    }

    @Override
    public LocalSession load(UUID id) throws IOException {
        File file = getPath(id);
        try (Closer closer = Closer.create()) {
            FileReader fr = closer.register(new FileReader(file));
            BufferedReader br = closer.register(new BufferedReader(fr));
            return gson.fromJson(br, LocalSession.class);
        } catch (JsonParseException e) {
            throw new IOException(e);
        } catch (FileNotFoundException e) {
            return new LocalSession();
        }
    }

    @Override
    public void save(UUID id, LocalSession session) throws IOException {
        File finalFile = getPath(id);
        File tempFile = new File(finalFile.getParentFile(), finalFile.getName() + ".tmp");

        try (Closer closer = Closer.create()) {
            FileWriter fr = closer.register(new FileWriter(tempFile));
            BufferedWriter bw = closer.register(new BufferedWriter(fr));
            gson.toJson(session, bw);
        } catch (JsonIOException e) {
            throw new IOException(e);
        }

        if (finalFile.exists()) {
            if (!finalFile.delete()) {
                log.log(Level.WARNING, "Failed to delete " + finalFile.getPath() + " so the .tmp file can replace it");
            }
        }

        if (!tempFile.renameTo(finalFile)) {
            log.log(Level.WARNING, "Failed to rename temporary session file to " + finalFile.getPath());
        }
    }

}
