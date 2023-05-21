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

import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.RecursiveDirectoryWatcher;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A backend that efficiently scans for file changes using {@link RecursiveDirectoryWatcher}.
 */
public class FileWatcherSchematicsBackend implements SchematicsBackend {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Set<Path> schematics = new HashSet<>();
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
    public static Optional<FileWatcherSchematicsBackend> create(Path schematicsFolder) throws IOException {
        return RecursiveDirectoryWatcher.create(schematicsFolder).map(FileWatcherSchematicsBackend::new);
    }

    @Override
    public void init() {
        directoryWatcher.start(event -> {
            lock.writeLock().lock();
            try {
                if (event instanceof RecursiveDirectoryWatcher.FileCreatedEvent) {
                    schematics.add(event.path());
                } else if (event instanceof RecursiveDirectoryWatcher.FileDeletedEvent) {
                    schematics.remove(event.path());
                }
            } finally {
                lock.writeLock().unlock();
            }
            if (event instanceof RecursiveDirectoryWatcher.FileCreatedEvent) {
                LOGGER.debug("New Schematic found: " + event.path());
            } else if (event instanceof RecursiveDirectoryWatcher.FileDeletedEvent) {
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
        lock.readLock().lock();
        try {
            return Set.copyOf(schematics);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void update() {
        // Nothing to do here, we probably already know :)
    }
}