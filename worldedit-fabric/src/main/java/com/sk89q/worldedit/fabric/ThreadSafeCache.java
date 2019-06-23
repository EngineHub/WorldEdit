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

package com.sk89q.worldedit.fabric;

import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Caches data that cannot be accessed from another thread safely.
 */
public class ThreadSafeCache implements ServerTickCallback {

    private static final long REFRESH_DELAY = 1000 * 30;
    private static final ThreadSafeCache INSTANCE = new ThreadSafeCache();
    private Set<UUID> onlineIds = Collections.emptySet();
    private long lastRefresh = 0;

    /**
     * Get an concurrent-safe set of UUIDs of online players.
     *
     * @return a set of UUIDs
     */
    public Set<UUID> getOnlineIds() {
        return onlineIds;
    }

    @Override
    public void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        if (now - lastRefresh > REFRESH_DELAY) {
            Set<UUID> onlineIds = new HashSet<>();

            if (server == null) {
                return;
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != null) {
                    onlineIds.add(player.getUuid());
                }
            }

            this.onlineIds = new CopyOnWriteArraySet<>(onlineIds);

            lastRefresh = now;
        }
    }

    public static ThreadSafeCache getInstance() {
        return INSTANCE;
    }
}
