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

package com.sk89q.worldedit.util;

import java.util.Locale;

public enum SideEffect {
    LIGHTING(false, true),
    NEIGHBORS(false, true),
    CONNECTIONS(false, true),
    ENTITY_AI(false, true),
    PLUGIN_EVENTS(false, false);

    private String displayName;
    private String description;
    private boolean dirty;
    private boolean configurable;

    SideEffect(boolean dirty, boolean configurable) {
        this.displayName = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US);
        this.description = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US) + ".description";
        this.dirty = dirty;
        this.configurable = configurable;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean requiresCleanup() {
        return this.dirty;
    }

    public boolean isConfigurable() {
        return this.configurable;
    }
}
