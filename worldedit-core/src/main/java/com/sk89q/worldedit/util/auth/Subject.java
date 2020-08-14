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

package com.sk89q.worldedit.util.auth;

/**
 * A subject has authorization attached to it.
 */
public interface Subject {

    /**
     * Get a list of groups that this subject is a part of.
     *
     * @return an array containing a group name per entry
     */
    String[] getGroups();

    /**
     * Check whether this subject has been granted the given permission
     * and throw an exception on error.
     *
     * @param permission the permission
     * @throws AuthorizationException thrown if not permitted
     */
    void checkPermission(String permission) throws AuthorizationException;

    /**
     * Return whether this subject has the given permission.
     *
     * @param permission the permission
     * @return true if permission is granted
     */
    boolean hasPermission(String permission);

}
