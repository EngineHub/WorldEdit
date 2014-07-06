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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.command.tool.*;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.Interaction;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.internal.ServerInterfaceAdapter;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages registered {@link Platform}s for WorldEdit. Platforms are
 * implementations of WorldEdit.
 * </p>
 * This class is thread-safe.
 */
public class PlatformManager {

    private static final Logger logger = Logger.getLogger(PlatformManager.class.getCanonicalName());

    private final WorldEdit worldEdit;
    private final CommandManager commandManager;
    private final List<Platform> platforms = new ArrayList<Platform>();
    private final Map<Capability, Platform> preferences = new EnumMap<Capability, Platform>(Capability.class);
    private @Nullable String firstSeenVersion;

    /**
     * Create a new platform manager.
     *
     * @param worldEdit the WorldEdit instance
     */
    public PlatformManager(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
        this.commandManager = new CommandManager(worldEdit, this);

        // Register this instance for events
        worldEdit.getEventBus().register(this);
    }

    /**
     * Register a platform with WorldEdit.
     *
     * @param platform the platform
     */
    public synchronized void register(Platform platform) {
        checkNotNull(platform);

        logger.log(Level.FINE, "Got request to register " + platform.getClass() + " with WorldEdit [" + super.toString() + "]");

        // Just add the platform to the list of platforms: we'll pick favorites
        // once all the platforms have been loaded
        platforms.add(platform);

        // Make sure that versions are in sync
        if (firstSeenVersion != null) {
            if (!firstSeenVersion.equals(platform.getVersion())) {
                logger.log(Level.WARNING,
                        "\n**********************************************\n" +
                        "** You have WorldEdit installed for multiple platforms in the same \n" +
                        "** game/program. This is OK except that you have different WorldEdit versions\n" +
                        "** installed (i.e. {0} and {1}).\n" +
                        "**\n" +
                        "** WorldEdit has seen both versions {0} and {1}.\n" +
                        "**\n" +
                        "** Things may break! Please make sure that your WE versions are in sync.\n" +
                        "**********************************************\n",
                        new Object[]{
                                firstSeenVersion, platform.getVersion()
                        });
            }
        } else {
            firstSeenVersion = platform.getVersion();
        }
    }

    /**
     * Unregister a platform from WorldEdit.
     * </p>
     * If the platform has been chosen for any capabilities, then a new
     * platform will be found.
     *
     * @param platform the platform
     */
    public synchronized boolean unregister(Platform platform) {
        checkNotNull(platform);

        boolean removed = platforms.remove(platform);

        if (removed) {
            logger.log(Level.FINE, "Unregistering " + platform.getClass().getCanonicalName() + " from WorldEdit");

            boolean choosePreferred = false;

            // Check whether this platform was chosen to be the preferred one
            // for any capability and be sure to remove it
            Iterator<Entry<Capability, Platform>> it = preferences.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Capability, Platform> entry = it.next();
                if (entry.getValue().equals(platform)) {
                    entry.getKey().unload(this, entry.getValue());
                    it.remove();
                    choosePreferred = true; // Have to choose new favorites
                }
            }

            if (choosePreferred) {
                choosePreferred();
            }
        }

