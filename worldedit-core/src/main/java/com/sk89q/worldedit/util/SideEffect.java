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
    LIGHTING(State.ON, true),
    NEIGHBORS(State.ON, false),
    CONNECTIONS(State.ON, false),
    ENTITY_AI(State.OFF, false),
    PLUGIN_EVENTS(State.OFF, false);

    private final String displayName;
    private final String description;
    private final State defaultValue;
    private final boolean requiresCleanup;

    SideEffect(State defaultValue, boolean requiresCleanup) {
        this.displayName = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US);
        this.description = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US) + ".description";
        this.defaultValue = defaultValue;
        this.requiresCleanup = requiresCleanup;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public State getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Whether the world requires cleanup when this is disabled.
     *
     * @return if the world needs a cleanup
     */
    public boolean requiresCleanup() {
        return this.requiresCleanup;
    }

    public enum State {
        OFF,
        ON,
        DELAYED;

        private final String displayName;

        State() {
            this.displayName = "worldedit.sideeffect.state." + this.name().toLowerCase(Locale.US);
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}
