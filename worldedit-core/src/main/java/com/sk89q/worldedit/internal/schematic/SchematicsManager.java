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

package com.sk89q.worldedit.internal.schematic;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.schematic.backends.DummySchematicsBackend;
import com.sk89q.worldedit.internal.schematic.backends.FileWatcherSchematicsBackend;
import com.sk89q.worldedit.internal.schematic.backends.PollingSchematicsBackend;
import com.sk89q.worldedit.internal.schematic.backends.SchematicsBackend;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class that manages the known schematic files.
 *
 * <p>This class monitors the schematics folder for changes and maintains an up-to-date list of known
 * schematics in order to speed up queries.
 *
 * <p>If initialization of the file-watching backend fails, a polling backend is used instead.
 */
public class SchematicsManager {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final WorldEdit worldEdit;

    private Path schematicsDir;
    private SchematicsBackend backend;

    public SchematicsManager(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    private void createFallbackBackend() {
        LOGGER.warn("Failed to initialize file-monitoring based schematics backend. Falling back to polling.");
        backend = PollingSchematicsBackend.create(schematicsDir);
    }

    private void setupBackend() {
        try {
            var fileWatcherBackend = FileWatcherSchematicsBackend.create(schematicsDir);
            if (fileWatcherBackend.isPresent()) {
                backend = fileWatcherBackend.get();
            } else {
                createFallbackBackend();
            }
        } catch (IOException e) {
            createFallbackBackend();
        }
    }

    /**
     * Initialize this SchematicsManager.
     * This sets everything up, and initially scans the schematics folder.
     */
    public void init() {
        try {
            schematicsDir = worldEdit.getWorkingDirectoryPath(worldEdit.getConfiguration().saveDir);
            Files.createDirectories(schematicsDir);
            schematicsDir = schematicsDir.toRealPath();
            setupBackend();
        } catch (IOException e) {
            LOGGER.warn("Failed to create schematics directory", e);
            backend = new DummySchematicsBackend(); //fallback to dummy backend
        }
        backend.init();
    }

    /**
     * Uninitialize this SchematicsManager.
     */
    public void uninit() {
        if (backend != null) {
            backend.uninit();
            backend = null;
        }
    }

    /**
     * Gets the root folder in which the schematics are stored.
     *
     * @return the root folder where schematics are stored
     */
    public Path getRoot() {
        checkNotNull(schematicsDir, "not initialized");
        return schematicsDir;
    }

    /**
     * Gets a set of known schematics.
     *
     * @return a set of all known schematics
     */
    public Set<Path> getSchematicPaths() {
        checkNotNull(backend);
        return backend.getPaths();
    }

    /**
     * Force an update of the list.
     */
    public void update() {
        backend.update();
    }

}
