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

package com.sk89q.worldedit.bukkit;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.events.WEBlockBreakEvent;
import com.sk89q.worldedit.events.WorldEditBlockEvent;
import com.sk89q.worldedit.events.WorldEditEvent;

public class BukkitServerInterface extends ServerInterface {
    public Server server;
    public WorldEditPlugin plugin;
    
    public BukkitServerInterface(WorldEditPlugin plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public int resolveItem(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isValidMobType(String type) {
        return CreatureType.fromName(type) != null;
    }

    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @Override
    public boolean callEvent(LocalPlayer lPlayer, WorldEditEvent event) {
        switch (event.getType()) {
        case BLOCK_DESTROY:
            WorldEditBlockEvent blockEvent = (WorldEditBlockEvent) event;
            Vector pt = blockEvent.getVector();
            Player player = server.getPlayer(lPlayer.getName());
            Block block = player.getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            WEBlockBreakEvent bEvent = new WEBlockBreakEvent(block, player, blockEvent);
            server.getPluginManager().callEvent(bEvent);
            return bEvent.isCancelled();
        default:
            throw new UnsupportedOperationException();
        }
    }

}
