/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.spout;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import org.spout.api.Engine;
import org.spout.api.geo.World;
import org.spout.api.material.Material;
import org.spout.api.material.MaterialRegistry;
import org.spout.api.scheduler.TaskPriority;
import org.spout.vanilla.api.material.VanillaMaterial;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpoutServerInterface extends ServerInterface {
    public Engine game;
    public WorldEditPlugin plugin;
    private final SpoutRawCommandExecutor executor;
    private SpoutBiomeTypes biomes;

    public SpoutServerInterface(WorldEditPlugin plugin, Engine game) {
        this.plugin = plugin;
        this.game = game;
        this.biomes = new SpoutBiomeTypes();
        this.executor = new SpoutRawCommandExecutor(plugin);
    }

    @Override
    public int resolveItem(String name) {
        Material mat = MaterialRegistry.get(name);
        return mat == null || !(mat instanceof VanillaMaterial) ? 0 : ((VanillaMaterial) mat).getMinecraftId();
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
    public SpoutBiomeTypes getBiomes() {
        return biomes;
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return game.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay * 50, period * 50, TaskPriority.NORMAL).getTaskId();
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
    public void onCommandRegistration(List<Command> commands, CommandsManager<LocalPlayer> manager) {
        for (Command command : commands) {
            org.spout.api.command.Command spoutCommand = game.getRootCommand().addSubCommand(plugin, command.aliases()[0])
                    .addAlias(command.aliases()).setRawExecutor(executor)
                    .setHelp("/" + command.aliases()[0] + " " + command.usage() + " - " + command.desc());
            Method cmdMethod = manager.getMethods().get(null).get(command.aliases()[0]);
            if (cmdMethod != null && cmdMethod.isAnnotationPresent(CommandPermissions.class)) {
                spoutCommand.setPermissions(false, cmdMethod.getAnnotation(CommandPermissions.class).value());
            }
            spoutCommand.closeSubCommand();
        }
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return plugin.getLocalConfiguration();
    }
}
