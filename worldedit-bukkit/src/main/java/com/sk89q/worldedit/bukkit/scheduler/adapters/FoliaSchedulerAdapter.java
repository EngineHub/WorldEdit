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

package com.sk89q.worldedit.bukkit.scheduler.adapters;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.scheduler.SchedulerAdapter;
import com.sk89q.worldedit.util.Location;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerAdapter implements SchedulerAdapter {
    private final Plugin plugin;
    private final AsyncScheduler asyncScheduler;
    private final RegionScheduler regionScheduler;

    public FoliaSchedulerAdapter(final Plugin plugin) {
        this.plugin = plugin;
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
    }

    @Override
    public void runAsyncRate(final Runnable runnable, final long delay, final long period) {
        asyncScheduler.runAtFixedRate(plugin, task -> runnable.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void executeAtRegion(final Location location, final Runnable runnable) {
        regionScheduler.execute(plugin, BukkitAdapter.adapt(location), runnable);
    }

    @Override
    public void cancelTasks() {
        asyncScheduler.cancelTasks(plugin);
    }

}
