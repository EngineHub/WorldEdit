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

import com.sk89q.worldedit.LocalWorld;
import org.enginehub.common.Actor;

/**
 * Provides context for an authorization request.
 *
 * <p>This class can be subclassed so that more context can be stored with a request,
 * such as, for example, the affected region in Worldedit. {@link PermissionProvider}s
 * may choose, or may choose not to, use this extra information.</p>
 */
public class PermissionRequest {

    private Actor subject;
    private String permission;
    private LocalWorld world;

    public PermissionRequest(Actor subject, String permission, LocalWorld world) {
        this.subject = subject;
        this.permission = permission;
        this.world = world;
    }

    public PermissionRequest(Actor subject, String permission) {
        this.subject = subject;
        this.permission = permission;
    }

    public Actor getSubject() {
        return subject;
    }

    public void setSubject(Actor subject) {
        this.subject = subject;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public LocalWorld getWorld() {
        return world;
    }

    public void setWorld(LocalWorld world) {
        this.world = world;
    }

}
