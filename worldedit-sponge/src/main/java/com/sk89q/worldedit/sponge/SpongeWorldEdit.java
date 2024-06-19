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

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.internal.event.InteractionDebouncer;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.item.ItemCategory;
import net.kyori.adventure.audience.Audience;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;
import static java.util.stream.Collectors.toList;

/**
 * The Sponge implementation of WorldEdit.
 */
@Plugin(SpongeWorldEdit.MOD_ID)
public class SpongeWorldEdit {

    public static final String MOD_ID = "worldedit";
    private static final int BSTATS_PLUGIN_ID = 3329;

    private static SpongeWorldEdit inst;

    public static SpongeWorldEdit inst() {
        return inst;
    }

    private final Logger logger;
    private final PluginContainer container;
    private final SpongeConfiguration config;
    private final Path workingDir;

    private InteractionDebouncer debouncer;
    private SpongePermissionsProvider provider;
    private SpongePlatform platform;

    @Inject
    public SpongeWorldEdit(Logger logger,
                           PluginContainer container,
                           SpongeConfiguration config,
                           Metrics.Factory metricsFactory,
                           @ConfigDir(sharedRoot = false)
                               Path workingDir) {
        this.logger = logger;
        this.container = container;
        this.config = config;
        this.workingDir = workingDir;
        metricsFactory.make(BSTATS_PLUGIN_ID);
        inst = this;
    }

    @Listener
    public void onPluginConstruction(ConstructPluginEvent event) {
        this.platform = new SpongePlatform(this);
        debouncer = new InteractionDebouncer(platform);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        this.provider = new SpongePermissionsProvider();

        event.game().eventManager().registerListeners(
            container,
            new CUIChannelHandler.RegistrationHandler()
        );
        logger.info("WorldEdit for Sponge (version " + getInternalVersion() + ") is loaded");
    }

