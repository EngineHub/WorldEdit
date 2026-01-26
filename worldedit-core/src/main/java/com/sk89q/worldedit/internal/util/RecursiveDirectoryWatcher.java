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

package com.sk89q.worldedit.internal.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.file.FilenameException;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;

/**
 * Helper class that recursively monitors a directory for changes to files and folders, including creation, deletion, and modification.
 *
 * @apiNote File and folder events might be sent multiple times. Users of this class need to employ their own
 *      deduplication!
 */
public class RecursiveDirectoryWatcher implements Closeable {

    /**
     * Base interface for all change events.
     */
    public interface DirEntryChangeEvent {
        Path path();
    }

    /**
     * Event signaling the creation of a new file.
     */
    public record FileCreatedEvent(Path path) implements DirEntryChangeEvent {
    }

    /**
     * Event signaling the deletion of a file.
     */
    public record FileDeletedEvent(Path path) implements DirEntryChangeEvent {
    }

    /**
     * Event signaling the creation of a new directory.
     */
    public record DirectoryCreatedEvent(Path path) implements DirEntryChangeEvent {
    }

    /**
     * Event signaling the deletion of a directory.
     */
    public record DirectoryDeletedEvent(Path path) implements DirEntryChangeEvent {
    }


    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final Path root;
    private final WatchService watchService;
    private Thread watchThread;
    private Consumer<DirEntryChangeEvent> eventConsumer;
    private final BiMap<WatchKey, Path> watchRootMap = HashBiMap.create();

    private RecursiveDirectoryWatcher(Path root, WatchService watchService) {
        this.root = root;
        this.watchService = watchService;
    }

    /**
     * Create a new recursive directory watcher for the given root folder.
     * You have to call {@link #start(Consumer)} before the instance starts monitoring.
     *
     * @param root Folder to watch for changed files recursively.
     * @return a new instance that will monitor the given root folder
     * @throws IOException If creating the watcher failed, e.g. due to root not existing.
     */
    public static RecursiveDirectoryWatcher create(Path root) throws IOException {
        WatchService watchService;
        try {
            watchService = root.getFileSystem().newWatchService();
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Root must support file watching", e);
        }
        return new RecursiveDirectoryWatcher(root, watchService);
    }

    private void registerFolderWatcher(Path root) throws IOException {
        WatchKey watchKey = root.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        LOGGER.debug("Watch registered: " + root);
        watchRootMap.put(watchKey, root);
    }

    private void triggerInitialEvents(Path root) throws IOException, FilenameException {
        Path schematicRoot = WorldEdit.getInstance().getSchematicsManager().getRoot();
        eventConsumer.accept(new DirectoryCreatedEvent(root));
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    triggerInitialEvents(path);
                } else {
                    path = WorldEdit.getInstance().getSafeOpenFile(null, schematicRoot.toFile(), schematicRoot.relativize(path).toString(), null).toPath();
                    eventConsumer.accept(new FileCreatedEvent(path));
                }
            }
        }
    }

    /**
     * Make this RecursiveDirectoryWatcher instance start monitoring the root folder it was created on.
     * When this is called, RecursiveDirectoryWatcher will send initial notifications for the entire
     * file structure in the configured root.
     * @param eventConsumer The lambda that's fired for every file event.
     */
    public void start(Consumer<DirEntryChangeEvent> eventConsumer) {
        Path schematicRoot = WorldEdit.getInstance().getSchematicsManager().getRoot();

        this.eventConsumer = eventConsumer;

        if (watchThread != null) {
            // We have an existing thread, interrupt it first to trigger a clean shutdown
            watchThread.interrupt();

            try {
                watchThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
        }

        watchThread = new Thread(() -> {
            LOGGER.debug("RecursiveDirectoryWatcher::EventConsumer started");

            try {
                registerFolderWatcher(root);
                triggerInitialEvents(root);
            } catch (IOException | FilenameException e) {
                LOGGER.error(e);
            }

            try {
                WatchKey watchKey;
                while (!Thread.interrupted()) {
                    try {
                        watchKey = watchService.take();
                    } catch (InterruptedException e) {
                        break;
                    }

                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                            LOGGER.warn("Seems like we can't keep up with updates");
                            continue;
                        }
                        // make sure to work with an absolute path
                        Path path = (Path) event.context();
                        Path parentPath = watchRootMap.get(watchKey);
                        path = parentPath.resolve(path);

                        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                            path = WorldEdit.getInstance().getSafeOpenFile(null, schematicRoot.toFile(), schematicRoot.relativize(path).toString(), null).toPath();

                            if (Files.isDirectory(path)) { // new subfolder created, create watch for it
                                try {
                                    registerFolderWatcher(path);
                                    triggerInitialEvents(path);
                                } catch (IOException | FilenameException e) {
                                    LOGGER.error(e);
                                }
                            } else { // new file created
                                eventConsumer.accept(new FileCreatedEvent(path));
                            }
                        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                            // When we are notified about a deleted entry, we can't simply ask the filesystem
                            // whether the entry is a file or a folder. But we have our watchRootMap, that stores
                            // one WatchKey per (sub)folder, so we can just ask it.
                            if (watchRootMap.containsValue(path)) { // was a folder
                                LOGGER.debug("Watch unregistered: " + path);
                                WatchKey obsoleteSubfolderWatchKey = watchRootMap.inverse().get(path);
                                // stop listening to changes from deleted dir
                                obsoleteSubfolderWatchKey.cancel();
                                watchRootMap.remove(obsoleteSubfolderWatchKey);
                                eventConsumer.accept(new DirectoryDeletedEvent(path));
                            } else { // was a file
                                eventConsumer.accept(new FileDeletedEvent(path));
                            }
                        }
                    }

                    if (!watchKey.reset()) {
                        watchRootMap.remove(watchKey);
                        if (watchRootMap.isEmpty()) {
                            break; // nothing left to watch
                        }
                    }
                }
            } catch (ClosedWatchServiceException | FilenameException ignored) {
                // Watch service closed, exit
            }
            LOGGER.debug("RecursiveDirectoryWatcher::EventConsumer exited");
        });
        watchThread.setName("WorldEdit-RecursiveDirectoryWatcher");
        watchThread.start();
    }

    /**
     * Close this RecursiveDirectoryWatcher instance and wait for it to be completely shut down.
     * @apiNote RecursiveDirectoryWatcher is not reusable!
     */
    @Override
    public void close() {
        try {
            watchService.close();
        } catch (IOException e) {
            LOGGER.error(e);
        }
        if (watchThread != null) {
            watchThread.interrupt();

            try {
                watchThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
            watchThread = null;
        }
        eventConsumer = null;
    }

}
