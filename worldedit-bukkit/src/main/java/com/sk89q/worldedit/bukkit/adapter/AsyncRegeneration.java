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
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Drives {@link BukkitImplAdapter#regenerateAsync} for adapters that generate in a temporary world.
 *
 * <p>Chunk futures complete on the temporary world's own region threads. Once all of them are done
 * the result is copied, then the temporary world is released on the global region thread: a tick
 * thread, as closing a world requires, that is never inside one of that world's own chunk tasks.</p>
 */
public final class AsyncRegeneration {

    private AsyncRegeneration() {
    }

    /**
     * Copies generated chunks into the output extent.
     *
     * @param <C> the adapter's chunk type
     */
    @FunctionalInterface
    public interface ChunkCopier<C> {

        /**
         * Copy the generated chunks into the output extent.
         *
         * @param chunks the chunk futures, all of which are complete
         * @throws WorldEditException if writing to the extent fails
         */
        void copy(List<CompletableFuture<C>> chunks) throws WorldEditException;
    }

    /**
     * Generate every chunk of the region, copy the result and release the temporary world.
     *
     * @param region the region to generate
     * @param loadChunk schedules generation of one chunk; the future completes with null on failure
     * @param copier copies the generated chunks into the output extent
     * @param temporaryWorld released on the global region thread once copying is done
     * @param <C> the adapter's chunk type
     * @return a future that completes after the temporary world has been released
     */
    public static <C> CompletableFuture<Void> run(Region region,
                                                  Function<BlockVector2, CompletableFuture<C>> loadChunk,
                                                  ChunkCopier<C> copier,
                                                  AutoCloseable temporaryWorld) {
        List<CompletableFuture<C>> chunks = new ArrayList<>();
        for (BlockVector2 chunk : region.getChunks()) {
            try {
                chunks.add(loadChunk.apply(chunk));
            } catch (RuntimeException e) {
                // Still wait for the chunks already scheduled before releasing the world.
                chunks.add(CompletableFuture.failedFuture(e));
                break;
            }
        }
        return CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new))
            .handle((unused, error) -> {
                if (error != null) {
                    return error;
                }
                try {
                    copier.copy(chunks);
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            })
            .thenCompose(failure -> release(temporaryWorld, failure));
    }

    private static CompletableFuture<Void> release(AutoCloseable temporaryWorld, @Nullable Throwable failure) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        // Closing a world requires a tick thread; the global one is also never inside this world's chunk tasks.
        Bukkit.getGlobalRegionScheduler().execute(JavaPlugin.getPlugin(WorldEditPlugin.class), () -> {
            Throwable finalFailure = failure;
            try {
                temporaryWorld.close();
            } catch (Exception e) {
                if (finalFailure == null) {
                    finalFailure = e;
                } else {
                    finalFailure.addSuppressed(e);
                }
            }
            if (finalFailure == null) {
                result.complete(null);
            } else {
                result.completeExceptionally(finalFailure);
            }
        });
        return result;
    }
}
