// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.spout;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import org.spout.api.Game;
import org.spout.api.Spout;
import org.spout.api.geo.World;
import org.spout.api.material.Material;
import org.spout.api.material.MaterialData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpoutServerInterface extends ServerInterface {
    public Game game;
    public WorldEditPlugin plugin;
    private final SpoutRawCommandExecutor executor;

    public SpoutServerInterface(WorldEditPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.executor = new SpoutRawCommandExecutor(plugin);
    }

    @Override
    public int resolveItem(String name) {
        Material mat = MaterialData.getMaterial(name);
        return mat == null ? 0 : mat.getId();
    }

    @Override
    public boolean isValidMobType(String type) {
        return false;
        //return CreatureType.fromName(type) != null;
    }

    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return game.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
    }

    @Override
    public List<LocalWorld> getWorlds() {
        Collection<World> worlds = game.getWorlds();
        List<LocalWorld> ret = new ArrayList<LocalWorld>(worlds.size());

        for (World world : worlds) {
            ret.add(SpoutUtil.getLocalWorld(world));
        }

        return ret;
    }
    
    @Override
    public void onCommandRegistration(List<Command> commands) {
        for (Command command : commands) {
            Spout.getGame().getRootCommand().addSubCommand(plugin, command.aliases()[0])
                    .addAlias(command.aliases()).setRawExecutor(executor).closeSubCommand();
        }
    }
}
