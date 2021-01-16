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

package com.sk89q.worldedit.event.platform;

import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.session.SessionKey;

/**
 * An event fired when a session becomes idle.
 *
 * <p>This can happen when a player leaves the server.</p>
 */
public final class SessionIdleEvent extends Event {
    private final SessionKey key;

    public SessionIdleEvent(SessionKey key) {
        this.key = key;
    }

    /**
     * Get a key identifying the session that has become idle.
     *
     * @return the key for the session
     */
    public SessionKey getKey() {
        return this.key;
    }
}
