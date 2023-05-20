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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;

public abstract class AbstractCommandBlockActor extends AbstractNonPlayerActor implements Locatable {
    protected static final String UUID_PREFIX = "CMD";

    private final Location location;

    public AbstractCommandBlockActor(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public boolean setLocation(Location location) {
        // Can't move a CommandBlock
        return false;
    }

    @Override
    public Extent getExtent() {
        return this.location.getExtent();
    }
}
