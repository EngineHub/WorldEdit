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

package com.sk89q.worldedit.session.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.util.gson.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores sessions as JSON files in a directory.
 *
 * <p>Currently, this implementation doesn't handle thread safety very well.</p>
 */
public class JsonFileSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(JsonFileSessionStore.class);
    private final Gson gson;
    private final Path dir;

    /**
     * Create a new session store.
     *
     * @param dir the directory
     * @deprecated Use {@link #JsonFileSessionStore(Path)} instead
     */
    @Deprecated
    public JsonFileSessionStore(File dir) {
        this(dir.toPath());
    }

    /**
     * Create a new session store.
     *
     * @param dir the directory
     */
    public JsonFileSessionStore(Path dir) {
        checkNotNull(dir);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.warn("Failed to create directory '" + dir + "' for sessions", e);
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
    private Path getPath(UUID id) {
        checkNotNull(id);
        return dir.resolve(id + ".json");
    }

    @Override
    public LocalSession load(UUID id) throws IOException {
        Path path = getPath(id);
        try (Reader reader = Files.newBufferedReader(path)) {
            LocalSession session = gson.fromJson(reader, LocalSession.class);
            if (session == null) {
                log.warn("Loaded a null session from {}, creating new session", path);
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.warn("Failed to delete corrupted session " + path, e);
                }
                session = new LocalSession();
            }
            return session;
        } catch (JsonParseException e) {
            throw new IOException(e);
        } catch (FileNotFoundException e) {
            return new LocalSession();
        }
    }

    @Override
    public void save(UUID id, LocalSession session) throws IOException {
        checkNotNull(session);
        Path finalFile = getPath(id);
        Path tempFile = finalFile.resolveSibling(finalFile.getFileName() + ".tmp");

        try (Writer writer = Files.newBufferedWriter(tempFile)) {
            gson.toJson(session, writer);
        } catch (JsonIOException e) {
            throw new IOException(e);
        }

        if (Files.size(tempFile) == 0) {
            throw new IllegalStateException("Gson wrote zero bytes");
        }

        try {
            try {
                Files.move(
                    finalFile, tempFile,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(
                    finalFile, tempFile,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException e) {
            log.warn("Failed to rename temporary session file to " + finalFile, e);
        }
    }

}
