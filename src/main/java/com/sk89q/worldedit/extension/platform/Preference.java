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

package com.sk89q.worldedit.extension.platform;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indicates the preference of a platform for a particular
 * {@link Capability}.
 */
public enum Preference {

    /**
     * Indicates that the platform should be preferred for a given capability.
     */
    PREFERRED,

    /**
     * Indicates that preference for a platform is neutral for a given
     * capability.
     */
    NORMAL,

    /**
     * Indicates that there should not be a preference for the platform for
     * a given capability.
     */
    PREFER_OTHERS;

    /**
     * Returns whether this given preference is preferred over the given
     * other preference.
     *
     * @param other the other preference
     * @return true if this preference is greater
     */
    public boolean isPreferredOver(Preference other) {
        checkNotNull(other);
        return ordinal() < other.ordinal();
    }

}
