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

package org.enginehub.session;


/**
 * A session map that also constructs sessions automatically when they do not yet
 * exist for a given object.
 *
 * @param <K> the session key
 * @param <V> the session object
 */
public abstract class GuaranteedSessionMap<K, V extends Session> extends SessionMap<K, V> {

    /**
     * Creates a session for the given key.
     * 
     * @param key the key
     * @return a new session
     */
    protected abstract V create(K key);

    /**
     * Gets the session indexed by the given key.
     * 
     * <p>If the session does not yet exist, it will be created. Only one session will
     * be created if a session does not yet exist for a given key, even if two session
     * requests come in for the same key at the same time, because of a re-entrant lock
     * used to keep the map thread-safe.</p>
     * 
     * <p>Creation of the session is done while the lock on sessions is active, and
     * therefore session creation should be quick.</p>
     * 
     * @param key the key
     * @return the session
     */
    @Override
    public synchronized V get(K key) {
        V session = super.get(key);
        if (session == null) {
            session = create(key);
        }
        return session;
    }

    /**
     * Gets the session indexed by the given key.
     * 
     * @param key the key
     * @return the session, or null
     */
    public V getRaw(K key) {
        return super.get(key);
    }

}
