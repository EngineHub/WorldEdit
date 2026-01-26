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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to handle scheduling tasks.
 *
 * <p>This class uses Paper's threaded-region schedulers when running on Folia,
 * otherwise it falls back to the standard Bukkit scheduler.
 *
 * <p><b>Note:</b> This implementation is <i>adapted from</i> the
 * <a href="https://github.com/retrooper/packetevents/blob/2.0/spigot/src/main/java/io/github/retrooper/packetevents/util/folia/FoliaScheduler.java">
 * packetevents Folia scheduling utilities</a> by retrooper.
 * Adjustments were made to fit WorldEdit’s initialization flow and scheduling abstractions.
 */
public class FoliaScheduler {

    static final boolean isFolia;
    private static Class<? extends Event> regionizedServerInitEventClass;

    private static AsyncScheduler asyncScheduler;
    private static EntityScheduler entityScheduler;
    private static GlobalRegionScheduler globalRegionScheduler;
    private static RegionScheduler regionScheduler;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;

            Class<?> raw = Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            if (!Event.class.isAssignableFrom(raw)) {
                throw new ClassNotFoundException("RegionizedServerInitEvent does not extend Event");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) raw;
            regionizedServerInitEventClass = eventClass;
        } catch (ClassNotFoundException e) {
            folia = false;
        }

        isFolia = folia;
    }

    /**
     * Checks whether the server is running Folia.
     *
     * @return Whether the server is running Folia
     */
    public static boolean isFolia() {
        return isFolia;
    }

    /**
     * Returns the async scheduler.
     *
     * @return async scheduler instance of {@link AsyncScheduler}
     */
    public static AsyncScheduler getAsyncScheduler() {
        if (asyncScheduler == null) {
            asyncScheduler = new AsyncScheduler();
        }
        return asyncScheduler;
    }

    /**
     * Returns the entity scheduler.
     *
     * @return entity scheduler instance of {@link EntityScheduler}
     */
    public static EntityScheduler getEntityScheduler() {
        if (entityScheduler == null) {
            entityScheduler = new EntityScheduler();
        }
        return entityScheduler;
    }

    /**
     * Returns the global region scheduler.
     *
     * @return global region scheduler instance of {@link GlobalRegionScheduler}
     */
    public static GlobalRegionScheduler getGlobalRegionScheduler() {
        if (globalRegionScheduler == null) {
            globalRegionScheduler = new GlobalRegionScheduler();
        }
        return globalRegionScheduler;
    }

    /**
     * Returns the region scheduler.
     *
     * @return region scheduler instance of {@link RegionScheduler}
     */
    public static RegionScheduler getRegionScheduler() {
        if (regionScheduler == null) {
            regionScheduler = new RegionScheduler();
        }
        return regionScheduler;
    }

    /**
     * Run a task after the server has finished initializing.
     *
     * <p>Undefined behavior if called after the server has finished initializing.
     * We still need to use reflection to get the server init event class,
     * as this is only part of the Folia API.
     *
     * @param plugin The plugin owning this task
     * @param run    The task to run
     */
    public static void runTaskOnInit(Plugin plugin, Runnable run) {
        if (!isFolia) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run);
        } else {
            Bukkit.getServer().getPluginManager().registerEvent(
                regionizedServerInitEventClass,
                new Listener() { },
                EventPriority.HIGHEST,
                (listener, event) -> run.run(),
                plugin
            );
        }
    }
}
