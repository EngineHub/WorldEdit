/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.forge;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.toList;

class ForgePlatform extends AbstractPlatform implements MultiUserPlatform {

    private final ForgeWorldEdit mod;
    private final MinecraftServer server;
    private final ForgeDataFixer dataFixer;
    private final @Nullable ForgeWatchdog watchdog;
    private boolean hookingEvents = false;
    private final ResourceLoader resourceLoader = new ForgeResourceLoader(WorldEdit.getInstance());

    ForgePlatform(ForgeWorldEdit mod) {
        this.mod = mod;
        this.server = ServerLifecycleHooks.getCurrentServer();
        this.dataFixer = new ForgeDataFixer(getDataVersion());
        this.watchdog = server instanceof DedicatedServer
            ? new ForgeWatchdog((DedicatedServer) server) : null;
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public Registries getRegistries() {
        return ForgeRegistries.getInstance();
    }

    @Override
    public int getDataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    @Override
    public DataFixer getDataFixer() {
        return dataFixer;
    }

    @Override
    public boolean isValidMobType(String type) {
        return net.minecraftforge.registries.ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type));
    }

    @Override
    public void reload() {
        getConfiguration().load();
        super.reload();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    @Override
    @Nullable
    public ForgeWatchdog getWatchdog() {
        return watchdog;
    }

    @Override
    public List<? extends World> getWorlds() {
        Iterable<ServerLevel> worlds = server.getAllLevels();
        List<World> ret = new ArrayList<>();
        for (ServerLevel world : worlds) {
            ret.add(new ForgeWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof ForgePlayer) {
            return player;
        } else {
            ServerPlayer entity = server.getPlayerList().getPlayerByName(player.getName());
            return entity != null ? new ForgePlayer(entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof ForgeWorld) {
            return world;
        } else {
            for (ServerLevel ws : server.getAllLevels()) {
                if (((ServerLevelData) ws.getLevelData()).getLevelName().equals(world.getName())) {
                    return new ForgeWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(CommandManager manager) {
        if (server == null) {
            return;
        }
        Commands mcMan = server.getCommands();

        for (Command command : manager.getAllCommands().collect(toList())) {
            CommandWrapper.register(mcMan.getDispatcher(), command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(ForgeWorldEdit.inst.getPermissionsProvider()::registerPermission);
            }
        }
    }

    @Override
    public void setGameHooksEnabled(boolean enabled) {
        this.hookingEvents = enabled;
    }

    @Override
    public ForgeConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "Forge-Official";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getId() {
        return "enginehub:forge";
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

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(
        SideEffect.VALIDATION,
        SideEffect.ENTITY_AI,
        SideEffect.LIGHTING,
        SideEffect.NEIGHBORS,
        SideEffect.EVENTS
    );

    @Override
    public Set<SideEffect> getSupportedSideEffects() {
        return SUPPORTED_SIDE_EFFECTS;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        PlayerList scm = server.getPlayerList();
        for (ServerPlayer entity : scm.getPlayers()) {
            if (entity != null) {
                users.add(new ForgePlayer(entity));
            }
        }
        return users;
    }
}
