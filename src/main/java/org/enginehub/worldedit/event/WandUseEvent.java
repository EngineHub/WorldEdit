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

import org.enginehub.event.Cancellable;
import org.enginehub.event.Event;
import org.enginehub.event.ExecutorList;
import org.enginehub.util.ControllerFunction;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldVector;

/**
 * Event indicating when a WorldEdit wand is utilized.
 */
public class WandUseEvent extends Event implements Cancellable {
    
    private static final ExecutorList<WandUseEvent> executors = new ExecutorList<WandUseEvent>();
    
    private boolean cancelled;
    private LocalPlayer actor;
    private ControllerFunction function;
    private WorldVector position;

    protected WandUseEvent() {
    }

    public WandUseEvent(LocalPlayer actor, ControllerFunction function, WorldVector position) {
        this.actor = actor;
        this.function = function;
        this.position = position;
    }

    public LocalPlayer getActor() {
        return actor;
    }

    public ControllerFunction getFunction() {
        return function;
    }

    public WorldVector getPosition() {
        return position;
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
    protected ExecutorList<WandUseEvent> getExecutors() {
        return executors;
    }

}
