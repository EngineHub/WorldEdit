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

package com.sk89q.worldedit.bukkit.folia;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.RegenOptions;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

/**
 * Runs {@code //regen} on Folia, where terrain generation cannot be awaited on a region thread.
 *
 * <p>The adapter generates the terrain into a detached snapshot. The chunks of the selection are then
 * loaded and held with plugin tickets, the snapshot is written to the extent on the thread owning them,
 * and the returned future completes on the actor's own scheduler. A clipboard is written directly by
 * the adapter, since it never touches the world.</p>
 */
public final class FoliaRegeneration {

    /**
     * Freshly loaded chunks can take a few ticks to merge into the region applying the result.
     */
    private static final int OWNERSHIP_ATTEMPTS = 40;

    private FoliaRegeneration() {
    }

    /**
     * Regenerate the region without blocking the calling region thread.
     *
     * @param plugin the plugin to schedule with
     * @param adapter the adapter generating the terrain
     * @param world the world to regenerate in
     * @param region the region to regenerate
     * @param extent the extent to write the result to
     * @param options the regeneration options
     * @param actor the actor that requested the regeneration, or null for an API call without one
     * @return a future completing with {@code true} on the actor's scheduler, or exceptionally on failure
     */
    public static CompletionStage<Boolean> regenerate(WorldEditPlugin plugin, BukkitImplAdapter adapter,
                                                      BukkitWorld world, Region region, Extent extent,
                                                      RegenOptions options, @Nullable Actor actor) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            if (extent instanceof EditSession editSession && editSession.getBlockBag() != null) {
                // The block bag reads the player's inventory, which may be in another region by then.
                throw new RegionOperationException(TranslatableComponent.of("worldedit.regen.inventory-unsupported"));
            }
            boolean direct = extent instanceof Clipboard;
            BlockArrayClipboard snapshot = direct ? null : new BlockArrayClipboard(region);
            Extent generated = direct ? extent : snapshot;
            adapter.regenerateAsync(world.getWorld(), region, generated, options).whenComplete((_, error) -> {
                if (error != null || direct) {
                    complete(plugin, actor, result, error);
                    return;
                }
                Job job = new Job(plugin, world, region, snapshot, extent, options, actor, result,
                    ConcurrentHashMap.newKeySet());
                loadChunks(job).whenComplete((_, loadError) -> {
                    if (loadError != null) {
                        finish(job, loadError);
                    } else {
                        applyWhenOwned(job, 0);
                    }
                });
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletionStage<Void> loadChunks(Job job) {
        World world = job.world().getWorld();
        List<CompletableFuture<Void>> loads = new ArrayList<>();
        for (BlockVector2 chunk : job.region().getChunks()) {
            loads.add(world.getChunkAtAsync(chunk.x(), chunk.z()).thenCompose(_ -> holdChunk(job, chunk)));
        }
        return CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new));
    }

    private static CompletableFuture<Void> holdChunk(Job job, BlockVector2 chunk) {
        World world = job.world().getWorld();
        CompletableFuture<Void> held = new CompletableFuture<>();
        // A plugin ticket loads the chunk synchronously if needed, so it is added on the thread owning the chunk.
        Bukkit.getRegionScheduler().execute(job.plugin(), world, chunk.x(), chunk.z(), () -> {
            try {
                if (world.addPluginChunkTicket(chunk.x(), chunk.z(), job.plugin())) {
                    job.tickets().add(chunk);
                }
                held.complete(null);
            } catch (Throwable t) {
                held.completeExceptionally(t);
            }
        });
        return held;
    }

    private static void applyWhenOwned(Job job, int attempt) {
        World world = job.world().getWorld();
        BlockVector3 anchor = job.region().getMinimumPoint();
        Bukkit.getRegionScheduler().runDelayed(job.plugin(), world, anchor.x() >> 4, anchor.z() >> 4, _ -> {
            if (!isOwned(world, job.region())) {
                if (attempt < OWNERSHIP_ATTEMPTS) {
                    applyWhenOwned(job, attempt + 1);
                } else {
                    finish(job, new RegionOperationException(TranslatableComponent.of("worldedit.regen.folia-region")));
                }
                return;
            }
            Throwable failure = null;
            try {
                apply(job);
            } catch (Exception e) {
                failure = e;
            }
            finish(job, failure);
        }, 1);
    }

    private static boolean isOwned(World world, Region region) {
        for (BlockVector2 chunk : region.getChunks()) {
            if (!Bukkit.isOwnedByCurrentRegion(world, chunk.x(), chunk.z())) {
                return false;
            }
        }
        return true;
    }

    private static void apply(Job job) throws WorldEditException {
        Extent extent = job.extent();
        try {
            // Masks parsed without an extent, such as the global mask, read it from the current request.
            Request.runWithRequest(() -> {
                Request.request().setWorld(job.world());
                if (extent instanceof EditSession editSession) {
                    Request.request().setEditSession(editSession);
                }
                try {
                    for (BlockVector3 position : job.region()) {
                        extent.setBlock(position, job.snapshot().getFullBlock(position));
                        if (job.options().shouldRegenBiomes()) {
                            extent.setBiome(position, job.snapshot().getBiome(position));
                        }
                    }
                } catch (WorldEditException e) {
                    throw new CompletionException(e);
                } finally {
                    // Flush here, as the thread the command returns to may not write to this region.
                    Operations.completeBlindly(extent.commit());
                }
            });
        } catch (CompletionException e) {
            if (e.getCause() instanceof WorldEditException cause) {
                throw cause;
            }
            throw e;
        }
    }

    private static void finish(Job job, @Nullable Throwable failure) {
        World world = job.world().getWorld();
        for (BlockVector2 chunk : job.tickets()) {
            world.removePluginChunkTicket(chunk.x(), chunk.z(), job.plugin());
        }
        complete(job.plugin(), job.actor(), job.result(), failure);
    }

    private static void complete(WorldEditPlugin plugin, @Nullable Actor actor, CompletableFuture<Boolean> result,
                                 @Nullable Throwable failure) {
        Runnable completion = () -> {
            if (failure == null) {
                result.complete(true);
            } else {
                result.completeExceptionally(failure);
            }
        };
        if (actor instanceof BukkitPlayer player) {
            // If the player is already gone, complete right here so that history is still recorded.
            if (!player.getPlayer().getScheduler().execute(plugin, completion, completion, 1)) {
                completion.run();
            }
        } else {
            // Other actors and API callers continue on the thread that finished the job.
            completion.run();
        }
    }

    private record Job(WorldEditPlugin plugin, BukkitWorld world, Region region, BlockArrayClipboard snapshot,
                       Extent extent, RegenOptions options, @Nullable Actor actor,
                       CompletableFuture<Boolean> result, Set<BlockVector2> tickets) {
    }
}
