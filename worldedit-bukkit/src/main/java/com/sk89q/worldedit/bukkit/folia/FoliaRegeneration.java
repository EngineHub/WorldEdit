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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;

/**
 * Runs {@code //regen} on Folia, where terrain generation cannot be awaited on a region thread.
 *
 * <p>The selection must belong to the caller's region when the command runs, and to a single region
 * when the result is applied. The adapter generates the terrain into a detached snapshot, the snapshot
 * is written to the extent on the thread owning the selection, and the returned future completes on
 * the actor's own scheduler.</p>
 */
public final class FoliaRegeneration {

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
     * @param actor the actor that requested the regeneration
     * @return a future completing with {@code true} on the actor's scheduler, or exceptionally on failure
     */
    public static CompletionStage<Boolean> regenerate(WorldEditPlugin plugin, BukkitImplAdapter adapter,
                                                      BukkitWorld world, Region region, Extent extent,
                                                      RegenOptions options, Actor actor) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        World bukkitWorld = world.getWorld();
        try {
            if (extent instanceof EditSession editSession && editSession.getBlockBag() != null) {
                // The block bag reads the player's inventory, which may be in another region by then.
                throw new RegionOperationException(TranslatableComponent.of("worldedit.regen.inventory-unsupported"));
            }
            checkOwned(bukkitWorld, region);
            BlockArrayClipboard snapshot = new BlockArrayClipboard(region);
            adapter.regenerateAsync(bukkitWorld, region, snapshot, options).whenComplete((_, error) -> {
                BlockVector3 anchor = region.getMinimumPoint();
                Bukkit.getRegionScheduler().execute(plugin, bukkitWorld, anchor.x() >> 4, anchor.z() >> 4, () -> {
                    Throwable failure = error;
                    if (failure == null) {
                        try {
                            apply(world, region, snapshot, extent, options);
                        } catch (Exception e) {
                            failure = e;
                        }
                    }
                    complete(plugin, actor, result, failure);
                });
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static void checkOwned(World world, Region region) throws RegionOperationException {
        for (BlockVector2 chunk : region.getChunks()) {
            if (!Bukkit.isOwnedByCurrentRegion(world, chunk.x(), chunk.z())) {
                throw new RegionOperationException(TranslatableComponent.of("worldedit.regen.folia-region"));
            }
        }
    }

    private static void apply(BukkitWorld world, Region region, BlockArrayClipboard snapshot, Extent extent,
                              RegenOptions options) throws WorldEditException {
        checkOwned(world.getWorld(), region);
        try {
            // Masks parsed without an extent, such as the global mask, read it from the current request.
            Request.runWithRequest(() -> {
                Request.request().setWorld(world);
                if (extent instanceof EditSession editSession) {
                    Request.request().setEditSession(editSession);
                }
                try {
                    for (BlockVector3 position : region) {
                        extent.setBlock(position, snapshot.getFullBlock(position));
                        if (options.shouldRegenBiomes()) {
                            extent.setBiome(position, snapshot.getBiome(position));
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

    private static void complete(WorldEditPlugin plugin, Actor actor, CompletableFuture<Boolean> result,
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
            Bukkit.getGlobalRegionScheduler().execute(plugin, completion);
        }
    }
}
