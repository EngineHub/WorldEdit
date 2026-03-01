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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.folia.FoliaScheduler;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.entity.metadata.EntitySchedulerFacet;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to adapt a Bukkit entity into a WorldEdit one.
 */
class BukkitEntity implements Entity {

    private final WeakReference<org.bukkit.entity.Entity> entityRef;

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    BukkitEntity(org.bukkit.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public Extent getExtent() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return BukkitAdapter.adapt(entity.getWorld());
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public Location getLocation() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return BukkitAdapter.adapt(entity.getLocation());
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity == null) {
            return false;
        }

        if (PaperLib.isPaper()) {
            @SuppressWarnings({"FutureReturnValueIgnored", "unused"})
            var unused = FoliaScheduler.getEntityScheduler().run(
                entity,
                WorldEditPlugin.getInstance(),
                o -> entity.teleportAsync(BukkitAdapter.adapt(location)),
                null
            );
            return true;
        }
        return entity.teleport(BukkitAdapter.adapt(location));
    }

    @Override
    public BaseEntity getState() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity == null || entity instanceof Player) {
            return null;
        }

        BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        if (adapter == null) {
            return null;
        }

        try {
            var loc = entity.getLocation();
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (FoliaScheduler.isFolia()) {
                if (Bukkit.isOwnedByCurrentRegion(loc.getWorld(), cx, cz)) {
                    return adapter.getEntity(entity);
                }
            } else {
                return adapter.getEntity(entity);
            }
        } catch (Throwable ignored) {
            // It's fine if we couldn't remove it
        }

        CompletableFuture<BaseEntity> future = new CompletableFuture<>();
        try {
            FoliaScheduler.getEntityScheduler().run(
                entity,
                WorldEditPlugin.getInstance(),
                task -> {
                    try {
                        BaseEntity result = adapter.getEntity(entity);
                        future.complete(result);
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                },
                null
            );
            try {
                return future.get();
            } catch (Exception e) {
                return null;
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Remove this entity safely, using region/main-thread scheduling as appropriate.
     *
     * @return true if removal was scheduled or completed successfully
     */
    @Override
    public boolean remove() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity == null) {
            return true;
        }

        try {
            entity.remove();
            return entity.isDead();
        } catch (Throwable offThread) {
            try {
                FoliaScheduler.getEntityScheduler().run(
                    entity,
                    WorldEditPlugin.getInstance(),
                    scheduledTask -> {
                        try {
                            entity.remove();
                        } catch (UnsupportedOperationException ignored) {
                            // Some entities may refuse removal
                        }
                    },
                    null
                );
                return true;
            } catch (UnsupportedOperationException e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity == null) {
            return null;
        }

        if (EntityProperties.class.isAssignableFrom(cls)) {
            return (T) new BukkitEntityProperties(entity);
        }

        if (EntitySchedulerFacet.class.isAssignableFrom(cls)) {
            return (T) (EntitySchedulerFacet) task ->
                FoliaScheduler.getEntityScheduler().run(
                    entity,
                    WorldEditPlugin.getInstance(),
                    scheduledTask -> {
                        try {
                            task.run();
                        } catch (Throwable ignored) {
                            // It's fine if we couldn't remove it
                        }
                    },
                    null
                );
        }

        return null;
    }
}
