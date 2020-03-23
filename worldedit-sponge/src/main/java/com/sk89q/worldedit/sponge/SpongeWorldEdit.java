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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

import com.google.inject.Inject;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.sponge.adapter.AdapterLoadException;
import com.sk89q.worldedit.sponge.adapter.SpongeImplAdapter;
import com.sk89q.worldedit.sponge.adapter.SpongeImplLoader;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The Sponge implementation of WorldEdit.
 */
@Plugin(id = SpongeWorldEdit.MOD_ID, name = "WorldEdit",
        description = "WorldEdit is an easy-to-use in-game world editor for Minecraft",
        url = "https://enginehub.org/worldedit/")
public class SpongeWorldEdit {

    @Inject
    private Logger logger;

    private Metrics2 metrics;

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
    private SpongeImplAdapter spongeAdapter;

    @Inject
    private SpongeConfiguration config;

    @Inject @ConfigDir(sharedRoot = false)
    private File workingDir;

    @Inject
    public SpongeWorldEdit(Metrics2.Factory metricsFactory) {
        inst = this;
        metrics = metricsFactory.make(BSTATS_PLUGIN_ID);
    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        // Load configuration
        config.load();

        Task.builder().interval(30, TimeUnit.SECONDS).execute(ThreadSafeCache.getInstance()).submit(this);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        CUIChannelHandler.init();
    }

    @Listener
    public void postInit(GamePostInitializationEvent event) {
        logger.info("WorldEdit for Sponge (version " + getInternalVersion() + ") is loaded");
    }

    @Listener
    public void serverAboutToStart(GameAboutToStartServerEvent event) {
        if (this.platform != null) {
            logger.warn("GameAboutToStartServerEvent occurred when GameStoppingServerEvent hasn't");
            WorldEdit.getInstance().getPlatformManager().unregister(platform);
        }

        final Path delChunks = workingDir.toPath().resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }

        this.platform = new SpongePlatform(this);
        this.provider = new SpongePermissionsProvider();

        for (BlockType blockType : Sponge.getRegistry().getAllOf(BlockType.class)) {
            // TODO Handle blockstate stuff
            String id = blockType.getId();
            if (!com.sk89q.worldedit.world.block.BlockType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.block.BlockType.REGISTRY.register(id, new com.sk89q.worldedit.world.block.BlockType(id));
            }
        }

        for (ItemType itemType : Sponge.getRegistry().getAllOf(ItemType.class)) {
            String id = itemType.getId();
            if (!com.sk89q.worldedit.world.item.ItemType.REGISTRY.keySet().contains(id)) {
                com.sk89q.worldedit.world.item.ItemType.REGISTRY.register(id, new com.sk89q.worldedit.world.item.ItemType(id));
            }
        }

        WorldEdit.getInstance().getPlatformManager().register(platform);
    }

    @Listener
    public void serverStopping(GameStoppingServerEvent event) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        worldEdit.getPlatformManager().unregister(platform);
    }

    @Listener
    public void serverStarted(GameStartedServerEvent event) {
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());

        loadAdapter();
    }

    private void loadAdapter() {
        WorldEdit worldEdit = WorldEdit.getInstance();

        // Attempt to load a Sponge adapter
        SpongeImplLoader adapterLoader = new SpongeImplLoader();

        try {
            adapterLoader.addFromPath(getClass().getClassLoader());
        } catch (IOException e) {
            logger.warn("Failed to search path for Sponge adapters");
        }

        try {
            adapterLoader.addFromJar(container.getSource().get().toFile());
        } catch (IOException e) {
            logger.warn("Failed to search " + container.getSource().get().toFile() + " for Sponge adapters", e);
        }
        try {
            spongeAdapter = adapterLoader.loadAdapter();
            logger.info("Using " + spongeAdapter.getClass().getCanonicalName() + " as the Sponge adapter");
        } catch (AdapterLoadException e) {
            Platform platform = worldEdit.getPlatformManager().queryCapability(Capability.WORLD_EDITING);
            if (platform instanceof SpongePlatform) {
                logger.warn(e.getMessage());
            } else {
                logger.info("WorldEdit could not find a Sponge adapter for this MC version, " +
                        "but it seems that you have another implementation of WorldEdit installed (" + platform.getPlatformName() + ") " +
                        "that handles the world editing.");
            }
        }
    }

    public SpongeImplAdapter getAdapter() {
        return this.spongeAdapter;
    }

    @Listener
    public void onPlayerItemInteract(InteractItemEvent.Secondary event, @Root Player spongePlayer) {
        if (platform == null) {
            return;
        }

        if (!platform.isHookingEvents()) return; // We have to be told to catch these events

        WorldEdit we = WorldEdit.getInstance();

        SpongePlayer player = wrapPlayer(spongePlayer);
        if (we.handleRightClick(player)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent event, @Root Player spongePlayer) {
        if (platform == null) {
            return;
        }

        if (!platform.isHookingEvents()) return; // We have to be told to catch these events

        WorldEdit we = WorldEdit.getInstance();

        SpongePlayer player = wrapPlayer(spongePlayer);
        com.sk89q.worldedit.world.World world = player.getWorld();

        BlockSnapshot targetBlock = event.getTargetBlock();
        Optional<Location<World>> optLoc = targetBlock.getLocation();

        BlockType interactedType = targetBlock.getState().getType();
        if (event instanceof InteractBlockEvent.Primary) {
            if (interactedType != BlockTypes.AIR) {
                if (!optLoc.isPresent()) {
                    return;
                }

                Location<World> loc = optLoc.get();
                com.sk89q.worldedit.util.Location pos = new com.sk89q.worldedit.util.Location(
                        world, loc.getX(), loc.getY(), loc.getZ());

                if (we.handleBlockLeftClick(player, pos)) {
                    event.setCancelled(true);
                }

                if (we.handleArmSwing(player)) {
                    event.setCancelled(true);
                }
            } else {
                if (we.handleArmSwing(player)) {
                    event.setCancelled(true);
                }
            }
        } else if (event instanceof InteractBlockEvent.Secondary) {
            if (!optLoc.isPresent()) {
                return;
            }

            Location<World> loc = optLoc.get();
            com.sk89q.worldedit.util.Location pos = new com.sk89q.worldedit.util.Location(
                    world, loc.getX(), loc.getY(), loc.getZ());

            if (we.handleBlockRightClick(player, pos)) {
                event.setCancelled(true);
            }

            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }

    public static ItemStack toSpongeItemStack(BaseItemStack item) {
        return inst().getAdapter().makeSpongeStack(item);
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
    public SpongePlayer wrapPlayer(Player player) {
        checkNotNull(player);
        return new SpongePlayer(platform, player);
    }

    public Actor wrapCommandSource(CommandSource sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new SpongeCommandSender(this, sender);
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(Player player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(wrapPlayer(player));
    }

    /**
     * Get the WorldEdit proxy for the given world.
     *
     * @param world the world
     * @return the WorldEdit world
     */
    public SpongeWorld getWorld(World world) {
        checkNotNull(world);
        return getAdapter().getWorld(world);
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
    public File getWorkingDir() {
        return this.workingDir;
    }

    /**
     * Get the version of the WorldEdit Sponge implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return container.getVersion().orElse("Unknown");
    }

    public void setPermissionsProvider(SpongePermissionsProvider provider) {
        this.provider = provider;
    }

    public SpongePermissionsProvider getPermissionsProvider() {
        return provider;
    }

}
