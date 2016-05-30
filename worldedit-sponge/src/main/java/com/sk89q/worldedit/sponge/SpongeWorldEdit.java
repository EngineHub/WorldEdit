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

import com.google.inject.Inject;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.sponge.config.SpongeConfiguration;
import com.sk89q.worldedit.sponge.nms.NMSHelper;
import com.sk89q.worldedit.sponge.nms.SpongeNMSWorld;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Sponge implementation of WorldEdit.
 */
@Plugin(id = SpongeWorldEdit.MOD_ID, name = "WorldEdit",
        description = "WorldEdit is an easy-to-use in-game world editor for Minecraft",
        url = "http://www.enginehub.org/worldedit")
public class SpongeWorldEdit {

    @Inject
    private Logger logger;

    public static final String MOD_ID = "worldedit";

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
    private File workingDir;

    public SpongeWorldEdit() {
        inst = this;
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

        this.platform = new SpongePlatform(this);
        this.provider = new SpongePermissionsProvider();

        WorldEdit.getInstance().getPlatformManager().register(platform);
    }

    @Listener
    public void serverStopping(GameStoppingServerEvent event) {
        WorldEdit.getInstance().getPlatformManager().unregister(platform);
    }

    @Listener
    public void serverStarted(GameStartedServerEvent event) {
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
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
                WorldVector pos = new WorldVector(LocalWorldAdapter.adapt(world), loc.getX(), loc.getY(), loc.getZ());

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
            if (interactedType != BlockTypes.AIR) {
                if (!optLoc.isPresent()) {
                    return;
                }

                Location<World> loc = optLoc.get();
                WorldVector pos = new WorldVector(LocalWorldAdapter.adapt(world), loc.getX(), loc.getY(), loc.getZ());

                if (we.handleBlockRightClick(player, pos)) {
                    event.setCancelled(true);
                }

                if (we.handleRightClick(player)) {
                    event.setCancelled(true);
                }
            } else {
                if (we.handleRightClick(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public static ItemStack toSpongeItemStack(BaseItemStack item) {
        return NMSHelper.makeSpongeStack(item);
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
        return new SpongeNMSWorld(world);
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
