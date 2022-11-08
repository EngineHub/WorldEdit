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

package com.sk89q.worldedit.util.schematic;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.schematic.backends.DummySchematicsBackend;
import com.sk89q.worldedit.util.schematic.backends.FileWatcherSchematicsBackend;
import com.sk89q.worldedit.util.schematic.backends.PollingSchematicsBackend;
import com.sk89q.worldedit.util.schematic.backends.SchematicsBackend;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Class that manages the known Schematic files.
 *
 * <p>This class monitors the schematics folder for changes and keeps an always-up-to-date list of known
 * schematics in RAM in order to speed up queries.
 * This further also allows more convenient features like supporting command suggestions for known schematics.
 *
 * <p>If initialization of the inotify Backend fails, SchematicsManager is going to fall back to a polled variant, where
 * the result is cached for a certain amount of time, before a rescan is performed. (Eventual Consistency Cache)
 */
public class SchematicsManager {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private WorldEdit worldEdit;

    private Path schematicsDir;
    private SchematicsBackend backend;

    public SchematicsManager(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    private void setupBackend() {
        try {
            backend = FileWatcherSchematicsBackend.create(schematicsDir);
        } catch (IOException e) {
            LOGGER.warn("Failed to initialize folder-monitoring based Schematics backend. Falling back to scanning.");
            backend = PollingSchematicsBackend.create(schematicsDir);
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
     * Get the RootPath for schematics.
     * @return The root folder where schematics are stored.
     */
    public Path getRoot() {
        return schematicsDir;
    }

    /**
     * Get a list of all known schematics.
     * @return List of all known schematics.
     */
    public List<Schematic> getList() {
        return backend.getList();
    }

    /**
     * Tell SchematicManager that an update is in order.
     * This should be used whenever WorldEdit code adds a new Schematic, to make sure the next list
     * response is up-to-date for better user-experience.
     */
    public void update() {
        backend.update();
    }

}
