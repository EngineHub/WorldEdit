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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extent.Extent;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.EditSession.Stage;

/**
 * Raised (several times) when a new {@link EditSession} is being instantiated.
 * </p>
 * Block loggers, as well as block set interceptors, can use this event to wrap
 * the given {@link Extent} with their own, which would allow them to intercept
 * all changes made to the world. For example, the code below would wrap the
 * existing extent with a custom one, and the custom extent would receive
 * all method calls <strong>before</strong> the extent fetched from
 * {@link #getExtent()} would.
 * <pre>
 * event.setExtent(new MyExtent(event.getExtent())
 * </pre>
 * This event is fired several times during the creation of a single
 * {@link EditSession}, but {@link #getStage()} will differ each time.
 * The stage determines at which point {@link Extent}s added to this event
 * will be called. For example, if you inject an extent when the stage
 * is set to {@link Stage#BEFORE_HISTORY}, then you can drop (or log) changes
 * before the change has reached the history, reordering, and actual change
 * extents, <em>but</em> that means that any changes made with
 * {@link EditSession#rawSetBlock(Vector, BaseBlock)} will skip your
 * custom {@link Extent} because that method bypasses history (and reorder).
 * It is thus recommended that loggers intercept at {@link Stage#BEFORE_CHANGE}
 * and block interceptors intercept at BOTH {@link Stage#BEFORE_CHANGE} and
 * {@link Stage#BEFORE_HISTORY}.
 */
public class EditSessionEvent extends Event {

    private final LocalWorld world;
    private final LocalPlayer player;
    private final int maxBlocks;
    private final Stage stage;
    private Extent extent;

    /**
     * Create a new event.
     *
     * @param world the world
     * @param player the player, or null if not available
     * @param maxBlocks the maximum number of block changes
     * @param stage the stage
     */
    public EditSessionEvent(LocalWorld world, LocalPlayer player, int maxBlocks, Stage stage) {
        checkNotNull(world);
        this.world = world;
        this.player = player;
        this.maxBlocks = maxBlocks;
        this.stage = stage;
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
     * Get the stage that is being wrapped.
     *
     * @return the stage
     */
    public Stage getStage() {
        return stage;
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

    /**
     * Create a clone of this event with the given stage.
     *
     * @param stage the stage
     * @return a new event
     */
    public EditSessionEvent clone(Stage stage) {
        return new EditSessionEvent(world, player, maxBlocks, stage);
    }

}
