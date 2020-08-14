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

package com.sk89q.worldedit;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

/**
 * A builder-style factory for {@link EditSession EditSessions}.
 */
public final class EditSessionBuilder {

    private final EventBus eventBus;
    private @Nullable World world;
    private int maxBlocks = -1;
    private @Nullable Actor actor;
    private @Nullable BlockBag blockBag;
    private boolean tracing;

    EditSessionBuilder(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public @Nullable World getWorld() {
        return world;
    }

    /**
     * Set the world for the {@link EditSession}.
     *
     * @param world the world
     * @return this builder
     */
    public EditSessionBuilder world(@Nullable World world) {
        this.world = world;
        return this;
    }

    public int getMaxBlocks() {
        return maxBlocks;
    }

    /**
     * Set the maximum blocks to change for the {@link EditSession}.
     *
     * @param maxBlocks the maximum blocks to change
     * @return this builder
     */
    public EditSessionBuilder maxBlocks(int maxBlocks) {
        this.maxBlocks = maxBlocks;
        return this;
    }

    public @Nullable Actor getActor() {
        return actor;
    }

    /**
     * Set the actor who owns the {@link EditSession}.
     *
     * @param actor the actor
     * @return this builder
     */
    public EditSessionBuilder actor(@Nullable Actor actor) {
        this.actor = actor;
        return this;
    }

    public @Nullable BlockBag getBlockBag() {
        return blockBag;
    }

    /**
     * Set the block bag for the {@link EditSession}.
     *
     * @param blockBag the block bag
     * @return this builder
     */
    public EditSessionBuilder blockBag(@Nullable BlockBag blockBag) {
        this.blockBag = blockBag;
        return this;
    }

    /**
     * Check if tracing is enabled.
     *
     * <em>Internal use only.</em>
     */
    public boolean isTracing() {
        return tracing;
    }

    /**
     * Set tracing enabled/disabled.
     *
     * <em>Internal use only.</em>
     */
    public EditSessionBuilder tracing(boolean tracing) {
        this.tracing = tracing;
        return this;
    }

    // Extended methods
    public <A extends Actor & Locatable> EditSessionBuilder locatableActor(A locatable) {
        Extent extent = locatable.getExtent();
        Preconditions.checkArgument(extent instanceof World, "%s is not located in a World", locatable);
        return world(((World) extent)).actor(locatable);
    }

    /**
     * Build the {@link EditSession} using properties described in this builder.
     *
     * @return the new EditSession
     */
    public EditSession build() {
        if (WorldEdit.getInstance().getConfiguration().traceUnflushedSessions) {
            return new TracedEditSession(eventBus, world, maxBlocks, blockBag, actor, tracing);
        }
        return new EditSession(eventBus, world, maxBlocks, blockBag, actor, tracing);
    }
}
