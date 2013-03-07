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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.enginehub.util.StringIdentifiable;

/**
 * Manages sessions.
 * 
 * <p>{@link #flushExpired()} must be called periodically to remove dead sessions,
 * otherwise you have a potential memory leak.</p>
 * 
 * <p>Session maps are entirely thread-safe. Multiple threads will block each other
 * while accessing a session. Sessions retrieved do not need to be closed, as only
 * the retrieval of the session object is protected against parallel accesses.
 * Parallel access of objects within the session object are the responsibility of
 * the session objects themselves.</p>
 *
 * @param <K> the object that sessions are stored again
 * @param <V> the session object
 */
public class SessionMap<K, V extends Session> {
    
    private static final Logger logger = 
            Logger.getLogger(SessionMap.class.getCanonicalName());
    private Map<Object, V> sessions = new LinkedHashMap<Object, V>();
    
    /**
     * Remove expired sessions.
     * 
     * @return the number of sessions expired
     */
    public int flushExpired() {
        List<Session> toExpire = new ArrayList<Session>();
        long now = System.currentTimeMillis();
        int numberExpired = 0;
        
        synchronized (this) {
            Iterator<V> it = sessions.values().iterator();
            
            while (it.hasNext()) {
                V session = it.next();
                if (session.getExpirationTime() >= now) {
                    it.remove(); // Expire the session
                    numberExpired++;
                    toExpire.add(session);
                }
            }
        }
        
        for (Session session : toExpire) {
            expire(session);
        }
        
        return numberExpired;
    }
    
    /**
     * Call the expire method of a session.
     * 
     * @param session the session
     */
    private void expire(Session session) {
        try {
            session.onExpire();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Failed to call onExpire() for session", t);
        }
    }
    
    /**
     * Map the given key to the internal key.
     * 
     * @param key the raw key
     * @return
     */
    private Object mapKey(K key) {
        if (key instanceof StringIdentifiable) {
            return ((StringIdentifiable) key).getStringId();
        } else {
            return key;
        }
    }

    public synchronized int size() {
        return sessions.size();
    }

    public synchronized boolean isEmpty() {
        return sessions.isEmpty();
    }

    public synchronized boolean containsKey(K key) {
        return sessions.containsKey(mapKey(key));
    }

    public synchronized V get(K key) {
        return sessions.get(mapKey(key));
    }

    public synchronized V put(K key, V value) {
        return sessions.put(mapKey(key), value);
    }

    public V remove(K key) {
        V session;
        synchronized (this) {
            session = sessions.remove(mapKey(key));
        }
        if (session != null) {
            expire(session);
        }
        return session;
    }

    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        Collection<V> toExpire;
        
        synchronized (this) {
            toExpire = this.values();
            sessions = new LinkedHashMap<Object, V>();
        }
        
        for (Session session : toExpire) {
            expire(session);
        }
    }

    public synchronized Collection<V> values() {
        return sessions.values();
    }

}
