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

package com.sk89q.worldedit.bukkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;
import com.sk89q.minecraft.util.commands.Command;

import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.LocalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;

public class BukkitServerInterface extends ServerInterface {
    public Server server;
    public WorldEditPlugin plugin;
    private CommandRegistration dynamicCommands;
    private BukkitBiomeTypes biomes;

    public BukkitServerInterface(WorldEditPlugin plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
        this.biomes = new BukkitBiomeTypes();
        dynamicCommands = new CommandRegistration(plugin);
    }

    @Override
    public int resolveItem(String name) {
        Material mat = Material.matchMaterial(name);
        return mat == null ? 0 : mat.getId();
    }

    @Override
    public boolean isValidMobType(String type) {
        final EntityType entityType = EntityType.fromName(type);
        return entityType != null && entityType.isAlive();
    }

    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @Override
    public BiomeTypes getBiomes() {
        return biomes;
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
    }

    @Override
    public List<LocalWorld> getWorlds() {
        List<World> worlds = server.getWorlds();
        List<LocalWorld> ret = new ArrayList<LocalWorld>(worlds.size());

        for (World world : worlds) {
            ret.add(BukkitUtil.getLocalWorld(world));
        }

        return ret;
    }

    @Override
    public void onCommandRegistration(List<Command> commands, CommandsManager<LocalPlayer> manager) {
        List<CommandInfo> toRegister = new ArrayList<CommandInfo>();
        for (Command command : commands) {
            List<String> permissions = null;
            Method cmdMethod = manager.getMethods().get(null).get(command.aliases()[0]);
            Map<String, Method> childMethods = manager.getMethods().get(cmdMethod);

            if (cmdMethod != null && cmdMethod.isAnnotationPresent(CommandPermissions.class)) {
                permissions = Arrays.asList(cmdMethod.getAnnotation(CommandPermissions.class).value());
            } else if (cmdMethod != null && childMethods != null && childMethods.size() > 0) {
                permissions = new ArrayList<String>();
                for (Method m : childMethods.values()) {
                    if (m.isAnnotationPresent(CommandPermissions.class)) {
                        permissions.addAll(Arrays.asList(m.getAnnotation(CommandPermissions.class).value()));
                    }
                }
            }

            toRegister.add(new CommandInfo(command.usage(), command.desc(), command.aliases(), commands, permissions == null ? null : permissions.toArray(new String[permissions.size()])));
        }

        dynamicCommands.register(toRegister);
    }

    public void unregisterCommands() {
        dynamicCommands.unregisterCommands();
    }
}
