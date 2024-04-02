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

package com.sk89q.worldedit.sponge;

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import org.enginehub.piston.CommandManager;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.toList;

class SpongePlatform extends AbstractPlatform implements MultiUserPlatform {

    private final SpongeWorldEdit mod;
    private boolean hookingEvents = false;
    private int nextTaskId = 0;

    SpongePlatform(SpongeWorldEdit mod) {
        this.mod = mod;
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public Registries getRegistries() {
        return SpongeRegistries.getInstance();
    }

    @Override
    public int getDataVersion() {
        return Sponge.platform().minecraftVersion().dataVersion().orElse(-1);
    }

    @Override
    public boolean isValidMobType(String type) {
        return Sponge.game().registry(RegistryTypes.ENTITY_TYPE)
            .findValue(ResourceKey.resolve(type)).isPresent();
    }

    @Override
    public void reload() {
        getConfiguration().load();
        super.reload();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        Sponge.server().scheduler().submit(Task.builder()
            .delay(Ticks.of(delay))
            .interval(Ticks.of(period))
            .execute(task)
            .plugin(SpongeWorldEdit.inst().getPluginContainer())
            .build());
        return nextTaskId++;
    }

    @Override
    public List<? extends com.sk89q.worldedit.world.World> getWorlds() {
        Collection<ServerWorld> worlds = Sponge.server().worldManager().worlds();
        List<com.sk89q.worldedit.world.World> ret = new ArrayList<>(worlds.size());
        for (ServerWorld world : worlds) {
            ret.add(SpongeAdapter.adapt(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof SpongePlayer) {
            return player;
        } else {
            Optional<ServerPlayer> optPlayer = Sponge.server().player(player.getUniqueId());
            return optPlayer.map(SpongePlayer::new).orElse(null);
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof SpongeWorld) {
            return world;
        } else {
            // TODO this needs fixing for world name shenanigans
            for (ServerWorld spongeWorld : Sponge.server().worldManager().worlds()) {
                if (spongeWorld.key().toString().equals(world.getName())) {
                    return SpongeAdapter.adapt(spongeWorld);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(CommandManager manager) {
    }

    @Override
    public void setGameHooksEnabled(boolean enabled) {
        this.hookingEvents = enabled;
    }

    @Override
    public SpongeConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "Sponge-Official";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String id() {
        return "enginehub:sponge";
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.NORMAL);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    @Override
    public Set<SideEffect> getSupportedSideEffects() {
        return ImmutableSet.of(
            SideEffect.UPDATE, SideEffect.ENTITY_AI, SideEffect.LIGHTING, SideEffect.NEIGHBORS
        );
    }

    @Override
    public long getTickCount() {
        return Sponge.server().runningTimeTicks().ticks();
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        return Sponge.server().onlinePlayers().stream().map(SpongePlayer::new).collect(toList());
    }
}
