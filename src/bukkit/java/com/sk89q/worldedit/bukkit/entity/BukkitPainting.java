/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * @author zml2008
 */
public class BukkitPainting extends BukkitEntity {
    private static int spawnTask = -1;
    private static final Deque<QueuedPaintingSpawn> spawnQueue = new ArrayDeque<QueuedPaintingSpawn>();

    private class QueuedPaintingSpawn {
        private final Location weLoc;

        public QueuedPaintingSpawn(Location weLoc) {
            this.weLoc = weLoc;
        }

        public void spawn() {
            spawnRaw(weLoc);
        }
    }
    private static class PaintingSpawnRunnable implements Runnable {
        @Override
        public void run() {
            synchronized (spawnQueue) {
                QueuedPaintingSpawn spawn;
                while ((spawn = spawnQueue.poll()) != null) {
                    try {
                        spawn.spawn();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        continue;
                    }
                }
                spawnTask = -1;
            }
        }
    }

    private final Art art;
    private final BlockFace facingDirection;
    public BukkitPainting(Location loc, Art art, BlockFace facingDirection, UUID entityId) {
        super(loc, EntityType.PAINTING, entityId);
        this.art = art;
        this.facingDirection = facingDirection;
    }

    /**
     * Queue the painting to be spawned at the specified location.
     * This operation is delayed so that the block changes that may be applied can be applied before the painting spawn is attempted.
     *
     * @param weLoc The WorldEdit location
     * @return Whether the spawn as successful
     */
    public boolean spawn(Location weLoc) {
        synchronized (spawnQueue) {
            spawnQueue.add(new QueuedPaintingSpawn(weLoc));
            if (spawnTask == -1) {
                spawnTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit"), new PaintingSpawnRunnable(), 1L);
            }
        }
        return true;
    }

    public boolean spawnRaw(Location weLoc) {
        org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
        Painting paint = loc.getWorld().spawn(loc, Painting.class);
        if (paint != null) {
            paint.setFacingDirection(facingDirection, true);
            paint.setArt(art, true);
            return true;
        }
        return false;
    }
}
