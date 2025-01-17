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

package com.sk89q.worldedit.neoforge;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.neoforge.internal.ExtendedChunk;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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

class NeoForgePlatform extends AbstractPlatform implements MultiUserPlatform {

    private final NeoForgeWorldEdit mod;
    private final NeoForgeDataFixer dataFixer;
    private @Nullable NeoForgeWatchdog watchdog;
    private boolean hookingEvents = false;
    private final ResourceLoader resourceLoader = new NeoForgeResourceLoader(WorldEdit.getInstance());

    NeoForgePlatform(NeoForgeWorldEdit mod) {
        this.mod = mod;
        this.dataFixer = new NeoForgeDataFixer(getDataVersion());
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
        return NeoForgeRegistries.getInstance();
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
        return BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(type));
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
    public NeoForgeWatchdog getWatchdog() {
        if (watchdog == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server instanceof DedicatedServer) {
                watchdog = new NeoForgeWatchdog((DedicatedServer) server);
            }
        }
        return watchdog;
    }

    @Override
    public List<? extends World> getWorlds() {
        Iterable<ServerLevel> worlds = ServerLifecycleHooks.getCurrentServer().getAllLevels();
        List<World> ret = new ArrayList<>();
        for (ServerLevel world : worlds) {
            ret.add(new NeoForgeWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof NeoForgePlayer) {
            return player;
        } else {
            ServerPlayer entity = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(player.getName());
            return entity != null ? new NeoForgePlayer(entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof NeoForgeWorld) {
            return world;
        } else {
            for (ServerLevel ws : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                if (((ServerLevelData) ws.getLevelData()).getLevelName().equals(world.getName())) {
                    return new NeoForgeWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(CommandManager manager) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        Commands mcMan = server.getCommands();

        for (Command command : manager.getAllCommands().toList()) {
            CommandWrapper.register(mcMan.getDispatcher(), command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(NeoForgeWorldEdit.inst.getPermissionsProvider()::registerPermission);
            }
        }
    }

    @Override
    public void setGameHooksEnabled(boolean enabled) {
        this.hookingEvents = enabled;
    }

    @Override
    public NeoForgeConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "NeoForge-Official";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String id() {
        return "enginehub:neoforge";
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

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS_NO_MIXIN = Sets.immutableEnumSet(
        SideEffect.VALIDATION,
        SideEffect.ENTITY_AI,
        SideEffect.LIGHTING,
        SideEffect.NEIGHBORS,
        SideEffect.EVENTS
    );

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(
        Iterables.concat(SUPPORTED_SIDE_EFFECTS_NO_MIXIN, Collections.singleton(SideEffect.UPDATE))
    );

    @Override
    public Set<SideEffect> getSupportedSideEffects() {
        return ExtendedChunk.class.isAssignableFrom(LevelChunk.class)
            ? SUPPORTED_SIDE_EFFECTS
            : SUPPORTED_SIDE_EFFECTS_NO_MIXIN;
    }

    @Override
    public long getTickCount() {
        return ServerLifecycleHooks.getCurrentServer().getTickCount();
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        PlayerList scm = ServerLifecycleHooks.getCurrentServer().getPlayerList();
        for (ServerPlayer entity : scm.getPlayers()) {
            if (entity != null) {
                users.add(new NeoForgePlayer(entity));
            }
        }
        return users;
    }
}
