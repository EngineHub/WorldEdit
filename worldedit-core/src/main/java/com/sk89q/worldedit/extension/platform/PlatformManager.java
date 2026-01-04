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

package com.sk89q.worldedit.extension.platform;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.DoubleActionBlockTool;
import com.sk89q.worldedit.command.tool.DoubleActionTraceTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.TraceTool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.event.platform.Interaction;
import com.sk89q.worldedit.event.platform.PlatformInitializeEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.concurrency.EvenMoreExecutors;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import com.sk89q.worldedit.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.internal.util.SwitchEnhancements.dummyValue;
import static com.sk89q.worldedit.internal.util.SwitchEnhancements.exhaustive;

/**
 * Manages registered {@link Platform}s for WorldEdit. Platforms are
 * implementations of WorldEdit.
 *
 * <p>This class is thread-safe.</p>
 */
public class PlatformManager {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final WorldEdit worldEdit;
    private final PlatformCommandManager platformCommandManager;
    private final SimpleLifecycled<ListeningExecutorService> executorService;
    private final Map<Platform, Boolean> platforms = Maps.newHashMap();
    private final Map<Capability, Platform> preferences = new EnumMap<>(Capability.class);
    private @Nullable String firstSeenVersion;
    private final AtomicBoolean initialized = new AtomicBoolean();
    private final AtomicBoolean configured = new AtomicBoolean();

    /**
     * Create a new platform manager.
     *
     * @param worldEdit the WorldEdit instance
     */
    public PlatformManager(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
        this.platformCommandManager = new PlatformCommandManager(worldEdit, this);
        this.executorService = SimpleLifecycled.invalid();

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

        LOGGER.info("Got request to register " + platform.getClass() + " with WorldEdit [" + super.toString() + "]");

        // Just add the platform to the list of platforms: we'll pick favorites
        // once all the platforms have been loaded
        platforms.put(platform, false);

        // Make sure that versions are in sync
        if (firstSeenVersion != null) {
            if (!firstSeenVersion.equals(platform.getVersion())) {
                LOGGER.warn("Multiple ports of WorldEdit are installed but they report different versions ({} and {}). "
                        + "If these two versions are truly different, then you may run into unexpected crashes and errors.",
                    firstSeenVersion, platform.getVersion());
            }
        } else {
            firstSeenVersion = platform.getVersion();
        }
    }

