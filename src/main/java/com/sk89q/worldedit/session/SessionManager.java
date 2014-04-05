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

package com.sk89q.worldedit.session;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Session manager for WorldEdit.
 * </p>
 * Get a reference to one from {@link WorldEdit}.
 * </p>
 * While this class is thread-safe, the returned session may not be.
 */
public class SessionManager {

    private final WorldEdit worldEdit;
    private final HashMap<String, LocalSession> sessions = new HashMap<String, LocalSession>();

    /**
     * Create a new session manager.
     *
     * @param worldEdit a WorldEdit instance
     */
    public SessionManager(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /**
     * Get whether a session exists for the given actor.
     *
     * @param actor the actor
     * @return true if a session exists
     */
    public synchronized boolean contains(Actor actor) {
        checkNotNull(actor);
        return sessions.containsKey(getKey(actor));
    }

    /**
     * Gets the session for an actor and return it if it exists, otherwise
     * return <code>null</code>.
     *
     * @param actor the actor
     * @return the session for the actor, if it exists
     */
    public synchronized @Nullable LocalSession find(Actor actor) {
        checkNotNull(actor);
        return sessions.get(getKey(actor));
    }

    /**
     * Gets the session for someone named by the given name and return it if
     * it exists, otherwise return <code>null</code>.
     *
     * @param name the actor's name
     * @return the session for the actor, if it exists
     */
    public synchronized @Nullable LocalSession findByName(String name) {
        checkNotNull(name);
        return sessions.get(name);
    }

    /**
     * Get the session for an actor and create one if one doesn't exist.
     *
     * @param actor the actor
     * @return a session
     */
    public synchronized LocalSession get(Actor actor) {
        checkNotNull(actor);

        LocalSession session;
        LocalConfiguration config = worldEdit.getConfiguration();

        if (sessions.containsKey(actor.getName())) {
            session = sessions.get(actor.getName());
        } else {
            session = new LocalSession(config);
            session.setBlockChangeLimit(config.defaultChangeLimit);
            // Remember the session
            sessions.put(actor.getName(), session);
        }

        // Set the limit on the number of blocks that an operation can
        // change at once, or don't if the actor has an override or there
        // is no limit. There is also a default limit
        int currentChangeLimit = session.getBlockChangeLimit();

        if (!actor.hasPermission("worldedit.limit.unrestricted")
                && config.maxChangeLimit > -1) {

            // If the default limit is infinite but there is a maximum
            // limit, make sure to not have it be overridden
            if (config.defaultChangeLimit < 0) {
                if (currentChangeLimit < 0 || currentChangeLimit > config.maxChangeLimit) {
                    session.setBlockChangeLimit(config.maxChangeLimit);
                }
            } else {
                // Bound the change limit
                int maxChangeLimit = config.maxChangeLimit;
                if (currentChangeLimit == -1 || currentChangeLimit > maxChangeLimit) {
                    session.setBlockChangeLimit(maxChangeLimit);
                }
            }
        }

        // Have the session use inventory if it's enabled and the actor
        // doesn't have an override
        session.setUseInventory(config.useInventory
                && !(config.useInventoryOverride
                && (actor.hasPermission("worldedit.inventory.unrestricted")
                || (config.useInventoryCreativeOverride && (!(actor instanceof Player) || ((Player) actor).hasCreativeMode())))));

        return session;
    }

    /**
     * Get the key to use in the map for an actor.
     *
     * @param actor the actor
     * @return the key object
     */
    protected String getKey(Actor actor) {
        return actor.getName();
    }

    /**
     * Mark for expiration.
     *
     * @param actor the actor
     */
    public synchronized void markforExpiration(Actor actor) {
        checkNotNull(actor);
        LocalSession session = find(actor);
        if (session != null) {
            session.update();
        }
    }

    /**
     * Remove the session for the given actor if one exists.
     *
     * @param actor the actor
     */
    public synchronized void remove(Actor actor) {
        checkNotNull(actor);
        sessions.remove(actor.getName());
    }

    /**
     * Remove all sessions.
     */
    public synchronized void clear() {
        sessions.clear();
    }

    /**
     * Remove expired sessions with the given session checker.
     *
     * @param checker the session checker
     */
    public synchronized void removeExpired(SessionCheck checker) {
        Iterator<Map.Entry<String, LocalSession>> it = sessions.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, LocalSession> entry = it.next();
            if (entry.getValue().hasExpired() && !checker.isOnlinePlayer(entry.getKey())) {
                it.remove();
            }
        }
    }

}
