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

package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

class FabricPlatform extends AbstractPlatform implements MultiUserPlatform {

    private final FabricWorldEdit mod;
    private final MinecraftServer server;
    private final FabricDataFixer dataFixer;
    private final @Nullable Watchdog watchdog;
    private boolean hookingEvents = false;

    FabricPlatform(FabricWorldEdit mod, MinecraftServer server) {
        this.mod = mod;
        this.server = server;
        this.dataFixer = new FabricDataFixer(getDataVersion());
        this.watchdog = server instanceof MinecraftDedicatedServer
            ? (Watchdog) server : null;
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public Registries getRegistries() {
        return FabricRegistries.getInstance();
    }

    @Override
    public int getDataVersion() {
        return SharedConstants.getGameVersion().getWorldVersion();
    }

    @Override
    public DataFixer getDataFixer() {
        return dataFixer;
    }

    @Override
    public boolean isValidMobType(String type) {
        return Registry.ENTITY_TYPE.containsId(new Identifier(type));
    }

    @Override
    public void reload() {
        getConfiguration().load();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    @Override
    @Nullable
    public Watchdog getWatchdog() {
        return watchdog;
    }

    @Override
    public List<? extends World> getWorlds() {
        Iterable<ServerWorld> worlds = server.getWorlds();
        List<World> ret = new ArrayList<>();
        for (ServerWorld world : worlds) {
            ret.add(new FabricWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof FabricPlayer) {
            return player;
        } else {
            ServerPlayerEntity entity = server.getPlayerManager().getPlayer(player.getName());
            return entity != null ? new FabricPlayer(entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof FabricWorld) {
            return world;
        } else {
            for (ServerWorld ws : server.getWorlds()) {
                if (ws.getLevelProperties().getLevelName().equals(world.getName())) {
                    return new FabricWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(CommandManager manager) {
        if (server == null) return;
        net.minecraft.server.command.CommandManager mcMan = server.getCommandManager();

        for (Command command : manager.getAllCommands().collect(toList())) {
            CommandWrapper.register(mcMan.getDispatcher(), command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(FabricWorldEdit.inst.getPermissionsProvider()::registerPermission);
            }
        }
    }

    @Override
    public void registerGameHooks() {
        // We registered the events already anyway, so we just 'turn them on'
        hookingEvents = true;
    }

    @Override
    public FabricConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "Fabric-Official";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.PREFER_OTHERS);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        PlayerManager scm = server.getPlayerManager();
        for (ServerPlayerEntity entity : scm.getPlayerList()) {
            if (entity != null) {
                users.add(new FabricPlayer(entity));
            }
        }
        return users;
    }
}
