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

import com.sk89q.worldedit.WorldEditPermissionException;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.world.World;

import java.io.File;

/**
 * An object that can perform actions in WorldEdit.
 */
public interface Actor {

    /**
     * Get the name of the actor.
     *
     * @return String
     */
    String getName();

    /**
     * Get the actor's world.
     *
     * @return the world
     */
    World getWorld();

    /**
     * Print a message.
     *
     * @param msg The message text
     */
    void printRaw(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg The message text
     */
    void printDebug(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg The message text
     */
    void print(String msg);

    /**
     * Print a WorldEdit error.
     *
     * @param msg The error message text
     */
    void printError(String msg);

    /**
     * Returns true if the actor can destroy bedrock.
     *
     * @return true if bedrock can be broken by the actor
     */
    boolean canDestroyBedrock();

    /**
     * Get a actor's list of groups.
     *
     * @return an array containing a group name per entry
     */
    String[] getGroups();

    /**
     * Checks if a player has permission.
     *
     * @param perm The permission to check
     * @return true if the player has that permission
     */
    boolean hasPermission(String perm);

    /**
     * Check whether this actor has the given permission, and throw an
     * exception if not.
     *
     * @param permission the permission
     * @throws WorldEditPermissionException thrown if permission is not availabe
     */
    void checkPermission(String permission) throws WorldEditPermissionException;

    /**
     * Return whether this actor is a player.
     *
     * @return true if a player
     */
    boolean isPlayer();

    /**
     * Open a file open dialog.
     *
     * @param extensions null to allow all
     * @return the selected file or null if something went wrong
     */
    File openFileOpenDialog(String[] extensions);

    /**
     * Open a file save dialog.
     *
     * @param extensions null to allow all
     * @return the selected file or null if something went wrong
     */
    File openFileSaveDialog(String[] extensions);

    /**
     * Send a CUI event.
     *
     * @param event the event
     */
    void dispatchCUIEvent(CUIEvent event);

}