        return removed;
    }

    /**
     * Get the preferred platform for handling a certain capability. Returns
     * null if none is available.
     *
     * @param capability the capability
     * @return the platform
     * @throws NoCapablePlatformException thrown if no platform is capable
     */
    public synchronized Platform queryCapability(Capability capability) throws NoCapablePlatformException {
        Platform platform = preferences.get(checkNotNull(capability));
        if (platform != null) {
            return platform;
        } else {
            throw new NoCapablePlatformException("No platform was found supporting " + capability.name());
        }
    }

    /**
     * Choose preferred platforms and perform necessary initialization.
     */
    private synchronized void choosePreferred() {
        for (Capability capability : Capability.values()) {
            Platform preferred = findMostPreferred(capability);
            if (preferred != null) {
                preferences.put(capability, preferred);
                capability.initialize(this, preferred);
            }
        }
    }

    /**
     * Find the most preferred platform for a given capability from the list of
     * platforms. This does not use the map of preferred platforms.
     *
     * @param capability the capability
     * @return the most preferred platform, or null if no platform was found
     */
    private synchronized @Nullable Platform findMostPreferred(Capability capability) {
        Platform preferred = null;
        Preference highest = null;

        for (Platform platform : platforms) {
            Preference preference = platform.getCapabilities().get(capability);
            if (preference != null && (highest == null || preference.isPreferredOver(highest))) {
                preferred = platform;
                highest = preference;
            }
        }

        return preferred;
    }

    /**
     * Get a list of loaded platforms.
     * </p>
     * The returned list is a copy of the original and is mutable.
     *
     * @return a list of platforms
     */
    public synchronized List<Platform> getPlatforms() {
        return new ArrayList<Platform>(platforms);
    }

    /**
     * Given a world, possibly return the same world but using a different
     * platform preferred for world editing operations.
     *
     * @param base the world to match
     * @return the preferred world, if one was found, otherwise the given world
     */
    public World getWorldForEditing(World base) {
        checkNotNull(base);
        World match = queryCapability(Capability.WORLD_EDITING).matchWorld(base);
        return match != null ? match : base;
    }

    /**
     * Given an actor, return a new one that may use a different platform
     * for permissions and world editing.
     *
     * @param base the base actor to match
     * @return a new delegate actor
     */
    @SuppressWarnings("unchecked")
    public <T extends Actor> T createProxyActor(T base) {
        checkNotNull(base);

        if (base instanceof Player) {
            Player player = (Player) base;

            Player permActor = queryCapability(Capability.PERMISSIONS).matchPlayer(player);
            if (permActor == null) {
                permActor = player;
            }

            return (T) new PlayerProxy(player, permActor, getWorldForEditing(player.getWorld()));
        } else {
            return base;
        }
    }

    /**
     * Get the command manager.
     *
     * @return the command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Get the current configuration.
     * </p>
     * If no platform has been registered yet, then a default configuration
     * will be returned.
     *
     * @return the configuration
     */
    public LocalConfiguration getConfiguration() {
        return queryCapability(Capability.CONFIGURATION).getConfiguration();
    }

    /**
     * Return a legacy {@link ServerInterface}.
     *
     * @return a {@link ServerInterface}
     * @throws IllegalStateException if no platform has been registered
     */
    @SuppressWarnings("deprecation")
    public ServerInterface getServerInterface() throws IllegalStateException {
        return ServerInterfaceAdapter.adapt(queryCapability(Capability.USER_COMMANDS));
    }

    @Subscribe
    public void handlePlatformReady(PlatformReadyEvent event) {
        choosePreferred();
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void handleBlockInteract(BlockInteractEvent event) {
        // Create a proxy actor with a potentially different world for
        // making changes to the world
        Actor actor = createProxyActor(event.getCause());

        Location location = event.getLocation();
        Vector vector = location.toVector();

        // At this time, only handle interaction from players
        if (actor instanceof Player) {
            Player player = (Player) actor;
            LocalSession session = worldEdit.getSessionManager().get(actor);

            if (event.getType() == Interaction.HIT) {
                if (player.getItemInHand() == getConfiguration().wandItem) {
                    if (!session.isToolControlEnabled()) {
                        return;
                    }

                    if (!actor.hasPermission("worldedit.selection.pos")) {
                        return;
                    }

                    RegionSelector selector = session.getRegionSelector(player.getWorld());

                    if (selector.selectPrimary(location.toVector())) {
                        selector.explainPrimarySelection(actor, session, vector);
                    }

                    event.setCancelled(true);
                    return;
                }

                if (player.isHoldingPickAxe() && session.hasSuperPickAxe()) {
                    final BlockTool superPickaxe = session.getSuperPickaxe();
                    if (superPickaxe != null && superPickaxe.canUse(player)) {
                        event.setCancelled(superPickaxe.actPrimary(queryCapability(Capability.WORLD_EDITING), getConfiguration(), player, session, location));
                        return;
                    }
                }

                Tool tool = session.getTool(player.getItemInHand());
                if (tool != null && tool instanceof DoubleActionBlockTool) {
                    if (tool.canUse(player)) {
                        ((DoubleActionBlockTool) tool).actSecondary(queryCapability(Capability.WORLD_EDITING), getConfiguration(), player, session, location);
                        event.setCancelled(true);
                    }
                }

            } else if (event.getType() == Interaction.OPEN) {
                if (player.getItemInHand() == getConfiguration().wandItem) {
                    if (!session.isToolControlEnabled()) {
                        return;
                    }

                    if (!actor.hasPermission("worldedit.selection.pos")) {
                        return;
                    }

                    RegionSelector selector = session.getRegionSelector(player.getWorld());
                    if (selector.selectSecondary(vector)) {
                        selector.explainSecondarySelection(actor, session, vector);
                    }

                    event.setCancelled(true);
                    return;
                }

                Tool tool = session.getTool(player.getItemInHand());
                if (tool != null && tool instanceof BlockTool) {
                    if (tool.canUse(player)) {
                        ((BlockTool) tool).actPrimary(queryCapability(Capability.WORLD_EDITING), getConfiguration(), player, session, location);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void handlePlayerInput(PlayerInputEvent event) {
        // Create a proxy actor with a potentially different world for
        // making changes to the world
        Player player = createProxyActor(event.getPlayer());

        switch (event.getInputType()) {
            case PRIMARY: {
                if (player.getItemInHand() == getConfiguration().navigationWand) {
                    if (getConfiguration().navigationWandMaxDistance <= 0) {
                        return;
                    }

                    if (!player.hasPermission("worldedit.navigation.jumpto.tool")) {
                        return;
                    }

                    WorldVector pos = player.getSolidBlockTrace(getConfiguration().navigationWandMaxDistance);
                    if (pos != null) {
                        player.findFreePosition(pos);
                    } else {
                        player.printError("No block in sight (or too far)!");
                    }

                    event.setCancelled(true);
                    return;
                }

                LocalSession session = worldEdit.getSessionManager().get(player);

                Tool tool = session.getTool(player.getItemInHand());
                if (tool != null && tool instanceof DoubleActionTraceTool) {
                    if (tool.canUse(player)) {
                        ((DoubleActionTraceTool) tool).actSecondary(queryCapability(Capability.WORLD_EDITING), getConfiguration(), player, session);
                        event.setCancelled(true);
                        return;
                    }
                }

                break;
            }

            case SECONDARY: {
                if (player.getItemInHand() == getConfiguration().navigationWand) {
                    if (getConfiguration().navigationWandMaxDistance <= 0) {
                        return;
                    }

                    if (!player.hasPermission("worldedit.navigation.thru.tool")) {
                        return;
                    }

                    if (!player.passThroughForwardWall(40)) {
                        player.printError("Nothing to pass through!");
                    }

                    event.setCancelled(true);
                    return;
                }

                LocalSession session = worldEdit.getSessionManager().get(player);

                Tool tool = session.getTool(player.getItemInHand());
                if (tool != null && tool instanceof TraceTool) {
                    if (tool.canUse(player)) {
                        ((TraceTool) tool).actPrimary(queryCapability(Capability.WORLD_EDITING), getConfiguration(), player, session);
                        event.setCancelled(true);
                        return;
                    }
                }

                break;
            }
        }
    }


}
