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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

class SpongePlatform extends AbstractPlatform implements MultiUserPlatform {

    private final SpongeWorldEdit mod;
    private boolean hookingEvents = false;

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
    public boolean isValidMobType(String type) {
        return Sponge.getRegistry().getType(EntityType.class, type).isPresent();
    }

    @Override
    public void reload() {
        getConfiguration().load();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        Task.builder().delayTicks(delay).intervalTicks(period).execute(task).submit(SpongeWorldEdit.inst());
        return 0; // TODO This isn't right, but we only check for -1 values
    }

    @Override
    public List<? extends com.sk89q.worldedit.world.World> getWorlds() {
        Collection<org.spongepowered.api.world.World> worlds = Sponge.getServer().getWorlds();
        List<com.sk89q.worldedit.world.World> ret = new ArrayList<>(worlds.size());
        for (org.spongepowered.api.world.World world : worlds) {
            ret.add(SpongeWorldEdit.inst().getAdapter().getWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof SpongePlayer) {
            return player;
        } else {
            Optional<org.spongepowered.api.entity.living.player.Player> optPlayer = Sponge.getServer().getPlayer(player.getUniqueId());
            return optPlayer.<Player>map(player1 -> new SpongePlayer(this, player1)).orElse(null);
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof SpongeWorld) {
            return world;
        } else {
            for (org.spongepowered.api.world.World ws : Sponge.getServer().getWorlds()) {
                if (ws.getName().equals(world.getName())) {
                    return SpongeWorldEdit.inst().getAdapter().getWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(Dispatcher dispatcher) {
        for (CommandMapping command : dispatcher.getCommands()) {
            CommandAdapter adapter = new CommandAdapter(command) {
                @Override
                public CommandResult process(CommandSource source, String arguments) throws org.spongepowered.api.command.CommandException {
                    CommandEvent weEvent = new CommandEvent(SpongeWorldEdit.inst().wrapCommandSource(source), command.getPrimaryAlias() + " " + arguments);
                    WorldEdit.getInstance().getEventBus().post(weEvent);
                    return weEvent.isCancelled() ? CommandResult.success() : CommandResult.empty();
                }

                @Override
                public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<org.spongepowered.api.world.World> targetPosition) throws CommandException {
                    CommandSuggestionEvent weEvent = new CommandSuggestionEvent(SpongeWorldEdit.inst().wrapCommandSource(source), command.getPrimaryAlias() + " " + arguments);
                    WorldEdit.getInstance().getEventBus().post(weEvent);
                    return weEvent.getSuggestions();
                }
            };
            Sponge.getCommandManager().register(SpongeWorldEdit.inst(), adapter, command.getAllAliases());
        }
    }

    @Override
    public void registerGameHooks() {
        // We registered the events already anyway, so we just 'turn them on'
        hookingEvents = true;
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
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        for (org.spongepowered.api.entity.living.player.Player player : Sponge.getServer().getOnlinePlayers()) {
            users.add(new SpongePlayer(this, player));
        }
        return users;
    }
}
