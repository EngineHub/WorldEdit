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

package com.sk89q.worldedit.event.platform;

import com.sk89q.worldedit.event.Cancellable;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when a block is interacted with.
 */
public class BlockInteractEvent extends Event implements Cancellable {

    private final Actor cause;
    private final Location location;
    private final Interaction type;
    private final Direction face;
    private boolean cancelled;

    /**
     * Create a new event.
     *
     * @param cause the causing actor
     * @param location the location of the block
     * @param type the type of interaction
     */
    @Deprecated
    public BlockInteractEvent(Actor cause, Location location, Interaction type) {
        this(cause, location, null, type);
    }

    /**
     * Create a new event.
     *
     * @param cause the causing actor
     * @param location the location of the block
     * @param face the face of the block that was interacted with
     * @param type the type of interaction
     */
    public BlockInteractEvent(Actor cause, Location location, @Nullable Direction face, Interaction type) {
        checkNotNull(cause);
        checkNotNull(location);
        checkNotNull(type);
        this.cause = cause;
        this.location = location;
        this.face = face;
        this.type = type;
    }

    /**
     * Get the cause of this event.
     *
     * @return the cause
     */
    public Actor getCause() {
        return cause;
    }

    /**
     * Get the location of the block that was interacted with.
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the face of the block that was interacted with.
     *
     * @return The interacted face
     */
    @Nullable
    public Direction getFace() {
        return face;
    }

    /**
     * Get the type of interaction.
     *
     * @return the type of interaction
     */
    public Interaction getType() {
        return type;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
