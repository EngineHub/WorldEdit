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

package com.sk89q.worldedit.session;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.NavigationWand;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.session.storage.JsonFileSessionStore;
import com.sk89q.worldedit.session.storage.SessionStore;
import com.sk89q.worldedit.session.storage.VoidStore;
import com.sk89q.worldedit.util.concurrency.EvenMoreExecutors;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Session manager for WorldEdit.
 *
 * <p>Get a reference to one from {@link WorldEdit}.</p>
 *
 * <p>While this class is thread-safe, the returned session may not be.</p>
 */
public class SessionManager {

    public static int EXPIRATION_GRACE = 10 * 60 * 1000;
    private static final int FLUSH_PERIOD = 1000 * 30;
    private static final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
            EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 5, "WorldEdit Session Saver - %s"));
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static final Set<String> warnedInvalidTool = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Timer timer = new Timer("WorldEdit Session Manager");
    private final WorldEdit worldEdit;
    private final Map<UUID, SessionHolder> sessions = new HashMap<>();
    private SessionStore store = new VoidStore();

    /**
     * Create a new session manager.
     *
     * @param worldEdit a WorldEdit instance
     */
    public SessionManager(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;

        worldEdit.getEventBus().register(this);
        timer.schedule(new SessionTracker(), FLUSH_PERIOD, FLUSH_PERIOD);
    }

    /**
     * Get whether a session exists for the given owner.
     *
     * @param owner the owner
     * @return true if a session exists
     */
    public synchronized boolean contains(SessionOwner owner) {
        checkNotNull(owner);
        return sessions.containsKey(getKey(owner));
    }

    /**
     * Find a session by its name specified by {@link SessionKey#getName()}.
     *
     * @param name the name
     * @return the session, if found, otherwise {@code null}
     */
    @Nullable
    public synchronized LocalSession findByName(String name) {
        checkNotNull(name);
        for (SessionHolder holder : sessions.values()) {
            String test = holder.key.getName();
            if (name.equals(test)) {
                return holder.session;
            }
        }

        return null;
    }

    /**
     * Gets the session for an owner and return it if it exists, otherwise
     * return {@code null}.
     *
     * @param owner the owner
     * @return the session for the owner, if it exists
     */
    @Nullable
    public synchronized LocalSession getIfPresent(SessionOwner owner) {
        checkNotNull(owner);
        SessionHolder stored = sessions.get(getKey(owner));
        if (stored != null) {
            if (stored.sessionIdle && stored.key.isActive()) {
                stored.sessionIdle = false;
            }
            return stored.session;
        } else {
            return null;
        }
    }

    /**
     * Get the session for an owner and create one if one doesn't exist.
     *
     * @param owner the owner
     * @return a session
     */
    public synchronized LocalSession get(SessionOwner owner) {
        checkNotNull(owner);

        LocalSession session = getIfPresent(owner);
        LocalConfiguration config = worldEdit.getConfiguration();
        SessionKey sessionKey = owner.getSessionKey();

        // No session exists yet -- create one
        if (session == null) {
            try {
                session = store.load(getKey(sessionKey));
                session.postLoad();
            } catch (IOException e) {
                log.warn("Failed to load saved session", e);
                session = new LocalSession();
            }
            Request.request().setSession(session);

            session.setConfiguration(config);
            session.setBlockChangeLimit(config.defaultChangeLimit);
            session.setTimeout(config.calculationTimeout);
            try {
                String sessionItem = session.isWandItemDefault() ? null : session.getWandItem();
                setDefaultWand(sessionItem, config.wandItem, session, new SelectionWand());
            } catch (InvalidToolBindException e) {
                if (warnedInvalidTool.add("selwand")) {
                    log.warn("Invalid selection wand tool set in config. Tool will not be assigned: " + e.getItemType());
                }
            }
            try {
                String sessionItem = session.isNavWandItemDefault() ? null : session.getNavWandItem();
                setDefaultWand(sessionItem, config.navigationWand, session, new NavigationWand());
            } catch (InvalidToolBindException e) {
                if (warnedInvalidTool.add("navwand")) {
                    log.warn("Invalid navigation wand tool set in config. Tool will not be assigned: " + e.getItemType());
                }
            }
            session.compareAndResetDirty();

            // Remember the session regardless of if it's currently active or not.
            // And have the SessionTracker FLUSH inactive sessions.
            sessions.put(getKey(owner), new SessionHolder(sessionKey, session));
        }

        if (shouldBoundLimit(owner, "worldedit.limit.unrestricted", session.getBlockChangeLimit(), config.maxChangeLimit)) {
            session.setBlockChangeLimit(config.maxChangeLimit);
        }
        if (shouldBoundLimit(owner, "worldedit.timeout.unrestricted", session.getTimeout(), config.maxCalculationTimeout)) {
            session.setTimeout(config.maxCalculationTimeout);
        }

        // Have the session use inventory if it's enabled and the owner
        // doesn't have an override
        session.setUseInventory(config.useInventory
                && !(config.useInventoryOverride
                && (owner.hasPermission("worldedit.inventory.unrestricted")
                || (config.useInventoryCreativeOverride && (!(owner instanceof Player) || ((Player) owner).getGameMode() == GameModes.CREATIVE)))));

        // Force non-locatable actors to use placeAtPos1
        if (!(owner instanceof Locatable)) {
            session.setPlaceAtPos1(true);
        }

        return session;
    }

    private boolean shouldBoundLimit(SessionOwner owner, String permission, int currentLimit, int maxLimit) {
        if (maxLimit > -1) { // if max is finite
            return (currentLimit < 0 || currentLimit > maxLimit) // make sure current is finite and less than max
                    && !owner.hasPermission(permission); // unless user has unlimited permission
        }
        return false;
    }

    private void setDefaultWand(String sessionItem, String configItem, LocalSession session, Tool wand) throws InvalidToolBindException {
        ItemType wandItem = null;
        if (sessionItem != null) {
            wandItem = ItemTypes.get(sessionItem);
        }
        if (wandItem == null) {
            wandItem = ItemTypes.get(configItem);
        }
        if (wandItem != null) {
            session.setTool(wandItem, wand);
        }
    }

    /**
     * Save a map of sessions to disk.
     *
     * @param sessions a map of sessions to save
     * @return a future that completes on save or error
     */
    private ListenableFuture<?> commit(final Map<SessionKey, LocalSession> sessions) {
        checkNotNull(sessions);

        if (sessions.isEmpty()) {
            return Futures.immediateFuture(sessions);
        }

        return executorService.submit((Callable<Object>) () -> {
            Exception exception = null;

            for (Map.Entry<SessionKey, LocalSession> entry : sessions.entrySet()) {
                SessionKey key = entry.getKey();

                if (key.isPersistent()) {
                    try {
                        store.save(getKey(key), entry.getValue());
                    } catch (IOException e) {
                        log.warn("Failed to write session for UUID " + getKey(key), e);
                        exception = e;
                    }
                }
            }

            if (exception != null) {
                throw exception;
            }

            return sessions;
        });
    }

    /**
     * Get the key to use in the map for an owner.
     *
     * @param owner the owner
     * @return the key object
     */
    protected UUID getKey(SessionOwner owner) {
        return getKey(owner.getSessionKey());
    }


    /**
     * Get the key to use in the map for a {@code SessionKey}.
     *
     * @param key the session key object
     * @return the key object
     */
    protected UUID getKey(SessionKey key) {
        return key.getUniqueId();
    }

    /**
     * Remove the session for the given owner if one exists.
     *
     * @param owner the owner
     */
    public synchronized void remove(SessionOwner owner) {
        checkNotNull(owner);
        sessions.remove(getKey(owner));
    }

    /**
     * Called to unload this session manager.
     */
    public synchronized void unload() {
        clear();
        timer.cancel();
    }

    /**
     * Remove all sessions.
     */
    public synchronized void clear() {
        saveChangedSessions();
        sessions.clear();
    }

    private synchronized void saveChangedSessions() {
        long now = System.currentTimeMillis();
        Iterator<SessionHolder> it = sessions.values().iterator();
        Map<SessionKey, LocalSession> saveQueue = new HashMap<>();

        while (it.hasNext()) {
            SessionHolder stored = it.next();
            if (stored.key.isActive()) {
                stored.lastActive = now;

                if (stored.session.compareAndResetDirty()) {
                    saveQueue.put(stored.key, stored.session);
                }
            } else {
                if (now - stored.lastActive > EXPIRATION_GRACE) {
                    if (stored.session.compareAndResetDirty()) {
                        saveQueue.put(stored.key, stored.session);
                    }

                    it.remove();
                }
            }
        }

        if (!saveQueue.isEmpty()) {
            commit(saveQueue);
        }
    }

    @Subscribe
    public void onConfigurationLoad(ConfigurationLoadEvent event) {
        LocalConfiguration config = event.getConfiguration();
        File dir = new File(config.getWorkingDirectory(), "sessions");
        store = new JsonFileSessionStore(dir);
    }

    @Subscribe
    public void onSessionIdle(final SessionIdleEvent event) {
        SessionHolder holder = this.sessions.get(getKey(event.getKey()));
        if (holder != null && !holder.sessionIdle) {
            holder.sessionIdle = true;
            LocalSession session = holder.session;

            // Perform any session cleanup for data that should not be persisted.
            session.onIdle();
        }
    }

    /**
     * Stores the owner of a session, the session, and the last active time.
     */
    private static final class SessionHolder {
        private final SessionKey key;
        private final LocalSession session;
        private long lastActive = System.currentTimeMillis();
        private boolean sessionIdle = false;

        private SessionHolder(SessionKey key, LocalSession session) {
            this.key = key;
            this.session = session;
        }
    }

    /**
     * Removes inactive sessions after they have been inactive for a period
     * of time. Commits them as well.
     */
    private class SessionTracker extends TimerTask {
        @Override
        public void run() {
            synchronized (SessionManager.this) {
                saveChangedSessions();
            }
        }
    }

}
