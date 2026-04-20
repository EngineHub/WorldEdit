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

package com.sk89q.worldedit.coremc.internal;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.coremc.CoreMcAdapter;
import com.sk89q.worldedit.coremc.CoreMcPermissionsProvider;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.lifecycle.ConstantLifecycled;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import org.enginehub.piston.CommandManager;
import org.enginehub.worldeditcui.protocol.CUIPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public abstract class CoreMcPlatform extends AbstractPlatform implements MultiUserPlatform {

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(
        SideEffect.VALIDATION,
        SideEffect.ENTITY_AI,
        SideEffect.LIGHTING,
        SideEffect.NEIGHBORS,
        SideEffect.UPDATE
    );

    private final CoreMcMod mod;
    private final CoreMcDataFixer dataFixer;
    private final CoreMcRegistries registries;
    private final CoreMcTransmogrifier transmogrifier;
    private final Lifecycled<MinecraftServer> server;
    private final Lifecycled<Watchdog> watchdog;
    private boolean hookingEvents = false;
    private CoreMcPermissionsProvider permissionsProvider;

    protected CoreMcPlatform(CoreMcMod mod, Lifecycled<MinecraftServer> server) {
        this.mod = mod;
        this.dataFixer = new CoreMcDataFixer(this, getDataVersion());
        this.registries = new CoreMcRegistries(this);
        this.transmogrifier = new CoreMcTransmogrifier(this);
        this.server = server;
        this.watchdog = server.flatMap(s -> {
            if (s instanceof DedicatedServer dedicatedServer) {
                return new ConstantLifecycled<>(new CoreMcWatchdog(dedicatedServer));
            } else {
                return SimpleLifecycled.invalid();
            }
        });
    }

    // region Methods to be implemented by subclasses that are not public API

    protected abstract void sendCUIPacket(ServerPlayer player, CUIPacket packet);

    protected void extraOnBlockStateChange(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
    }

    // endregion

    /**
     * {@return the platform-specific {@link CoreMcAdapter}}
     */
    public abstract CoreMcAdapter getAdapter();

    /**
     * {@return the registry access of this platform's server}
     */
    public RegistryAccess serverRegistryAccess() {
        return server.valueOrThrow().registryAccess();
    }

    public CoreMcMod getMod() {
        return mod;
    }

    /**
     * {@return the permissions provider for this platform}
     *
     * <p>Queried via {@link Capability#PERMISSIONS}.
     */
    public CoreMcPermissionsProvider getPermissionsProvider() {
        return permissionsProvider;
    }

    /**
     * Set the permissions provider for this platform.
     *
     * @param provider the permissions provider
     */
    public void setPermissionsProvider(CoreMcPermissionsProvider provider) {
        this.permissionsProvider = Objects.requireNonNull(provider);
    }

    public boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public Registries getRegistries() {
        return registries;
    }

    public CoreMcTransmogrifier getTransmogrifier() {
        return transmogrifier;
    }

    @Override
    public int getDataVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }

    @Override
    public DataFixer getDataFixer() {
        return dataFixer;
    }

    @Override
    public boolean isValidMobType(String type) {
        return BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(type));
    }

    @Override
    public void reload() {
        getConfiguration().load();
        super.reload();
    }

    @Override
    @Nullable
    public Watchdog getWatchdog() {
        return watchdog.value().orElse(null);
    }

    @Override
    public List<? extends World> getWorlds() {
        Iterable<ServerLevel> worlds = server.valueOrThrow().getAllLevels();
        List<World> ret = new ArrayList<>();
        for (ServerLevel world : worlds) {
            ret.add(new CoreMcWorld(this, world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof CoreMcPlayer) {
            return player;
        } else {
            ServerPlayer entity = server.valueOrThrow().getPlayerList().getPlayerByName(player.getName());
            return entity != null ? getAdapter().fromNativePlayer(entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof CoreMcWorld) {
            return world;
        } else {
            for (ServerLevel ws : server.valueOrThrow().getAllLevels()) {
                if (((ServerLevelData) ws.getLevelData()).getLevelName().equals(world.getName())) {
                    return new CoreMcWorld(this, ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(CommandManager manager) {
        // No-op, we register using the platform's event system.
    }

    @Override
    public void setGameHooksEnabled(boolean enabled) {
        this.hookingEvents = enabled;
    }

    @Override
    public CoreMcConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformVersion() {
        return getVersion();
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
    public Set<SideEffect> getSupportedSideEffects() {
        return SUPPORTED_SIDE_EFFECTS;
    }

    @Override
    public long getTickCount() {
        return server.valueOrThrow().getTickCount();
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        PlayerList scm = server.valueOrThrow().getPlayerList();
        for (ServerPlayer entity : scm.getPlayers()) {
            users.add(getAdapter().fromNativePlayer(entity));
        }
        return users;
    }
}
