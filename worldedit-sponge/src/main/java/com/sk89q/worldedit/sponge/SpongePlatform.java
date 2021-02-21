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
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import com.sk89q.worldedit.sponge.registry.SpongeRegistries;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import net.kyori.adventure.text.Component;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
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

    private RegisterCommandEvent<org.spongepowered.api.command.Command.Raw> commandRegisterEvent;

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
        return Sponge.getPlatform().getMinecraftVersion().getDataVersion().orElse(-1);
    }

    @Override
    public boolean isValidMobType(String type) {
        return RegistryTypes.ENTITY_TYPE.get().findEntry(ResourceKey.resolve(type)).isPresent();
    }

    @Override
    public void reload() {
        getConfiguration().load();
        super.reload();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        try {
            Task.builder()
                .delay(Ticks.of(delay))
                .interval(Ticks.of(period))
                .execute(task)
                .plugin(mod.getContainer())
                .build();
        } catch (IllegalStateException e) {
            // Thrown when it failed to schedule
            SpongeWorldEdit.inst().getLogger().warn("Failed to schedule a task", e);
            return -1;
        }
        return 0;
    }

    @Override
    public List<? extends com.sk89q.worldedit.world.World> getWorlds() {
        Collection<ServerWorld> worlds = Sponge.getServer().getWorldManager().worlds();
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
            Optional<ServerPlayer> optPlayer = Sponge.getServer().getPlayer(player.getUniqueId());
            return optPlayer.<Player>map(player1 -> new SpongePlayer(this, player1)).orElse(null);
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof SpongeWorld) {
            return world;
        } else {
            for (ServerWorld ws : Sponge.getServer().getWorldManager().worlds()) {
                if (ws.getKey().toString().equals(world.getName())) {
                    return SpongeAdapter.adapt(ws);
                }
            }

            return null;
        }
    }

    void setCommandRegisterEvent(RegisterCommandEvent<org.spongepowered.api.command.Command.Raw> commandRegisterEvent) {
        this.commandRegisterEvent = commandRegisterEvent;
    }

    private String rebuildArguments(String commandLabel, ArgumentReader.Mutable args) {
        int plSep = commandLabel.indexOf(":");
        if (plSep >= 0 && plSep < commandLabel.length() + 1) {
            commandLabel = commandLabel.substring(plSep + 1);
        }

        StringBuilder sb = new StringBuilder("/").append(commandLabel);
        if (args.getTotalLength() > 0) {
            sb.append(" ").append(args.getInput());
        }
        return sb.toString();
    }

    @Override
    public void registerCommands(CommandManager manager) {
        if (commandRegisterEvent == null) {
            return;
        }

        for (Command command : manager.getAllCommands().collect(toList())) {
            CommandAdapter adapter = new CommandAdapter(command) {
                @Override
                public CommandResult process(CommandCause source, ArgumentReader.Mutable arguments) throws CommandException {
                    CommandEvent weEvent = new CommandEvent(SpongeWorldEdit.inst().wrapCommandCause(source), rebuildArguments(command.getName(), arguments));
                    WorldEdit.getInstance().getEventBus().post(weEvent);
                    return weEvent.isCancelled() ? CommandResult.success() : CommandResult.empty();
                }

                @Override
                public List<String> getSuggestions(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
                    String args = rebuildArguments(command.getName(), arguments);
                    CommandSuggestionEvent weEvent = new CommandSuggestionEvent(SpongeWorldEdit.inst().wrapCommandCause(cause), args);
                    WorldEdit.getInstance().getEventBus().post(weEvent);
                    return CommandUtil.fixSuggestions(args, weEvent.getSuggestions());
                }

                @Override
                public Optional<Component> getExtendedDescription(CommandCause cause) {
                    return Optional.empty();
                }
            };
            commandRegisterEvent.register(SpongeWorldEdit.container(), adapter, command.getName(), command.getAliases().toArray(new String[0]));
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
    public String getId() {
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
        return ImmutableSet.of(SideEffect.UPDATE, SideEffect.NEIGHBORS, SideEffect.ENTITY_AI, SideEffect.LIGHTING);
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        for (ServerPlayer player : Sponge.getServer().getOnlinePlayers()) {
            users.add(new SpongePlayer(this, player));
        }
        return users;
    }
}
