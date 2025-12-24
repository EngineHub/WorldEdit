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

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a wrapper around {@code BukkitTask} and Paper's {@code ScheduledTask}.
 * This class provides a unified interface for interacting with both Bukkit's task scheduler
 * and Paper's threaded-region task scheduler.
 *
 * <p><b>Note:</b> This implementation is <i>adapted from</i> the
 * <a href="https://github.com/retrooper/packetevents/blob/2.0/spigot/src/main/java/io/github/retrooper/packetevents/util/folia/TaskWrapper.java">
 * packetevents Folia scheduling utilities</a> by retrooper.
 * It was refactored for WorldEdit’s API consistency and internal task abstraction layer.
 */
public class TaskWrapper {

    private BukkitTask bukkitTask;
    private ScheduledTask scheduledTask;

    /**
     * Constructs a new TaskWrapper around a BukkitTask.
     *
     * @param bukkitTask the BukkitTask to wrap
     */
    public TaskWrapper(@NotNull BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    /**
     * Constructs a new TaskWrapper around Paper's ScheduledTask.
     *
     * @param scheduledTask the ScheduledTask to wrap
     */
    public TaskWrapper(@NotNull ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    /**
     * Retrieves the Plugin that owns this task.
     *
     * @return the owning {@link Plugin}
     */
    public Plugin getOwner() {
        return bukkitTask != null ? bukkitTask.getOwner() : scheduledTask.getOwningPlugin();
    }

    /**
     * Checks if the task is canceled.
     *
     * @return true if the task is canceled, false otherwise
     */
    public boolean isCancelled() {
        return bukkitTask != null ? bukkitTask.isCancelled() : scheduledTask.isCancelled();
    }

    /**
     * Cancels the task. If the task is running, it will be canceled.
     */
    public void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        } else {
            scheduledTask.cancel();
        }
    }

    /**
     * Gets the task ID for this task.
     *
     * @return the task ID
     */
    public int getTaskId() {
        if (bukkitTask != null) {
            return bukkitTask.getTaskId();
        } else {
            return scheduledTask.hashCode();
        }
    }
}