    @Listener
    public void serverStarting(StartingEngineEvent<Server> event) {
        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }
    }

    @Listener
    public void serverStarted(StartedEngineEvent<Server> event) {
        event.engine().scheduler().submit(Task.builder()
                .plugin(container)
                .interval(30, TimeUnit.SECONDS)
                .execute(ThreadSafeCache.getInstance())
                .build());

        event.game().registry(RegistryTypes.BLOCK_TYPE).streamEntries().forEach(blockType -> {
            String id = blockType.key().asString();
            if (!com.sk89q.worldedit.world.block.BlockType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.block.BlockType.REGISTRY.register(id, new com.sk89q.worldedit.world.block.BlockType(
                    id,
                    input -> {
                        BlockType spongeBlockType = Sponge.game().registry(RegistryTypes.BLOCK_TYPE).value(
                            ResourceKey.resolve(input.getBlockType().id())
                        );
                        return SpongeAdapter.adapt(spongeBlockType.defaultState());
                    }
                ));
            }
        });

        event.game().registry(RegistryTypes.ITEM_TYPE).streamEntries().forEach(itemType -> {
            String id = itemType.key().asString();
            if (!com.sk89q.worldedit.world.item.ItemType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.item.ItemType.REGISTRY.register(id, new com.sk89q.worldedit.world.item.ItemType(id));
            }
        });

        event.game().registry(RegistryTypes.ENTITY_TYPE).streamEntries().forEach(entityType -> {
            String id = entityType.key().asString();
            if (!com.sk89q.worldedit.world.entity.EntityType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.register(id, new com.sk89q.worldedit.world.entity.EntityType(id));
            }
        });

        for (ServerWorld world : event.engine().worldManager().worlds()) {
            world.registry(RegistryTypes.BIOME).streamEntries().forEach(biomeType -> {
                String id = biomeType.key().asString();
                if (!BiomeType.REGISTRY.keySet().contains(id)) {
                    BiomeType.REGISTRY.register(id, new BiomeType(id));
                }
            });
        }

        // Disabled until https://github.com/SpongePowered/SpongeAPI/issues/2520 is resolved
        // Will also need implementations in SpongeWorld to do placement
        //        Sponge.server().registry(RegistryTypes.FEATURE).streamEntries().forEach(feature -> {
        //            String id = feature.key().asString();
        //            if (!ConfiguredFeatureType.REGISTRY.keySet().contains(id)) {
        //                ConfiguredFeatureType.REGISTRY.register(id, new ConfiguredFeatureType(id));
        //            }
        //        });
        //        Sponge.server().registry(RegistryTypes.STRUCTURE).streamEntries().forEach(structure -> {
        //            String id = structure.key().asString();
        //            if (!StructureType.REGISTRY.keySet().contains(id)) {
        //                StructureType.REGISTRY.register(id, new StructureType(id));
        //            }
        //        });

        event.game().registry(RegistryTypes.BLOCK_TYPE).tags().forEach(blockTypeTag -> {
            String id = blockTypeTag.key().asString();
            if (!BlockCategory.REGISTRY.keySet().contains(id)) {
                BlockCategory.REGISTRY.register(id, new BlockCategory(id));
            }
        });
        event.game().registry(RegistryTypes.ITEM_TYPE).tags().forEach(itemTypeTag -> {
            String id = itemTypeTag.key().asString();
            if (!ItemCategory.REGISTRY.keySet().contains(id)) {
                ItemCategory.REGISTRY.register(id, new ItemCategory(id));
            }
        });
        Sponge.server().registry(RegistryTypes.BIOME).tags().forEach(biomeTag -> {
            String id = biomeTag.key().asString();
            if (!BiomeCategory.REGISTRY.keySet().contains(id)) {
                BiomeCategory.REGISTRY.register(id, new BiomeCategory(id, () -> event.game().registry(RegistryTypes.BIOME).taggedValues(biomeTag).stream().map(SpongeAdapter::adapt).collect(Collectors.toSet())));
            }
        });

        config.load();
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(platform));
    }

    @Listener
    public void serverStopping(StoppingEngineEvent<Server> event) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        WorldEdit.getInstance().getEventBus().post(new PlatformUnreadyEvent(platform));
    }

    @Listener
    public void registerCommand(RegisterCommandEvent<Command.Raw> event) {
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());
        PlatformManager manager = WorldEdit.getInstance().getPlatformManager();
        Platform commandsPlatform = manager.queryCapability(Capability.USER_COMMANDS);
        if (commandsPlatform != platform || !platform.isHookingEvents()) {
            // We're not in control of commands/events -- do not register.
            return;
        }

        List<org.enginehub.piston.Command> commands = manager.getPlatformCommandManager().getCommandManager()
            .getAllCommands().toList();
        for (org.enginehub.piston.Command command : commands) {
            registerAdaptedCommand(event, command);

            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(getPermissionsProvider()::registerPermission);
            }
        }
    }

    private String rebuildArguments(String commandLabel, String args) {
        int plSep = commandLabel.indexOf(':');
        if (plSep >= 0 && plSep < commandLabel.length() + 1) {
            commandLabel = commandLabel.substring(plSep + 1);
        }

        StringBuilder sb = new StringBuilder("/").append(commandLabel);

        String[] split = args.split(" ", -1);
        if (split.length > 0) {
            sb.append(" ");
        }
        return Joiner.on(" ").appendTo(sb, split).toString();
    }

    private void registerAdaptedCommand(RegisterCommandEvent<Command.Raw> event, org.enginehub.piston.Command command) {
        CommandAdapter adapter = new CommandAdapter(command) {
            @Override
            public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) {
                CommandEvent weEvent = new CommandEvent(SpongeWorldEdit.inst().wrapCommandCause(cause), rebuildArguments(command.getName(), arguments.remaining()).trim());
                WorldEdit.getInstance().getEventBus().post(weEvent);
                return weEvent.isCancelled() ? CommandResult.success() : CommandResult.builder().build();
            }

            @Override
            public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
                String args = rebuildArguments(command.getName(), arguments.remaining());
                CommandSuggestionEvent weEvent = new CommandSuggestionEvent(SpongeWorldEdit.inst().wrapCommandCause(cause), args);
                WorldEdit.getInstance().getEventBus().post(weEvent);
                return CommandUtil.fixSuggestions(args, weEvent.getSuggestions())
                    .stream().map(CommandCompletion::of).collect(toList());
            }
        };
        event.register(
            container, adapter, command.getName(), command.getAliases().toArray(new String[0])
        );
    }

    private boolean skipEvents() {
        return platform == null || !platform.isHookingEvents();
    }

    private boolean skipInteractionEvent(InteractEvent event) {
        return skipEvents() || event.context().get(EventContextKeys.USED_HAND).orElse(null) != HandTypes.MAIN_HAND.get();
    }

    @Listener
    public void onPlayerInteractItemPrimary(InteractItemEvent.Primary event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return;
        }

        boolean result = we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);
    }

    @Listener
    public void onPlayerInteractItemSecondary(InteractItemEvent.Secondary event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            if (previousResult.get()) {
                event.setCancelled(true);
            }
            return;
        }

        boolean result = we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        if (result) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractBlockPrimary(InteractBlockEvent.Primary.Start event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        BlockSnapshot targetBlock = event.block();
        Optional<ServerLocation> optLoc = targetBlock.location();

        boolean result = false;
        if (optLoc.isPresent()) {
            ServerLocation loc = optLoc.get();
            com.sk89q.worldedit.util.Location pos = SpongeAdapter.adapt(loc, Vector3d.ZERO);

            result = we.handleBlockLeftClick(player, pos, SpongeAdapter.adapt(event.targetSide()));
        }

        result = we.handleArmSwing(player) || result;
        debouncer.setLastInteraction(player, result);

        if (result) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractBlockSecondary(InteractBlockEvent.Secondary event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        BlockSnapshot targetBlock = event.block();
        Optional<ServerLocation> optLoc = targetBlock.location();

        boolean result = false;
        if (optLoc.isPresent()) {
            ServerLocation loc = optLoc.get();
            com.sk89q.worldedit.util.Location pos = SpongeAdapter.adapt(loc, Vector3d.ZERO);

            result = we.handleBlockRightClick(player, pos, SpongeAdapter.adapt(event.targetSide()));
        }

        result = we.handleRightClick(player) || result;
        debouncer.setLastInteraction(player, result);

        if (result) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerQuit(ServerSideConnectionEvent.Disconnect event) {
        event.profile().ifPresent(profile -> {
            debouncer.clearInteraction(profile::uniqueId);

            WorldEdit.getInstance().getEventBus()
                .post(new SessionIdleEvent(new SpongePlayer.SessionKeyImpl(profile.uniqueId(), profile.name().orElseThrow())));
        });
    }

    public PluginContainer getPluginContainer() {
        return container;
    }

    /**
     * Get the configuration.
     *
     * @return the Sponge configuration
     */
    SpongeConfiguration getConfig() {
        return this.config;
    }

    public Actor wrapCommandCause(CommandCause cause) {
        Object rootCause = cause.root();
        if (rootCause instanceof ServerPlayer) {
            return SpongeAdapter.adapt((ServerPlayer) rootCause);
        }
        if (rootCause instanceof LocatableBlock locatableBlock) {
            Optional<? extends BlockEntity> optionalBlockEntity = locatableBlock.world().blockEntity(locatableBlock.blockPosition());
            if (optionalBlockEntity.isPresent()) {
                BlockEntity blockEntity = optionalBlockEntity.get();
                if (blockEntity instanceof CommandBlock commandBlock) {
                    return new SpongeBlockCommandSender(this, commandBlock);
                }
            }
        }
        if (rootCause instanceof Audience) {
            return new SpongeCommandSender((Audience) rootCause);
        }

        throw new UnsupportedOperationException("Cannot wrap " + rootCause.getClass());
    }


    /**
     * Get the WorldEdit proxy for the platform.
     *
     * @return the WorldEdit platform
     */
    public Platform getPlatform() {
        return this.platform;
    }

    SpongePlatform getInternalPlatform() {
        return this.platform;
    }

    /**
     * Get the working directory where WorldEdit's files are stored.
     *
     * @return the working directory
     */
    public Path getWorkingDir() {
        return this.workingDir;
    }

    /**
     * Get the version of the WorldEdit Sponge implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return container.metadata().version().toString();
    }

    public void setPermissionsProvider(SpongePermissionsProvider provider) {
        this.provider = provider;
    }

    public SpongePermissionsProvider getPermissionsProvider() {
        return provider;
    }

}
