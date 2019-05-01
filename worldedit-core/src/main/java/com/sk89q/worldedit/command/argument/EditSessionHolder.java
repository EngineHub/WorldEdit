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

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;

import java.util.concurrent.locks.StampedLock;

/**
 * Lazily-created {@link EditSession}.
 */
public class EditSessionHolder {

    private final StampedLock lock = new StampedLock();
    private final WorldEdit worldEdit;
    private final Player player;

    public EditSessionHolder(WorldEdit worldEdit, Player player) {
        this.worldEdit = worldEdit;
        this.player = player;
    }

    private EditSession session;

    /**
     * Get the session, but does not create it if it doesn't exist.
     */
    public EditSession getSession() {
        long stamp = lock.tryOptimisticRead();
        EditSession result = session;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                result = session;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return result;
    }

    public EditSession getOrCreateSession() {
        // use the already-generated result if possible
        EditSession result = getSession();
        if (result != null) {
            return result;
        }
        // otherwise, acquire write lock
        long stamp = lock.writeLock();
        try {
            // check session field again -- maybe another writer hit it in between
            result = session;
            if (result != null) {
                return result;
            }
            // now we can do the actual creation
            LocalSession localSession = worldEdit.getSessionManager().get(player);
            EditSession editSession = localSession.createEditSession(player);
            editSession.enableStandardMode();
            localSession.tellVersion(player);
            return session = editSession;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

}
