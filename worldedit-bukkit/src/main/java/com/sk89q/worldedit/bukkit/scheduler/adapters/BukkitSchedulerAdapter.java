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

import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.scheduler.SchedulerAdapter;
import com.sk89q.worldedit.util.Location;
import org.bukkit.plugin.Plugin;

public class BukkitSchedulerAdapter implements SchedulerAdapter {

    private final Plugin plugin;
    @SuppressWarnings("deprecation")
    private final org.bukkit.scheduler.BukkitScheduler scheduler;

    public BukkitSchedulerAdapter(final Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void runAsyncRate(Runnable runnable, long delay, long period) {
        scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    @Override
    public void executeAtEntity(Entity entity, Runnable runnable) {
        scheduler.runTask(plugin, runnable);
    }

    @Override
    public void runAtEntityDelayed(final Entity entity, final Runnable runnable, final long delay) {
        scheduler.runTaskLater(plugin, runnable, delay);
    }

    @Override
    public void executeAtRegion(Location location, Runnable runnable) {
        scheduler.runTask(plugin, runnable);
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelTasks(plugin);
    }
}