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

package com.sk89q.worldedit.session.request;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

/**
 * Describes the current request using a {@link ThreadLocal}.
 */
public final class Request {

    private static final ThreadLocal<Request> threadLocal = ThreadLocal.withInitial(Request::new);

    private @Nullable World world;
    private @Nullable LocalSession session;
    private @Nullable EditSession editSession;
    private boolean valid;

    private Request() {
    }

    /**
     * Get the request world.
     *
     * @return the world, which may be null
     */
    public @Nullable World getWorld() {
        return world;
    }

    /**
     * Set the request world.
     *
     * @param world the world, which may be null
     */
    public void setWorld(@Nullable World world) {
        this.world = world;
    }

    /**
     * Get the request session.
     *
     * @return the session, which may be null
     */
    public @Nullable LocalSession getSession() {
        return session;
    }

    /**
     * Get the request session.
     *
     * @param session the session, which may be null
     */
    public void setSession(@Nullable LocalSession session) {
        this.session = session;
    }

    /**
     * Get the {@link EditSession}.
     *
     * @return the edit session, which may be null
     */
    public @Nullable EditSession getEditSession() {
        return editSession;
    }

    /**
     * Set the {@link EditSession}.
     *
     * @param editSession the edit session, which may be null
     */
    public void setEditSession(@Nullable EditSession editSession) {
        this.editSession = editSession;
    }

    /**
     * Get the current request, which is specific to the current thread.
     *
     * @return the current request
     */
    public static Request request() {
        return threadLocal.get();
    }

    /**
     * Reset the current request and clear all fields.
     */
    public static void reset() {
        request().invalidate();
        threadLocal.remove();
    }

    /**
     * Check if the current request object is still valid. Invalid requests may contain outdated values.
     *
     * @return true if the request is valid
     */
    public boolean isValid() {
        return valid;
    }

    private void invalidate() {
        valid = false;
    }
}
