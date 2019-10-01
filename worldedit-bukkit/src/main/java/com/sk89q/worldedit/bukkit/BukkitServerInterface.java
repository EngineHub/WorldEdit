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

package com.sk89q.worldedit.bukkit;

import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.registry.Registries;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.enginehub.piston.CommandManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.formatting.WorldEditText.reduceToText;

public class BukkitServerInterface implements MultiUserPlatform {
    public Server server;
    public WorldEditPlugin plugin;
    private CommandRegistration dynamicCommands;
    private boolean hookingEvents;
    private final LazyReference<Watchdog> watchdog = LazyReference.from(() -> {
        if (plugin.getBukkitImplAdapter() != null) {
            return plugin.getBukkitImplAdapter().supportsWatchdog()
                ? new BukkitWatchdog(plugin.getBukkitImplAdapter())
                : null;
        }
        return null;
    });

    public BukkitServerInterface(WorldEditPlugin plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
        dynamicCommands = new CommandRegistration(plugin);
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public Registries getRegistries() {
        return BukkitRegistries.getInstance();
    }

    @Override
    public int getDataVersion() {
        if (plugin.getBukkitImplAdapter() != null) {
            return plugin.getBukkitImplAdapter().getDataVersion();
        }
        return -1;
    }

    @Override
    public DataFixer getDataFixer() {
        if (plugin.getBukkitImplAdapter() != null) {
            return plugin.getBukkitImplAdapter().getDataFixer();
        }
        return null;
    }

    @Override
    public boolean isValidMobType(String type) {
        if (!type.startsWith("minecraft:")) {
            return false;
        }
        final EntityType entityType = EntityType.fromName(type.substring(10));
        return entityType != null && entityType.isAlive();
    }

    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
    }

    @Override
    public Watchdog getWatchdog() {
        return watchdog.getValue();
    }

    @Override
    public List<com.sk89q.worldedit.world.World> getWorlds() {
        List<World> worlds = server.getWorlds();
        List<com.sk89q.worldedit.world.World> ret = new ArrayList<>(worlds.size());

        for (World world : worlds) {
            ret.add(BukkitAdapter.adapt(world));
        }

        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof BukkitPlayer) {
            return player;
        } else {
            org.bukkit.entity.Player bukkitPlayer = server.getPlayerExact(player.getName());
            return bukkitPlayer != null ? WorldEditPlugin.getInstance().wrapPlayer(bukkitPlayer) : null;
        }
    }

    @Nullable
    @Override
    public BukkitWorld matchWorld(com.sk89q.worldedit.world.World world) {
        if (world instanceof BukkitWorld) {
            return (BukkitWorld) world;
        } else {
            World bukkitWorld = server.getWorld(world.getName());
            return bukkitWorld != null ? new BukkitWorld(bukkitWorld) : null;
        }
    }

    @Override
    public void registerCommands(CommandManager dispatcher) {
        BukkitCommandInspector inspector = new BukkitCommandInspector(plugin, dispatcher);

        dynamicCommands.register(dispatcher.getAllCommands()
            .map(command -> {
                String[] permissionsArray = command.getCondition()
                    .as(PermissionCondition.class)
                    .map(PermissionCondition::getPermissions)
                    .map(s -> s.toArray(new String[0]))
                    .orElseGet(() -> new String[0]);

                String[] aliases = Stream.concat(
                    Stream.of(command.getName()),
                    command.getAliases().stream()
                ).toArray(String[]::new);
                return new CommandInfo(reduceToText(command.getUsage()),
                    reduceToText(command.getDescription()), aliases,
                    inspector, permissionsArray);
            }).collect(Collectors.toList()));
    }

    @Override
    public void registerGameHooks() {
        hookingEvents = true;
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return plugin.getLocalConfiguration();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getPlatformName() {
        return "Bukkit-Official";
    }

    @Override
    public String getPlatformVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.NORMAL);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.PREFERRED);
        capabilities.put(Capability.PERMISSIONS, Preference.PREFERRED);
        capabilities.put(Capability.USER_COMMANDS, Preference.PREFERRED);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFER_OTHERS);
        return capabilities;
    }

    public void unregisterCommands() {
        dynamicCommands.unregisterCommands();
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        for (org.bukkit.entity.Player player : Bukkit.getServer().getOnlinePlayers()) {
            users.add(WorldEditPlugin.getInstance().wrapPlayer(player));
        }
        return users;
    }
}
