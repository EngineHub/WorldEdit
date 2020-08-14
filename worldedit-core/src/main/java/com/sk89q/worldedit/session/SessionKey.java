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

package com.sk89q.worldedit.session;

import com.sk89q.worldedit.util.Identifiable;

import javax.annotation.Nullable;

/**
 * Provides information about a session.
 *
 * <p>A reference for this object may be kept around for a long time.</p>
 */
public interface SessionKey extends Identifiable {

    /**
     * Get the name for this session, if one is available, so that it can
     * be referred to by others.
     *
     * @return a name or {@code null}
     */
    @Nullable
    String getName();

    /**
     * Return whether the session is still active. Sessions that are inactive
     * for a prolonged amount of time may be removed. If this method
     * always returns {@code false}, the the related session may never
     * be stored.
     *
     * <p>This method may be called from any thread, so this call
     * must be thread safe.</p>
     *
     * @return true if active
     */
    boolean isActive();

    /**
     * Return whether this session should be persisted.
     *
     * @return true if persistent
     */
    boolean isPersistent();

}
