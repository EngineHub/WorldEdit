// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.auth;

import com.sk89q.worldedit.foundation.World;

/**
 * An object that has permissions assigned to it.
 */
public interface Principal {

    /**
     * Checks if a request for permissions is satisfied.
     *
     * @param request the permissions request
     * @return true if permission is granted
     */
    boolean hasPermission(PermissionRequest request);

    /**
     * Convenience method to check whether an actor has been granted a
     * certain permission.
     *
     * @param permission the permission
     * @return true if permission is granted
     */
    boolean hasPermission(String permission);

    /**
     * Convenience method to check whether an actor has been granted a
     * certain permission.
     *
     * @param permission the permission
     * @param targetWorld the target world that the permission request applies to
     * @return true if permission is granted
     */
    boolean hasPermission(String permission, World targetWorld);

}
