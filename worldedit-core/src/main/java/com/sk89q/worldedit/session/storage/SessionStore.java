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

package com.sk89q.worldedit.session.storage;

import com.sk89q.worldedit.LocalSession;

import java.io.IOException;
import java.util.UUID;

/**
 * Commits sessions to disk.
 *
 * <p>Both {@link #load(UUID)} and {@link #save(UUID, LocalSession)} may be
 * called at the same in different threads, so implementations should
 * be aware of this issue.</p>
 */
public interface SessionStore {

    /**
     * Load a session identified by the given UUID.
     *
     * <p>If the session does not exist (has never been saved), then
     * a new {@link LocalSession} must be returned.</p>
     *
     * @param id the UUID
     * @return a session
     * @throws IOException thrown on read error
     */
    LocalSession load(UUID id) throws IOException;

    /**
     * Save the given session identified by the given UUID.
     *
     * @param id the UUID
     * @param session a session
     * @throws IOException thrown on read error
     */
    void save(UUID id, LocalSession session) throws IOException;

}
