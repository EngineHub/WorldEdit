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

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.event.AbstractCancellable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is currently only for internal use. Do not post or catch this event.
 */
public class CommandEvent extends AbstractCancellable {

    private final LocalPlayer player;
    private final String[] args;

    /**
     * Create a new instance.
     *
     * @param player the player
     * @param args the arguments
     */
    public CommandEvent(LocalPlayer player, String[] args) {
        checkNotNull(player);
        checkNotNull(args);

        this.player = player;
        this.args = args;
    }

    /**
     * Get the player.
     *
     * @return the player
     */
    public LocalPlayer getPlayer() {
        return player;
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public String[] getArguments() {
        return args;
    }

}
