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

package com.sk89q.worldedit.function;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;

import javax.annotation.Nullable;

public class EditContext {

    private Extent destination;
    @Nullable private Region region;
    @Nullable private Pattern fill;
    @Nullable private LocalSession session;

    public Extent getDestination() {
        return destination;
    }

    public void setDestination(Extent destination) {
        checkNotNull(destination, "destination");
        this.destination = destination;
    }

    @Nullable
    public Region getRegion() {
        return region;
    }

    public void setRegion(@Nullable Region region) {
        this.region = region;
    }

    @Nullable
    public Pattern getFill() {
        return fill;
    }

    public void setFill(@Nullable Pattern fill) {
        this.fill = fill;
    }

    @Nullable
    public LocalSession getSession() {
        return session;
    }

    public void setSession(@Nullable LocalSession session) {
        this.session = session;
    }
}
