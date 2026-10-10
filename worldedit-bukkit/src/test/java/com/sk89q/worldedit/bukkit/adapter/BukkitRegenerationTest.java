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

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BukkitRegenerationTest {
    @Test
    void releasesResourcesInReverseOrderAndUnregistersOnlyItsOwnWorld() throws Exception {
        Map<String, World> worlds = new HashMap<>();
        World source = mock(World.class);
        worlds.put("source", source);
        var generation = new BukkitRegeneration<AtomicInteger>(worlds);
        worlds.put(generation.name(), mock(World.class));
        assertThrows(IOException.class, () -> new BukkitRegeneration<AtomicInteger>(worlds));
        Path directory = generation.directory();
        Files.writeString(directory.resolve("test"), "temporary data");
        List<String> closed = new ArrayList<>();
        generation.registerResource(() -> closed.add("storage"));
        generation.registerResource(() -> closed.add("chunks"));
        generation.close();
        generation.close();
        assertEquals(List.of("chunks", "storage"), closed);
        assertEquals(Map.of("source", source), worlds);
        assertFalse(Files.exists(directory));
        assertTrue(generation.result().toCompletableFuture().isCancelled());
    }

    @Test
    void continuesCleanupWhenStoppingChunksFails() throws Exception {
        Map<String, World> worlds = new HashMap<>();
        var generation = new BukkitRegeneration<AtomicInteger>(worlds);
        worlds.put(generation.name(), mock(World.class));
        AtomicInteger storageCloses = new AtomicInteger();
        IOException failure = new IOException("chunk shutdown failed");
        generation.registerResource(storageCloses::incrementAndGet);
        generation.registerResource(() -> {
            throw failure;
        });
        IOException error = assertThrows(IOException.class, generation::close);
        assertSame(failure, error.getCause());
        assertEquals(1, storageCloses.get());
        assertTrue(worlds.isEmpty());
        assertFalse(Files.exists(generation.directory()));
        assertTrue(generation.result().toCompletableFuture().isCancelled());
    }

    @Test
    void cancelsTheResultEvenWhenDeletingTheDirectoryFails() throws Exception {
        Map<String, World> worlds = new HashMap<>();
        var generation = new BukkitRegeneration<AtomicInteger>(worlds);
        worlds.put(generation.name(), mock(World.class));
        Path directory = generation.directory();
        Files.delete(directory);
        Files.writeString(directory, "not a directory");
        try {
            assertThrows(IOException.class, generation::close);
            assertTrue(worlds.isEmpty());
            assertTrue(generation.result().toCompletableFuture().isCancelled());
        } finally {
            Files.delete(directory);
        }
    }

    @Test
    void preservesTheGenerationFailureWhenCleanupAlsoFails() throws Exception {
        var generation = new BukkitRegeneration<AtomicInteger>(new HashMap<>());
        IOException failure = new IOException("generation failed");
        IOException cleanupFailure = new IOException("chunk shutdown failed");
        generation.registerResource(() -> {
            throw cleanupFailure;
        });
        assertSame(failure, assertThrows(IOException.class, () -> generation.rethrowAndClose(failure)));
        assertSame(cleanupFailure, failure.getSuppressed()[0].getCause());
        assertFalse(Files.exists(generation.directory()));
        assertTrue(generation.result().toCompletableFuture().isCancelled());
    }

    @Test
    void waitsForAllChunksBeforeCopyingNeighborFeatures() throws Exception {
        try (var generation = new BukkitRegeneration<AtomicInteger>(new HashMap<>())) {
            CompletableFuture<AtomicInteger> first = new CompletableFuture<>();
            CompletableFuture<AtomicInteger> neighbor = new CompletableFuture<>();
            AtomicInteger firstBlock = new AtomicInteger(1);
            List<Integer> reads = new ArrayList<>();
            start(generation, first, neighbor, reads);
            first.complete(firstBlock);
            assertTrue(reads.isEmpty());
            assertFalse(generation.result().toCompletableFuture().isDone());
            firstBlock.set(2);
            neighbor.complete(new AtomicInteger(3));
            generation.result().toCompletableFuture().join();
            assertEquals(17, reads.size());
            assertEquals(16, reads.stream().filter(value -> value == 2).count());
            assertEquals(1, reads.stream().filter(value -> value == 3).count());
        }
    }

    @Test
    void ignoresChunksCompletingAfterDisposal() throws Exception {
        var generation = new BukkitRegeneration<AtomicInteger>(new HashMap<>());
        CompletableFuture<AtomicInteger> first = new CompletableFuture<>();
        CompletableFuture<AtomicInteger> neighbor = new CompletableFuture<>();
        List<Integer> reads = new ArrayList<>();
        start(generation, first, neighbor, reads);
        first.complete(new AtomicInteger(1));
        generation.close();
        neighbor.complete(new AtomicInteger(2));
        assertTrue(reads.isEmpty());
        assertTrue(generation.result().toCompletableFuture().isCancelled());
    }

    @Test
    void doesNotReadChunksWhenGenerationFails() throws Exception {
        try (var generation = new BukkitRegeneration<AtomicInteger>(new HashMap<>())) {
            var first = new CompletableFuture<AtomicInteger>();
            var neighbor = new CompletableFuture<AtomicInteger>();
            List<Integer> reads = new ArrayList<>();
            start(generation, first, neighbor, reads);
            first.complete(new AtomicInteger(1));
            neighbor.completeExceptionally(new IllegalStateException("generation failed"));
            assertTrue(generation.result().toCompletableFuture().isCompletedExceptionally());
            assertTrue(reads.isEmpty());
        }
    }

    private static void start(BukkitRegeneration<AtomicInteger> generation,
                              CompletableFuture<AtomicInteger> first,
                              CompletableFuture<AtomicInteger> neighbor, List<Integer> reads) {
        var region = new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(16, 0, 0));
        generation.generate(region, (position, callback) -> {
            var chunk = position.equals(BlockVector2.ZERO) ? first : neighbor;
            var _ = chunk.whenComplete((value, _) -> callback.accept(value));
        }, (selection, _, _) -> {
            for (BlockVector3 position : selection) {
                generation.checkOpen();
                reads.add((position.x() < 16 ? first : neighbor).join().get());
            }
        });
    }
}
