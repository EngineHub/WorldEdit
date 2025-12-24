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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents a scheduler for executing region tasks.
 *
 * <p><b>Note:</b> This implementation is <i>adapted from</i> the
 * <a href="https://github.com/retrooper/packetevents/blob/2.0/spigot/src/main/java/io/github/retrooper/packetevents/util/folia/RegionScheduler.java">
 * packetevents Folia scheduling utilities</a> by retrooper.
 * Adjustments were made for WorldEdit's region execution model and unified task wrapping.
 */
public class RegionScheduler {

    private BukkitScheduler bukkitScheduler;
    private io.papermc.paper.threadedregions.scheduler.RegionScheduler regionScheduler;

    protected RegionScheduler() {
        if (FoliaScheduler.isFolia) {
            regionScheduler = Bukkit.getRegionScheduler();
        } else {
            bukkitScheduler = Bukkit.getScheduler();
        }
    }

    /**
     * Schedules a task to be executed on the region which owns the location.
     *
     * @param plugin The plugin that owns the task
     * @param world  The world of the region that owns the task
     * @param chunkX The chunk X coordinate of the region that owns the task
     * @param chunkZ The chunk Z coordinate of the region that owns the task
     * @param run    The task to execute
     */
    public void execute(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run) {
        if (!FoliaScheduler.isFolia) {
            bukkitScheduler.runTask(plugin, run);
        } else {
            regionScheduler.execute(plugin, world, chunkX, chunkZ, run);
        }
    }

    /**
     * Schedules a task to be executed on the region which owns the location.
     *
     * @param plugin   The plugin that owns the task
     * @param location The location at which the region executing should own
     * @param run      The task to execute
     */
    public void execute(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable run) {
        if (!FoliaScheduler.isFolia) {
            Bukkit.getScheduler().runTask(plugin, run);
        } else {
            regionScheduler.execute(plugin, location, run);
        }
    }

    /**
     * Schedules a task to be executed on the region which owns the location on the next tick.
     *
     * @param plugin The plugin that owns the task
     * @param world  The world of the region that owns the task
     * @param chunkX The chunk X coordinate of the region that owns the task
     * @param chunkZ The chunk Z coordinate of the region that owns the task
     * @param task   The task to execute
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper run(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<Object> task) {
        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(Bukkit.getScheduler().runTask(plugin, () -> task.accept(null)));
        } else {
            return new TaskWrapper(regionScheduler.run(plugin, world, chunkX, chunkZ, (o) -> task.accept(null)));
        }
    }

    /**
     * Schedules a task to be executed on the region which owns the location on the next tick.
     *
     * @param plugin   The plugin that owns the task
     * @param location The location at which the region executing should own
     * @param task     The task to execute
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper run(@NotNull Plugin plugin, @NotNull Location location, @NotNull Consumer<Object> task) {
        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(Bukkit.getScheduler().runTask(plugin, () -> task.accept(null)));
        } else {
            return new TaskWrapper(regionScheduler.run(plugin, location, (o) -> task.accept(null)));
        }
    }

    /**
     * Schedules a task to be executed on the region which owns the location after the specified delay in ticks.
     *
     * @param plugin     The plugin that owns the task
     * @param world      The world of the region that owns the task
     * @param chunkX     The chunk X coordinate of the region that owns the task
     * @param chunkZ     The chunk Z coordinate of the region that owns the task
     * @param task       The task to execute
     * @param delayTicks The delay, in ticks before the method is invoked. Any value less-than 1 is treated as 1.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper runDelayed(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ,
                                  @NotNull Consumer<Object> task, long delayTicks) {
        if (delayTicks < 1) {
            delayTicks = 1;
        }

        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, () -> task.accept(null), delayTicks));
        } else {
            return new TaskWrapper(regionScheduler.runDelayed(plugin, world, chunkX, chunkZ, (o) -> task.accept(null), delayTicks));
        }
    }

    /**
     * Schedules a task to be executed on the region which owns the location after the specified delay in ticks.
     *
     * @param plugin     The plugin that owns the task
     * @param location   The location at which the region executing should own
     * @param task       The task to execute
     * @param delayTicks The delay, in ticks before the method is invoked. Any value less-than 1 is treated as 1.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper runDelayed(@NotNull Plugin plugin, @NotNull Location location,
                                  @NotNull Consumer<Object> task, long delayTicks) {
        if (delayTicks < 1) {
            delayTicks = 1;
        }

        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, () -> task.accept(null), delayTicks));
        } else {
            return new TaskWrapper(regionScheduler.runDelayed(plugin, location, (o) -> task.accept(null), delayTicks));
        }
    }

    /**
     * Schedules a repeating task to be executed on the region which owns the location after the initial delay with the specified period.
     *
     * @param plugin            The plugin that owns the task
     * @param world             The world of the region that owns the task
     * @param chunkX            The chunk X coordinate of the region that owns the task
     * @param chunkZ            The chunk Z coordinate of the region that owns the task
     * @param task              The task to execute
     * @param initialDelayTicks The initial delay, in ticks before the method is invoked. Any value less-than 1 is treated as 1.
     * @param periodTicks       The period, in ticks. Any value less-than 1 is treated as 1.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper runAtFixedRate(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ,
                                      @NotNull Consumer<Object> task, long initialDelayTicks, long periodTicks) {
        if (initialDelayTicks < 1) {
            initialDelayTicks = 1;
        }
        if (periodTicks < 1) {
            periodTicks = 1;
        }

        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null),
                    initialDelayTicks, periodTicks));
        } else {
            return new TaskWrapper(regionScheduler.runAtFixedRate(
                    plugin, world, chunkX, chunkZ, (o) -> task.accept(null), initialDelayTicks, periodTicks));
        }
    }

    /**
     * Schedules a repeating task to be executed on the region which owns the location after the initial delay with the specified period.
     *
     * @param plugin            The plugin that owns the task
     * @param location          The location at which the region executing should own
     * @param task              The task to execute
     * @param initialDelayTicks The initial delay, in ticks before the method is invoked. Any value less-than 1 is treated as 1.
     * @param periodTicks       The period, in ticks. Any value less-than 1 is treated as 1.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper runAtFixedRate(@NotNull Plugin plugin, @NotNull Location location,
                                      @NotNull Consumer<Object> task, long initialDelayTicks, long periodTicks) {
        if (initialDelayTicks < 1) {
            initialDelayTicks = 1;
        }
        if (periodTicks < 1) {
            periodTicks = 1;
        }

        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null),
                    initialDelayTicks, periodTicks));
        } else {
            return new TaskWrapper(regionScheduler.runAtFixedRate(
                    plugin, location, (o) -> task.accept(null), initialDelayTicks, periodTicks));
        }
    }
}
