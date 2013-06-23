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

package com.sk89q.worldedit.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.SessionCheck;

/**
 * Holds WorldEdit sessions.
 */
public class SessionMap {

    private final LocalConfiguration config;
    private final HashMap<String, LocalSession> sessions = new HashMap<String, LocalSession>();

    /**
     * Create a new session map.
     *
     * @param config the configuration
     */
    public SessionMap(LocalConfiguration config) {
        this.config = config;
    }

    /**
     * Gets the LocalSession for a player name if it exists.
     *
     * @param player the player
     * @return the session for the player, if it exists, otherwise null
     */
    public LocalSession getIfExists(String player) {
        return sessions.get(player);
    }

    /**
     * Gets the WorldEdit session for a player.
     * 
     * <p>If the session does not yet exist, create a new one.</p>
     *
     * @param player the player
     * @return the session
     */
    public LocalSession get(LocalPlayer player) {
        LocalSession session;

        synchronized (sessions) {
            if (sessions.containsKey(player.getName())) {
                session = sessions.get(player.getName());
            } else {
                session = new LocalSession(config);
                session.setBlockChangeLimit(config.defaultChangeLimit);
                // Remember the session
                sessions.put(player.getName(), session);
            }

            // Set the limit on the number of blocks that an operation can
            // change at once, or don't if the player has an override or there
            // is no limit. There is also a default limit
            int currentChangeLimit = session.getBlockChangeLimit();

            if (!player.hasPermission("worldedit.limit.unrestricted")
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

            // Have the session use inventory if it's enabled and the player
            // doesn't have an override
            session.setUseInventory(config.useInventory
                    && !((config.useInventoryOverride ||
                    (config.useInventoryCreativeOverride && player.hasCreativeMode()))
                    && player.hasPermission("worldedit.inventory.unrestricted")));

        }

        return session;
    }

    /**
     * Returns true if the player has a session.
     *
     * @param player the player to lookup
     * @return true if the player has a session
     */
    public boolean contains(LocalPlayer player) {
        synchronized (sessions) {
            return sessions.containsKey(player.getName());
        }
    }

    /**
     * Mark a session to be expired.
     *
     * <p>This might be called if the player has left the game in the case of
     * a multiplayer server.</p>
     *
     * @param player the player
     */
    public void setExpiration(LocalPlayer player) {
        synchronized (sessions) {
            LocalSession session = sessions.get(player.getName());
            if (session != null) {
                session.update();
            }
        }
    }

    /**
     * Remove a session.
     *
     * @param player the player
     */
    public void remove(LocalPlayer player) {
        synchronized (sessions) {
            sessions.remove(player.getName());
        }
    }

    /**
     * Remove all sessions.
     */
    public void clear() {
        synchronized (sessions) {
            sessions.clear();
        }
    }

    /*
     * Flush expired sessions.
     */
    public void flush(SessionCheck checker) {
        synchronized (sessions) {
            Iterator<Map.Entry<String, LocalSession>> it = sessions.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, LocalSession> entry = it.next();
                if (entry.getValue().hasExpired()
                        && !checker.isOnlinePlayer(entry.getKey())) {
                    it.remove();
                }
            }
        }
    }

}
