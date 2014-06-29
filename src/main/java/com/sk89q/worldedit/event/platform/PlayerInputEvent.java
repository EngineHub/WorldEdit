/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.event.platform;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.Cancellable;
import com.sk89q.worldedit.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Raised whenever a player sends input.
 */
public class PlayerInputEvent extends Event implements Cancellable {

    private final Player player;
    private final InputType inputType;
    private boolean cancelled;

    /**
     * Create a new event.
     *
     * @param player the player
     * @param inputType the input type
     */
    public PlayerInputEvent(Player player, InputType inputType) {
        checkNotNull(player);
        checkNotNull(inputType);
        this.player = player;
        this.inputType = inputType;
    }

    /**
     * Get the player that sent the input.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the type of input sent.
     *
     * @return the input sent
     */
    public InputType getInputType() {
        return inputType;
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
