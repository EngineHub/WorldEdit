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
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Represents a scheduler for executing entity tasks.
 *
 * <p>This implementation provides compatibility with both Folia and standard Bukkit scheduling,
 * allowing tasks to be safely executed on entities within their owning regions.
 *
 * <p><b>Origin:</b> This class is adapted from the
 * <a href="https://github.com/retrooper/packetevents/blob/2.0/spigot/src/main/java/io/github/retrooper/packetevents/util/folia/EntityScheduler.java">
 * packetevents Folia EntityScheduler</a> implementation by retrooper.
 * Modifications were made for WorldEdit's internal use and code consistency.
 */
public class EntityScheduler {

    private BukkitScheduler bukkitScheduler;

    protected EntityScheduler() {
        if (!FoliaScheduler.isFolia) {
            bukkitScheduler = Bukkit.getScheduler();
        }
    }

    /**
     * Schedules a task with the given delay.
     *
     * <p>If the task failed to schedule because the scheduler is retired (entity removed), then
     * returns false. Otherwise, either the run callback will be invoked after the specified delay,
     * or the retired callback will be invoked if the scheduler is retired. Note that the retired
     * callback is invoked in critical code, so it should not attempt to remove the entity,
     * remove other entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>It is guaranteed that the run and retired callback are invoked on the region which owns
     * the entity.
     *
     * @param plugin  Plugin which owns the specified task.
     * @param run     The callback to run after the specified delay, may not be null.
     * @param retired Retire callback to run if the entity is retired before the run callback can
     *                be invoked, may be null.
     * @param delay   The delay in ticks before the run callback is invoked.
     */
    public void execute(@NotNull Entity entity, @NotNull Plugin plugin, @NotNull Runnable run,
                        @Nullable Runnable retired, long delay) {
        if (!FoliaScheduler.isFolia) {
            bukkitScheduler.runTaskLater(plugin, run, delay);
        } else {
            entity.getScheduler().execute(plugin, run, retired, delay);
        }
    }

    /**
     * Schedules a task to execute on the next tick.
     *
     * <p>If the task failed to schedule because the scheduler is retired (entity removed), then
     * returns null. Otherwise, either the task callback will be invoked after the specified delay,
     * or the retired callback will be invoked if the scheduler is retired. Note that the retired
     * callback is invoked in critical code, so it should not attempt to remove the entity,
     * remove other entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>It is guaranteed that the task and retired callback are invoked on the region which owns
     * the entity.
     *
     * @param plugin  The plugin that owns the task
     * @param task    The task to execute
     * @param retired Retire callback to run if the entity is retired before the run callback can
     *                be invoked, may be null.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper run(@NotNull Entity entity, @NotNull Plugin plugin,
                           @NotNull Consumer<Object> task, @Nullable Runnable retired) {
        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(bukkitScheduler.runTask(plugin, () -> task.accept(null)));
        } else {
            return new TaskWrapper(entity.getScheduler().run(plugin, (o) -> task.accept(null), retired));
        }
    }

    /**
     * Schedules a task with the given delay.
     *
     * <p>If the task failed to schedule because the scheduler is retired (entity removed),
     * then returns null. Otherwise, either the task callback will be invoked after the specified
     * delay, or the retired callback will be invoked if the scheduler is retired.
     *
     * <p>It is guaranteed that the task and retired callback are invoked on the region which owns
     * the entity.
     *
     * @param plugin     The plugin that owns the task
     * @param task       The task to execute
     * @param retired    Retire callback to run if the entity is retired before the run callback
     *                   can be invoked, may be null.
     * @param delayTicks The delay in ticks before the run callback is invoked. Any value less
     *                   than 1 is treated as 1.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper runDelayed(@NotNull Entity entity, @NotNull Plugin plugin,
                                  @NotNull Consumer<Object> task, @Nullable Runnable retired,
                                  long delayTicks) {
        if (delayTicks < 1) {
            delayTicks = 1;
        }

        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(bukkitScheduler.runTaskLater(plugin, () -> task.accept(null), delayTicks));
        } else {
            return new TaskWrapper(entity.getScheduler()
                    .runDelayed(plugin, (o) -> task.accept(null), retired, delayTicks));
        }
    }

    /**
     * Schedules a repeating task with the given delay and period.
     *
     * <p>If the task failed to schedule because the scheduler is retired (entity removed),
     * then returns null. Otherwise, either the task callback will be invoked after the specified
     * delay, or the retired callback will be invoked if the scheduler is retired.
     *
     * <p>It is guaranteed that the task and retired callback are invoked on the region which owns
     * the entity.
     *
     * @param plugin            The plugin that owns the task
     * @param task              The task to execute
     * @param retired           Retire callback to run if the entity is retired before the run
     *                          callback can be invoked, may be null.
     * @param initialDelayTicks The initial delay, in ticks before the method is invoked. Any
     *                          value less-than 1 is treated as 1.
     * @param periodTicks       The period, in ticks. Any value less-than 1 is treated as 1.
     * @return {@link TaskWrapper} instance representing a wrapped task
     */
    public TaskWrapper runAtFixedRate(@NotNull Entity entity, @NotNull Plugin plugin,
                                      @NotNull Consumer<Object> task, @Nullable Runnable retired,
                                      long initialDelayTicks, long periodTicks) {
        if (initialDelayTicks < 1) {
            initialDelayTicks = 1;
        }
        if (periodTicks < 1) {
            periodTicks = 1;
        }

        if (!FoliaScheduler.isFolia) {
            return new TaskWrapper(bukkitScheduler.runTaskTimer(plugin, () -> task.accept(null),
                    initialDelayTicks, periodTicks));
        } else {
            return new TaskWrapper(entity.getScheduler()
                    .runAtFixedRate(plugin, (o) -> task.accept(null), retired,
                            initialDelayTicks, periodTicks));
        }
    }
}
