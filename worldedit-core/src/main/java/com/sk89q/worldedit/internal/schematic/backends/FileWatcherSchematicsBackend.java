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

package com.sk89q.worldedit.internal.schematic.backends;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.RecursiveDirectoryWatcher;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * A backend that efficiently scans for file changes using {@link RecursiveDirectoryWatcher}.
 */
public class FileWatcherSchematicsBackend implements SchematicsBackend {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final Set<Path> schematics = Sets.newConcurrentHashSet();
    private final RecursiveDirectoryWatcher directoryWatcher;

    private FileWatcherSchematicsBackend(RecursiveDirectoryWatcher directoryWatcher) {
        this.directoryWatcher = directoryWatcher;
    }

    /**
     * Create a new instance of the directory-monitoring SchematicsManager backend.
     * @param schematicsFolder Root folder for schematics.
     * @return A new FileWatcherSchematicsBackend instance.
     * @throws IOException When creation of the filesystem watcher fails.
     */
    public static FileWatcherSchematicsBackend create(Path schematicsFolder) throws IOException {
        return new FileWatcherSchematicsBackend(RecursiveDirectoryWatcher.create(schematicsFolder));
    }

    @Override
    public void init() {
        directoryWatcher.start(event -> {
            if (event instanceof RecursiveDirectoryWatcher.FileCreatedEvent) {
                schematics.add(event.path());
                LOGGER.debug("New Schematic found: " + event.path());
            } else if (event instanceof RecursiveDirectoryWatcher.FileDeletedEvent) {
                schematics.remove(event.path());
                LOGGER.debug("Schematic deleted: " + event.path());
            }
        });
    }

    @Override
    public void uninit() {
        directoryWatcher.close();
    }

    @Override
    public Set<Path> getPaths() {
        return Set.copyOf(schematics);
    }

    @Override
    public void update() {
        // Nothing to do here, we probably already know :)
    }
}