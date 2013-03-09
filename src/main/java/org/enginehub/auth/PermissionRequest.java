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

import org.enginehub.common.Actor;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.foundation.World;

/**
 * Provides context for an authorization request.
 *
 * <p>This class can be subclassed so that more context can be stored with a request,
 * such as, for example, the affected region in WorldEdit. {@link PermissionProvider}s
 * may choose, or may choose not to, use this extra information.</p>
 */
public class PermissionRequest {

    private Actor<?> subject;
    private String permission;
    private World world;

    /**
     * Create a new request with the given subject, permission, and world.
     * 
     * @param subject the subject
     * @param permission the permission
     * @param world the world
     */
    public PermissionRequest(Actor<?> subject, String permission, LocalWorld world) {
        this.subject = subject;
        this.permission = permission;
        this.world = world;
    }

    /**
     * Create a new request with the given subject and permission, and a null world.
     * 
     * @param subject the subject
     * @param permission the permission
     */
    public PermissionRequest(Actor<?> subject, String permission) {
        this.subject = subject;
        this.permission = permission;
    }

    /**
     * Get the subject of the request.
     * 
     * <p>The subject is the entity for which the request is for.</p>
     * 
     * @return the subject
     */
    public Actor<?> getSubject() {
        return subject;
    }

    /**
     * Set the subject of the request.
     * 
     * @param subject the subject
     */
    public void setSubject(Actor<?> subject) {
        this.subject = subject;
    }

    /**
     * Get the permission string to check.
     * 
     * @return the permission string
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Set the permission string.
     * 
     * @param permission
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Get the world for the permission request.
     * 
     * @return the world, or null
     */
    public World getWorld() {
        return world;
    }

    /**
     * Set the world for the permission request.
     * 
     * @param world the world, or null
     */
    public void setWorld(World world) {
        this.world = world;
    }

}
