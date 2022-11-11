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

package com.sk89q.worldedit.util.schematic.backends;

import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.io.file.RecursiveDirectoryWatcher;
import com.sk89q.worldedit.util.schematic.SchematicPath;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SchematicsBackend making use of the RecursiveDirectoryWatcher.
 * This backend initially scans all schematics in the folder tree and then registers for file change events to
 * avoid manually polling / rescanning the folder structure for every query.
 */
public class FileWatcherSchematicsBackend implements SchematicsBackend {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Set<SchematicPath> schematics = new HashSet<>();
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
        RecursiveDirectoryWatcher watcher = RecursiveDirectoryWatcher.create(schematicsFolder);
        return new FileWatcherSchematicsBackend(watcher);
    }

    @Override
    public void init() {
        directoryWatcher.start(event -> {
            lock.writeLock().lock();
            if (event instanceof RecursiveDirectoryWatcher.FileCreatedEvent) {
                schematics.add(new SchematicPath(event.getPath()));
            } else if (event instanceof RecursiveDirectoryWatcher.FileDeletedEvent) {
                schematics.remove(new SchematicPath(event.getPath()));
            }
            lock.writeLock().unlock();
            if (event instanceof RecursiveDirectoryWatcher.FileCreatedEvent) {
                LOGGER.info("New Schematic found: " + event.getPath());
            } else if (event instanceof RecursiveDirectoryWatcher.FileDeletedEvent) {
                LOGGER.info("Schematic deleted: " + event.getPath());
            }
        });
    }

    @Override
    public void uninit() {
        directoryWatcher.stop();
    }

    @Override
    public List<SchematicPath> getList() {
        lock.readLock().lock();
        List<SchematicPath> result = new ArrayList<>(schematics);
        lock.readLock().unlock();
        return result;
    }

    @Override
    public void update() {
        // Nothing to do here, we probably already know :)
    }
}