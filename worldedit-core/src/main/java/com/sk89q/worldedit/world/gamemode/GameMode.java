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

package com.sk89q.worldedit.world.gamemode;

import com.sk89q.worldedit.registry.Registry;

public class GameMode {

    public static final Registry<GameMode> REGISTRY = new Registry<>("game mode");

    private String id;

    public GameMode(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Gets the name of this game mode, or the ID if the name cannot be found.
     *
     * @return The name, or ID
     */
    public String getName() {
        return getId();
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GameMode && this.id.equals(((GameMode) obj).id);
    }

}
