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

package org.enginehub.worldedit;

import org.enginehub.event.EventSystem;
import org.enginehub.event.Handler;
import org.enginehub.util.Owner;
import org.enginehub.worldedit.event.ActorInteractEvent;
import org.enginehub.worldedit.event.WandUseEvent;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;

/**
 * Helper class to provide the use of WorldEdit in-game.
 */
public class InteractiveProxy {
    
    private LocalConfiguration config;

    public InteractiveProxy(Owner owner, LocalConfiguration config) {
        EventSystem.getInstance().register(this, owner);
        this.config = config;
    }
    
    @Handler(ignoreCancelled = true)
    public void onActorInteract(ActorInteractEvent event) {
        LocalPlayer actor = event.getActor();
        
        if (event.hasObject() && actor.getItemInHand() == config.wandItem) {
            boolean cancelled = EventSystem.getInstance().testDispatch(
                    new WandUseEvent(actor, event.getFunction(), event.getPosition()));
            if (cancelled) {
                event.setCancelled(true);
            }
        }
    }

}
