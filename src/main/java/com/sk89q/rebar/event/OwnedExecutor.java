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

package com.sk89q.rebar.event;

import com.sk89q.rebar.util.Ownable;
import com.sk89q.rebar.util.Owner;

/**
 * An executor that has an owner attached to it.
 * 
 * @param <T> the event type
 */
public class OwnedExecutor<T extends Event> implements
        EventExecutor<T>, Comparable<EventExecutor<T>>, Ownable {

    private final EventExecutor<T> executor;
    private Owner owner;

    /**
     * Construct a new instance with a null owner.
     * 
     * @param executor the executor
     */
    public OwnedExecutor(EventExecutor<T> executor) {
        this.executor = executor;
    }

    /**
     * Construct a new instance with a set owner.
     * 
     * @param executor the executor
     * @param owner the owner, or null
     */
    public OwnedExecutor(EventExecutor<T> executor, Owner owner) {
        this.executor = executor;
        setOwner(owner);
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public void execute(T event) throws EventException {
        executor.execute(event);
    }

    @Override
    public int getPriority() {
        return executor.getPriority();
    }

    @Override
    public int compareTo(EventExecutor<T> o) {
        if (o.getPriority() == getPriority()) {
            return 0;
        }
        return getPriority() < o.getPriority() ? -1 : 1;
    }

    @Override
    public boolean isIgnoringCancelled() {
        return executor.isIgnoringCancelled();
    }

}
