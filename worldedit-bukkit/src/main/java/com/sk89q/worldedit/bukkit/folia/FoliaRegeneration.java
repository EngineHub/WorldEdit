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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.BukkitRegeneration;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.RegenOptions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nullable;

/** Coordinates generation and cleanup globally, then returns the snapshot to the caller. */
public final class FoliaRegeneration implements AutoCloseable {
    private static final long TIMEOUT_TICKS = 20 * 300;

    private final WorldEditPlugin plugin;
    private final Set<CompletableFuture<Clipboard>> pending = ConcurrentHashMap.newKeySet();
    // Only accessed on the global region thread.
    @Nullable
    private Job active;
    @Nullable
    private ScheduledTask timeout;
    private volatile boolean closed;

    /**
     * Create the regeneration coordinator for a Folia plugin instance.
     *
     * @param plugin the plugin used to schedule generation and result delivery
     */
    public FoliaRegeneration(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Generate a snapshot and return it on the caller's owning thread after checking ownership again.
     *
     * @param world the source world
     * @param selection the selection owned by the caller's current region
     * @param options the regeneration options
     * @param actor the actor receiving the result
     * @param adapter the adapter that creates the temporary generation world
     * @return the detached snapshot, or a failed result if regeneration or ownership checks fail
     */
    @SuppressWarnings("CollectionUndefinedEquality") // CompletableFuture uses object identity in the pending set.
    public CompletionStage<Clipboard> regenerate(World world, Region selection,
                                                 RegenOptions options,
                                                 Actor actor, BukkitImplAdapter adapter) {
        Region region = selection.clone();
        CompletableFuture<Clipboard> result = new CompletableFuture<>();
        pending.add(result);
        var _ = result.whenComplete((_, _) -> pending.remove(result));
        try {
            requireOwned(world, region);
            if (options.getSeed().isPresent() && !adapter.supportsRegenerationSeedOverride()) {
                throw failure("worldedit.regen.seed-unsupported");
            }
            if (world.getGenerator() != null || world.getBiomeProvider() != null) {
                throw failure("worldedit.regen.custom-generator-unsupported");
            }
            Player player = actor.isPlayer() ? Bukkit.getPlayer(actor.getUniqueId()) : null;
            if (actor.isPlayer() && player == null) {
                throw failure("worldedit.regen.cancelled");
            }
            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                if (closed) {
                    complete(world, region, player, result, null, failure("worldedit.regen.cancelled"));
                    return;
                }
                if (active != null) {
                    complete(world, region, player, result, null, failure("worldedit.regen.busy"));
                    return;
                }
                try {
                    Job job = new Job(adapter.beginRegeneration(world, region, options), result, world, region, player);
                    active = job;
                    timeout = Bukkit.getGlobalRegionScheduler().runDelayed(plugin,
                        _ -> finish(job, null, failure("worldedit.regen.timed-out")), TIMEOUT_TICKS);
                    job.generation.result().whenComplete((snapshot, error) -> {
                        if (!closed) {
                            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> finish(job, snapshot, error));
                        }
                    });
                } catch (Exception e) {
                    if (active != null) {
                        finish(active, null, e);
                    } else {
                        complete(world, region, player, result, null, e);
                    }
                }
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    static boolean isOwned(Region region, Predicate<BlockVector2> ownsChunk) {
        return region.getChunks().stream().allMatch(ownsChunk);
    }

    private static void requireOwned(World world, Region region) throws RegionOperationException {
        if (!isOwned(region, chunk -> Bukkit.isOwnedByCurrentRegion(world, chunk.x(), chunk.z()))) {
            throw failure("worldedit.regen.folia-region");
        }
    }

    private void finish(Job job, @Nullable Clipboard snapshot, @Nullable Throwable error) {
        if (active != job) {
            return;
        }
        active = null;
        if (timeout != null) {
            timeout.cancel();
            timeout = null;
        }
        try {
            job.generation.close();
        } catch (Exception e) {
            if (error == null) {
                error = e;
            } else {
                error.addSuppressed(e);
            }
        }
        complete(job.world, job.region, job.player, job.result, snapshot, error);
    }

    private void complete(World world, Region region, @Nullable Player player, CompletableFuture<Clipboard> result,
                          @Nullable Clipboard snapshot, @Nullable Throwable error) {
        Runnable completion = () -> {
            try {
                if (error != null) {
                    result.completeExceptionally(error);
                } else if (closed || (player != null && player.getWorld() != world)) {
                    result.completeExceptionally(failure("worldedit.regen.cancelled"));
                } else {
                    requireOwned(world, region);
                    result.complete(snapshot);
                }
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        };
        try {
            if (player != null) {
                Runnable retired = () -> result.completeExceptionally(failure("worldedit.regen.cancelled"));
                if (!player.getScheduler().execute(plugin, completion, retired, 1)) {
                    retired.run();
                }
            } else {
                BlockVector2 anchor = region.getChunks().iterator().next();
                Bukkit.getRegionScheduler().execute(plugin, world, anchor.x(), anchor.z(), completion);
            }
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
    }

    private static RegionOperationException failure(String key) {
        return new RegionOperationException(TranslatableComponent.of(key));
    }

    /** Dispose an active temporary world before the plugin's schedulers are cancelled. */
    @Override
    public void close() {
        closed = true;
        if (active != null) {
            Job job = active;
            active = null;
            if (timeout != null) {
                timeout.cancel();
            }
            try {
                job.generation.close();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close the regeneration world.", e);
            } finally {
                job.result.completeExceptionally(failure("worldedit.regen.cancelled"));
            }
        }
        // Generation may be closed already while its result is waiting on an entity scheduler.
        if (!pending.isEmpty()) {
            RegionOperationException cancelled = failure("worldedit.regen.cancelled");
            for (CompletableFuture<Clipboard> result : pending) {
                result.completeExceptionally(cancelled);
            }
        }
    }

    private record Job(BukkitRegeneration<?> generation, CompletableFuture<Clipboard> result,
                       World world, Region region, @Nullable Player player) {
    }
}
