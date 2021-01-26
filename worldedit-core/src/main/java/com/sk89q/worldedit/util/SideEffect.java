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

package com.sk89q.worldedit.util;

import java.util.Locale;

public enum SideEffect {
    LIGHTING(State.ON, true),
    NEIGHBORS(State.ON, true),
    UPDATE(State.ON, true),
    VALIDATION(State.OFF, true),
    ENTITY_AI(State.OFF, true),
    EVENTS(State.OFF, true),
    /**
     * Internal use only.
     */
    POI_UPDATE(State.ON, false),
    /**
     * Internal use only.
     */
    NETWORK(State.ON, false);

    // TODO Make these components in WE8
    private final String displayName;
    private final String description;
    private final State defaultValue;
    private final boolean exposed;

    SideEffect(State defaultValue, boolean exposed) {
        this.displayName = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US);
        this.description = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US) + ".description";
        this.defaultValue = defaultValue;
        this.exposed = exposed;
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
     * Determines if this side effect is considered API.
     *
     * @return if the side effect is exposed via API
     */
    public boolean isExposed() {
        return exposed;
    }

    public enum State {
        OFF,
        ON,
        DELAYED;

        // TODO Make this a component in WE8
        private final String displayName;

        State() {
            this.displayName = "worldedit.sideeffect.state." + this.name().toLowerCase(Locale.US);
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}
