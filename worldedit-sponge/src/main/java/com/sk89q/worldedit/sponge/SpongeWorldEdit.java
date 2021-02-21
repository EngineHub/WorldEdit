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

import com.google.inject.Inject;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import net.kyori.adventure.audience.Audience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterChannelEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

/**
 * The Sponge implementation of WorldEdit.
 */
@Plugin(value = SpongeWorldEdit.MOD_ID)
public class SpongeWorldEdit {

    private Logger logger = LoggerFactory.getLogger(SpongeWorldEdit.class);

    // private final Metrics2 metrics;

    public static final String MOD_ID = "worldedit";
    private static final int BSTATS_PLUGIN_ID = 3329;

    private SpongePermissionsProvider provider;

    @Inject
    private PluginContainer container;

    private static SpongeWorldEdit inst;

    public static PluginContainer container() {
        return inst.container;
    }

    public static SpongeWorldEdit inst() {
        return inst;
    }

    private SpongePlatform platform;

    @Inject
    private SpongeConfiguration config;

    @Inject @ConfigDir(sharedRoot = false)
    private Path workingDir;

    // Metrics2.Factory metricsFactory <- from constructor params
    @Inject
    public SpongeWorldEdit() {
        inst = this;
        // metrics = metricsFactory.make(BSTATS_PLUGIN_ID);
    }

    @Listener
    public void preInit(ConstructPluginEvent event) {
        // Load configuration
        config.load();

        Task.builder()
            .interval(30, TimeUnit.SECONDS)
            .execute(ThreadSafeCache.getInstance())
            .plugin(this.container)
            .build();

        if (this.platform != null) {
            logger.warn("StartingEngineEvent occurred when StoppingEngineEvent hasn't");
            WorldEdit.getInstance().getPlatformManager().unregister(platform);
        }

        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }

        this.platform = new SpongePlatform(this);
        this.provider = new SpongePermissionsProvider();

        WorldEdit.getInstance().getPlatformManager().register(platform);
    }

    @Listener
    public void serverStarting(StartingEngineEvent<Server> event) {
        RegistryTypes.BLOCK_TYPE.get().streamEntries().forEach(registryEntry -> {
            String id = registryEntry.key().getFormatted();
            if (!com.sk89q.worldedit.world.block.BlockType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.block.BlockType.REGISTRY.register(
                    id,
                    new com.sk89q.worldedit.world.block.BlockType(id,
                        blockState -> SpongeAdapter.adapt(SpongeAdapter.adapt(blockState.getBlockType()).getDefaultState()))
                );
            }
        });

        RegistryTypes.ITEM_TYPE.get().streamEntries().forEach(registryEntry -> {
            String id = registryEntry.key().getFormatted();
            if (!com.sk89q.worldedit.world.item.ItemType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.item.ItemType.REGISTRY.register(id, new com.sk89q.worldedit.world.item.ItemType(id));
            }
        });

        logger.info("WorldEdit for Sponge (version " + getInternalVersion() + ") is loaded");
    }

    @Listener
    public void serverStopping(StoppingEngineEvent<Server> event) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        worldEdit.getPlatformManager().unregister(platform);
    }

    @Listener
    public void serverStarted(StartedEngineEvent<Server> event) {
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
    }

    @Listener
    public void onRegisterChannel(RegisterChannelEvent event) {
        CUIChannelHandler.init(event);
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Raw> event) {
        platform.setCommandRegisterEvent(event);
        platform.registerCommands(WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getCommandManager());
        platform.setCommandRegisterEvent(null);
    }

    public PluginContainer getContainer() {
        return this.container;
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Listener
    public void onPlayerItemInteract(InteractItemEvent.Secondary event, @Root ServerPlayer spongePlayer) {
        if (platform == null) {
            return;
        }

        if (!platform.isHookingEvents()) {
            return; // We have to be told to catch these events
        }

        WorldEdit we = WorldEdit.getInstance();

        SpongePlayer player = wrapPlayer(spongePlayer);
        if (we.handleRightClick(player)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent event, @Root ServerPlayer spongePlayer) {
        if (platform == null) {
            return;
        }

        if (!platform.isHookingEvents()) {
            return; // We have to be told to catch these events
        }

        WorldEdit we = WorldEdit.getInstance();

        SpongePlayer player = wrapPlayer(spongePlayer);
        com.sk89q.worldedit.world.World world = player.getWorld();

        BlockSnapshot targetBlock = event.getBlock();
        Optional<ServerLocation> optLoc = targetBlock.getLocation();

        BlockType interactedType = targetBlock.getState().getType();
        if (event instanceof InteractBlockEvent.Primary.Start) {
            if (interactedType != BlockTypes.AIR.get()) {
                if (!optLoc.isPresent()) {
                    return;
                }

                ServerLocation loc = optLoc.get();
                com.sk89q.worldedit.util.Location pos = new com.sk89q.worldedit.util.Location(
                        world, loc.getX(), loc.getY(), loc.getZ());

                if (we.handleBlockLeftClick(player, pos)) {
                    ((InteractBlockEvent.Primary.Start) event).setCancelled(true);
                }

            }
            if (we.handleArmSwing(player)) {
                ((InteractBlockEvent.Primary.Start) event).setCancelled(true);
            }
        } else if (event instanceof InteractBlockEvent.Secondary) {
            if (!optLoc.isPresent()) {
                return;
            }

            ServerLocation loc = optLoc.get();
            com.sk89q.worldedit.util.Location pos = new com.sk89q.worldedit.util.Location(
                    world, loc.getX(), loc.getY(), loc.getZ());

            if (we.handleBlockRightClick(player, pos)) {
                ((InteractBlockEvent.Secondary) event).setCancelled(true);
            }

            if (we.handleRightClick(player)) {
                ((InteractBlockEvent.Secondary) event).setCancelled(true);
            }
        }
    }

    @Listener
    public void onPlayerQuit(ServerSideConnectionEvent.Disconnect event) {
        WorldEdit.getInstance().getEventBus()
            .post(new SessionIdleEvent(new SpongePlayer.SessionKeyImpl(event.getPlayer())));
    }

    /**
     * Get the configuration.
     *
     * @return the Sponge configuration
     */
    SpongeConfiguration getConfig() {
        return this.config;
    }

    /**
     * Get the WorldEdit proxy for the given player.
     *
     * @param player the player
     * @return the WorldEdit player
     */
    public SpongePlayer wrapPlayer(ServerPlayer player) {
        checkNotNull(player);
        return new SpongePlayer(platform, player);
    }

    public Actor wrapCommandCause(CommandCause cause) {
        Object rootCause = cause.getCause().root();
        if (rootCause instanceof ServerPlayer) {
            return wrapPlayer((ServerPlayer) rootCause);
        }
        if (rootCause instanceof Audience) {
            return new SpongeCommandSender(this, (Audience) rootCause);
        }
        // TODO CommandBlocks
        return null;
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(ServerPlayer player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(wrapPlayer(player));
    }

    /**
     * Get the WorldEdit proxy for the platform.
     *
     * @return the WorldEdit platform
     */
    public Platform getPlatform() {
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
        return container.getMetadata().getVersion();
    }

    public void setPermissionsProvider(SpongePermissionsProvider provider) {
        this.provider = provider;
    }

    public SpongePermissionsProvider getPermissionsProvider() {
        return provider;
    }

}