    /**
     * Unregister a platform from WorldEdit.
     *
     * <p>If the platform has been chosen for any capabilities, then a new
     * platform will be found.</p>
     *
     * @param platform the platform
     */
    public synchronized boolean unregister(Platform platform) {
        checkNotNull(platform);

        boolean removed = platforms.remove(platform) != null;

        if (removed) {
            LOGGER.info("Unregistering " + platform.getClass().getCanonicalName() + " from WorldEdit");

            boolean choosePreferred = false;

            // Check whether this platform was chosen to be the preferred one
            // for any capability and be sure to remove it
            Iterator<Entry<Capability, Platform>> it = preferences.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Capability, Platform> entry = it.next();
                if (entry.getValue().equals(platform)) {
                    entry.getKey().uninitialize(this, entry.getValue());
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
     * Get the preferred platform for handling a certain capability. Throws if none are available.
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
            if (preferences.isEmpty()) {
                // Not all platforms registered, this is being called too early!
                throw new NoCapablePlatformException(
                    "Not all platforms have been registered yet!"
                        + " Please wait until WorldEdit is initialized."
                );
            }
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
                Platform oldPreferred = preferences.put(capability, preferred);
                // only (re)initialize if it changed
                if (preferred != oldPreferred) {
                    // uninitialize if needed
                    if (oldPreferred != null) {
                        capability.uninitialize(this, oldPreferred);
                    }
                    capability.initialize(this, preferred);
                }
            }
        }

        // Fire configuration event
        if (preferences.containsKey(Capability.CONFIGURATION) && configured.compareAndSet(false, true)) {
            worldEdit.getEventBus().post(new ConfigurationLoadEvent(queryCapability(Capability.CONFIGURATION).getConfiguration()));
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

        for (Platform platform : platforms.keySet()) {
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
     *
     * <p>The returned list is a copy of the original and is mutable.</p>
     *
     * @return a list of platforms
     */
    public synchronized List<Platform> getPlatforms() {
        return new ArrayList<>(platforms.keySet());
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

        if (base instanceof Player player) {
            Player permActor = queryCapability(Capability.PERMISSIONS).matchPlayer(player);
            if (permActor == null) {
                permActor = player;
            }

            Player cuiActor = queryCapability(Capability.WORLDEDIT_CUI).matchPlayer(player);
            if (cuiActor == null) {
                cuiActor = player;
            }

            return (T) new PlayerProxy(player, permActor, cuiActor, getWorldForEditing(player.getWorld()));
        } else {
            return base;
        }
    }

    /**
     * Get the command manager.
     *
     * @return the command manager
     */
    public PlatformCommandManager getPlatformCommandManager() {
        return platformCommandManager;
    }

    /**
     * Get the executor service. Internal, not for API use.
     *
     * @return the executor service
     */
    public ListeningExecutorService getExecutorService() {
        return executorService.valueOrThrow();
    }

    private static ListeningExecutorService createExecutor() {
        return MoreExecutors.listeningDecorator(
                EvenMoreExecutors.newBoundedCachedThreadPool(
                        0, 1, 20, "WorldEdit Task Executor - %s"));
    }

    /**
     * Get the current configuration.
     *
     * <p>If no platform has been registered yet, then a default configuration
     * will be returned.</p>
     *
     * @return the configuration
     */
    public LocalConfiguration getConfiguration() {
        return queryCapability(Capability.CONFIGURATION).getConfiguration();
    }

    /**
     * Get the current supported {@link SideEffect}s.
     *
     * @return the supported side effects
     * @throws NoCapablePlatformException thrown if no platform is capable
     */
    public Collection<SideEffect> getSupportedSideEffects() {
        return queryCapability(Capability.WORLD_EDITING).getSupportedSideEffects();
    }

    /**
     * Get the initialized state of the Platform.
     * @return if the platform manager is initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * You shouldn't have been calling this anyways, but this is now deprecated. Either don't
     * fire this event at all, or fire the new event via the event bus if you're a platform.
     */
    // Suppress InlineMeSuggester: This method cannot be made final due to backwards compatibility
    @SuppressWarnings("InlineMeSuggester")
    @Deprecated
    public void handlePlatformReady(@SuppressWarnings("unused") PlatformReadyEvent event) {
        handlePlatformsRegistered(new PlatformsRegisteredEvent());
    }

    /**
     * Internal, do not call.
     */
    @Subscribe
    public void handlePlatformsRegistered(PlatformsRegisteredEvent event) {
        choosePreferred();
        if (initialized.compareAndSet(false, true)) {
            worldEdit.getEventBus().post(new PlatformInitializeEvent());
        }
    }

    /**
     * Internal, do not call.
     */
    @Subscribe
    public void handleNewPlatformReady(PlatformReadyEvent event) {
        preferences.forEach((cap, platform) -> cap.ready(this, platform));
        platforms.put(event.getPlatform(), true);
        if (!executorService.isValid()) {
            executorService.newValue(createExecutor());
        }
    }

    /**
     * Internal, do not call.
     */
    @Subscribe
    public void handleNewPlatformUnready(PlatformUnreadyEvent event) {
        preferences.forEach((cap, platform) -> cap.unready(this, platform));
        platforms.put(event.getPlatform(), false);
        if (!platforms.containsValue(true)) {
            executorService.value().ifPresent(ListeningExecutorService::shutdownNow);
            executorService.invalidate();
        }
    }

    @Subscribe
    public void handleBlockInteract(BlockInteractEvent event) {
        // Create a proxy actor with a potentially different world for
        // making changes to the world
        Actor actor = createProxyActor(event.getCause());

        Location location = event.getLocation();

        // At this time, only handle interaction from players
        if (!(actor instanceof Player player)) {
            return;
        }
        LocalSession session = worldEdit.getSessionManager().get(actor);

        Request.reset();
        Request.request().setSession(session);
        Request.request().setWorld(player.getWorld());

        try {
            if (event.getType() == Interaction.HIT) {
                // superpickaxe is special because its primary interaction is a left click, not a right click
                // in addition, it is implicitly bound to all pickaxe items, not just a single tool item
                if (session.hasSuperPickAxe() && player.isHoldingPickAxe()) {
                    final BlockTool superPickaxe = session.getSuperPickaxe();
                    if (superPickaxe != null && superPickaxe.canUse(player)) {
                        if (superPickaxe.actPrimary(queryCapability(Capability.WORLD_EDITING),
                                getConfiguration(), player, session, location, event.getFace())) {
                            event.setCancelled(true);
                        }
                        return;
                    }
                }

                Tool tool = session.getTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
                if (tool instanceof DoubleActionBlockTool doubleActionBlockTool && tool.canUse(player)) {
                    if (doubleActionBlockTool.actSecondary(queryCapability(Capability.WORLD_EDITING),
                            getConfiguration(), player, session, location, event.getFace())) {
                        event.setCancelled(true);
                    }
                }

            } else if (event.getType() == Interaction.OPEN) {
                Tool tool = session.getTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
                if (tool instanceof BlockTool blockTool && tool.canUse(player)) {
                    if (blockTool.actPrimary(queryCapability(Capability.WORLD_EDITING),
                            getConfiguration(), player, session, location, event.getFace())) {
                        event.setCancelled(true);
                    }
                }
            }
        } finally {
            Request.reset();
        }
    }

    @Subscribe
    public void handlePlayerInput(PlayerInputEvent event) {
        // Create a proxy actor with a potentially different world for
        // making changes to the world
        Player player = createProxyActor(event.getPlayer());
        LocalSession session = worldEdit.getSessionManager().get(player);
        Request.reset();
        Request.request().setSession(session);
        Request.request().setWorld(player.getWorld());

        try {
            exhaustive(switch (event.getInputType()) {
                case PRIMARY -> {
                    Tool tool = session.getTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
                    if (tool instanceof DoubleActionTraceTool doubleActionTraceTool && tool.canUse(player)) {
                        if (doubleActionTraceTool.actSecondary(queryCapability(Capability.WORLD_EDITING),
                                getConfiguration(), player, session)) {
                            event.setCancelled(true);
                        }
                    }

                    yield dummyValue();
                }
                case SECONDARY -> {
                    Tool tool = session.getTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
                    if (tool instanceof TraceTool traceTool && tool.canUse(player)) {
                        if (traceTool.actPrimary(queryCapability(Capability.WORLD_EDITING),
                                getConfiguration(), player, session)) {
                            event.setCancelled(true);
                        }
                    }

                    yield dummyValue();
                }
            });
        } finally {
            Request.reset();
        }
    }


}
