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

package org.enginehub.worldedit.event;

import org.enginehub.common.WorldObject;
import org.enginehub.event.Cancellable;
import org.enginehub.event.Event;
import org.enginehub.event.ExecutorList;
import org.enginehub.util.ControllerFunction;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldVector;

/**
 * Raised when an actor interacts with an object in the world, or by pressing one of
 * their {@link ControllerFunction}s.
 */
public class ActorInteractEvent extends Event implements Cancellable {
    
    private static final ExecutorList<ActorInteractEvent> executors = new
            ExecutorList<ActorInteractEvent>();
    
    private boolean cancelled;
    private LocalPlayer actor;
    private WorldVector position;
    private WorldObject object;
    private ControllerFunction function;

    protected ActorInteractEvent() {
    }

    /**
     * The actor interacted with an actual object.
     * 
     * @param actor the actor
     * @param function the function key
     * @param position the position of the object
     * @param object the object
     */
    public ActorInteractEvent(LocalPlayer actor, ControllerFunction function, 
            WorldVector position, WorldObject object) {
        this.actor = actor;
        this.function = function;
        this.position = position;
        this.object = object;
    }

    /**
     * The actor merely used a {@link ControllerFunction} without actually utilizing
     * an object.
     * 
     * @param actor the actor
     * @param function the function key
     */
    public ActorInteractEvent(LocalPlayer actor, ControllerFunction function) {
        this.actor = actor;
        this.function = function;
        this.position = null;
        this.object = null;
    }

    public LocalPlayer getActor() {
        return actor;
    }

    public ControllerFunction getFunction() {
        return function;
    }
    
    public boolean hasObject() {
        return object != null;
    }

    public WorldVector getPosition() {
        return position;
    }

    public WorldObject getObject() {
        return object;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    protected ExecutorList<ActorInteractEvent> getExecutors() {
        return executors;
    }

}
