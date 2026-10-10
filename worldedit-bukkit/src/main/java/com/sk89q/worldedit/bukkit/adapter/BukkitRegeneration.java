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

package com.sk89q.worldedit.bukkit.adapter;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Owns temporary generation resources and snapshots chunks after all their FEATURES writes finish.
 * Construction, resource registration and disposal must run on the global region thread on Folia.
 *
 * @param <C> the adapter's chunk type
 */
public final class BukkitRegeneration<C> implements AutoCloseable {
    private static final String NAME = "worldeditregentempworld";
    private final Path directory;
    private final Closer resources = Closer.create();
    private final CompletableFuture<Clipboard> result = new CompletableFuture<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Create a temporary generation directory after checking that its world name is available.
     *
     * @param worldsField the server's world registry field
     * @throws IOException if the registry cannot be accessed or the directory cannot be created
     */
    public BukkitRegeneration(Field worldsField) throws IOException {
        this(worlds(worldsField));
    }

    BukkitRegeneration(Map<String, World> worlds) throws IOException {
        if (worlds.containsKey(NAME)) {
            throw new IOException("A world named " + NAME + " is already registered");
        }
        directory = Files.createTempDirectory("WorldEditWorldGen");
        registerResource(() -> SafeFiles.tryHardToDeleteDir(directory));
        registerResource(() -> worlds.remove(NAME));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, World> worlds(Field field) throws IOException {
        try {
            return (Map<String, World>) field.get(Bukkit.getServer());
        } catch (IllegalAccessException e) {
            throw new IOException("Could not access the server's worlds", e);
        }
    }

    /**
     * Get the temporary world's registry name.
     *
     * @return the temporary world name
     */
    public String name() {
        return NAME;
    }

    /**
     * Get the temporary world's storage directory.
     *
     * @return the directory removed when this generation job closes
     */
    public Path directory() {
        return directory;
    }

    /**
     * Register a resource immediately after acquisition; resources close in reverse order.
     *
     * @param resource the resource to close with this generation job
     * @param <T> the resource type
     * @return the registered resource
     */
    public <T extends AutoCloseable> T registerResource(T resource) {
        resources.register((Closeable) () -> {
            try {
                resource.close();
            } catch (Exception e) {
                throw new IOException("Could not close generation resource", e);
            }
        });
        return resource;
    }

    /**
     * Generate the selected chunks and copy them after every requested chunk finishes.
     *
     * @param selection the region to generate
     * @param loadChunk the chunk loader, which passes null to its callback on failure
     * @param copy the copier that detaches blocks and optional biomes from the generated chunks
     */
    public void generate(Region selection, BiConsumer<BlockVector2, Consumer<C>> loadChunk,
                         SnapshotCopier<C> copy) {
        Region region = selection.clone();
        List<CompletableFuture<C>> chunks = new ArrayList<>();
        for (BlockVector2 position : region.getChunks()) {
            CompletableFuture<C> chunk = new CompletableFuture<>();
            chunks.add(chunk);
            loadChunk.accept(position, generated -> {
                if (generated == null) {
                    chunk.completeExceptionally(new IllegalStateException("Failed to generate chunk " + position));
                } else {
                    chunk.complete(generated);
                }
            });
        }
        var _ = CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).thenApply(_ -> {
            // FEATURES can write into neighboring chunks. Read only after every requested chunk finishes.
            checkOpen();
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            try {
                // All futures are complete; the adapter can read them without blocking.
                copy.copy(region, clipboard, chunks);
            } catch (WorldEditException e) {
                throw new CompletionException(e);
            }
            return clipboard;
        }).whenComplete((snapshot, error) -> {
            if (error == null) {
                result.complete(snapshot);
            } else {
                result.completeExceptionally(error);
            }
        });
    }

    /** Check before reading each generated block, since disposal can race snapshot creation. */
    public void checkOpen() {
        if (closed.get()) {
            throw new CancellationException("Generation world was closed");
        }
    }

    /**
     * Copies completed chunks into a detached clipboard.
     *
     * @param <C> the adapter's chunk type
     */
    @FunctionalInterface
    public interface SnapshotCopier<C> {
        /**
         * Copy the selected blocks and optional biomes without retaining generation resources.
         *
         * @param region the region to copy
         * @param clipboard the destination clipboard
         * @param chunks the completed chunk futures
         * @throws WorldEditException if copying fails
         */
        void copy(Region region, Clipboard clipboard, List<CompletableFuture<C>> chunks) throws WorldEditException;
    }

    /**
     * Get the detached snapshot after generation completes.
     *
     * @return the generated blocks and optional biomes
     */
    public CompletionStage<Clipboard> result() {
        return result;
    }

    /**
     * Abort generation and release its resources without hiding the original failure.
     *
     * @param failure the failure to rethrow
     * @return does not return; always throws
     * @throws Exception if the original failure is a checked exception
     */
    public RuntimeException rethrowAndClose(Throwable failure) throws Exception {
        try {
            throw resources.rethrow(failure, Exception.class);
        } finally {
            close();
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        try {
            resources.close();
        } finally {
            result.cancel(false);
        }
    }
}
