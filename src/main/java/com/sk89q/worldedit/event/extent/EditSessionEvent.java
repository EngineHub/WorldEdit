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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.event.extent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extent.Extent;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Raised when a new {@link EditSession} is being instantiated.
 * </p>
 * Block loggers, as well as block set interceptors, can use this event to wrap
 * the given {@link Extent} with their own, which would allow them to intercept
 * all changes made to the world.
 */
public class EditSessionEvent extends Event {

    private final LocalWorld world;
    private final LocalPlayer player;
    private final int maxBlocks;
    private Extent extent;

    /**
     * Create a new event.
     *
     * @param player the player, or null if not available
     * @param world the world
     * @param maxBlocks the maximum number of block changes
     */
    public EditSessionEvent(LocalWorld world, LocalPlayer player, int maxBlocks) {
        checkNotNull(world);
        this.world = world;
        this.player = player;
        this.maxBlocks = maxBlocks;
    }

    /**
     * Get the player for this event.
     *
     * @return the player, which may be null if unavailable
     */
    public @Nullable LocalPlayer getPlayer() {
        return player;
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public LocalWorld getWorld() {
        return world;
    }

    /**
     * Get the maximum number of blocks that may be set.
     *
     * @return the maximum number of blocks, which is -1 if unlimited
     */
    public int getMaxBlocks() {
        return maxBlocks;
    }

    /**
     * Get the {@link Extent} that can be wrapped.
     *
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set a new extent that should be used. It should wrap the extent
     * returned from {@link #getExtent()}.
     *
     * @param extent the extent
     */
    public void setExtent(Extent extent) {
        checkNotNull(extent);
        this.extent = extent;
    }
}
