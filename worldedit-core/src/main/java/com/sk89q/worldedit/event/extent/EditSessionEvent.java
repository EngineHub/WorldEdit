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

package com.sk89q.worldedit.event.extent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.TracingExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.EditSession.Stage;

/**
 * Raised (several times) when a new {@link EditSession} is being instantiated.
 *
 * <p></p>Block loggers, as well as block set interceptors, can use this event to wrap
 * the given {@link Extent} with their own, which would allow them to intercept
 * all changes made to the world. For example, the code below would wrap the
 * existing extent with a custom one, and the custom extent would receive
 * all method calls <strong>before</strong> the extent fetched from
 * {@link #getExtent()} would.</p>
 *
 * <pre>
 * event.setExtent(new MyExtent(event.getExtent())
 * </pre>
 *
 * <p></p>This event is fired several times during the creation of a single
 * {@link EditSession}, but {@link #getStage()} will differ each time.
 * The stage determines at which point {@link Extent}s added to this event
 * will be called. For example, if you inject an extent when the stage
 * is set to {@link Stage#BEFORE_HISTORY}, then you can drop (or log) changes
 * before the change has reached the history, reordering, and actual change
 * extents, <em>but</em> that means that any changes made with
 * {@link EditSession#rawSetBlock(BlockVector3, BlockStateHolder)} will skip your
 * custom {@link Extent} because that method bypasses history (and reorder).
 * It is thus recommended that loggers intercept at {@link Stage#BEFORE_CHANGE}
 * and block interceptors intercept at BOTH {@link Stage#BEFORE_CHANGE} and
 * {@link Stage#BEFORE_HISTORY}.</p>
 */
public class EditSessionEvent extends Event {

    private final World world;
    private final Actor actor;
    private final int maxBlocks;
    private final Stage stage;
    private final List<TracingExtent> tracingExtents = new ArrayList<>();
    private Extent extent;
    private boolean tracing;

    /**
     * Create a new event.
     *
     * @param world the world
     * @param actor the actor, or null if there is no actor specified
     * @param maxBlocks the maximum number of block changes
     * @param stage the stage
     */
    public EditSessionEvent(@Nullable World world, Actor actor, int maxBlocks, Stage stage) {
        this.world = world;
        this.actor = actor;
        this.maxBlocks = maxBlocks;
        this.stage = stage;
    }

    /**
     * Get the actor for this event.
     *
     * @return the actor, which may be null if unavailable
     */
    public @Nullable Actor getActor() {
        return actor;
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public @Nullable World getWorld() {
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
        if (tracing && extent != this.extent) {
            TracingExtent tracingExtent = new TracingExtent(extent);
            extent = tracingExtent;
            tracingExtents.add(tracingExtent);
        }
        this.extent = extent;
    }

    /**
     * Set tracing enabled, with the current extent as the "base".
     *
     * <em>Internal use only.</em>
     * @param tracing if tracing is enabled
     */
    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    /**
     * Get the current list of tracing extents.
     *
     * <em>Internal use only.</em>
     */
    public List<TracingExtent> getTracingExtents() {
        return tracingExtents;
    }

    /**
     * Create a clone of this event with the given stage.
     *
     * @param stage the stage
     * @return a new event
     */
    public EditSessionEvent clone(Stage stage) {
        return new EditSessionEvent(world, actor, maxBlocks, stage);
    }

}
