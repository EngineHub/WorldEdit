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
 * A basic session. It is expected that this class be subclassed.
 */
public interface Session {

    /**
     * Get the expiration time of the session in milliseconds, which is the absolute
     * point in time when the session expires.
     * 
     * @return the absolute expiration time in milliseconds
     */
    public abstract long getExpirationTime();

    /**
     * Set the expiration time of the session in milliseconds, which is the absolute
     * point in time when the session expires
     * 
     * <p>Session managers are expected to keep sessions up until the expiration
     * time. At that point, session managers should remove the session.</p>
     * 
     * @param expirationTime the absolute expiration time in milliseconds
     */
    public abstract void setExpirationTime(long expirationTime);

    /**
     * Called when the session expires.
     * 
     * <p>Depending on how sessions are managed, this may be called in any thread.</p>
     */
    public abstract void onExpire();

}