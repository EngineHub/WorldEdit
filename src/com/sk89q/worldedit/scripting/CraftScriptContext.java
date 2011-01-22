// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.scripting;

import java.util.ArrayList;
import java.util.List;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEditController;
import com.sk89q.worldedit.blocks.BaseBlock;

public class CraftScriptContext extends CraftScriptEnvironment {
    private List<EditSession> editSessions = new ArrayList<EditSession>();
    
    public CraftScriptContext(WorldEditController controller,
            ServerInterface server, LocalConfiguration config,
            LocalSession session, LocalPlayer player) {
        super(controller, server, config, session, player);
    }
    
    public EditSession startEditSession() {
        EditSession editSession =
                new EditSession(server, player.getWorld(),
                        session.getBlockChangeLimit(), session.getBlockBag(player));
        editSessions.add(editSession);
        return editSession;
    }
    
    public LocalPlayer getPlayer() {
        return player;
    }
    
    public LocalSession getSession() {
        return session;
    }
    
    public LocalConfiguration getConfiguration() {
        return config;
    }
    
    public List<EditSession> _getEditSessions() {
        return editSessions;
    }
    
    public void print(String msg) {
        player.print(msg);
    }
    
    public void error(String msg) {
        player.printError(msg);
    }
    
    public BaseBlock getBlock(String arg) throws UnknownItemException, DisallowedItemException {
        return controller.getBlock(player, arg, false);
    }
    
    public BaseBlock getAnyBlock(String arg) throws UnknownItemException, DisallowedItemException {
        return controller.getBlock(player, arg, true);
    }
}
