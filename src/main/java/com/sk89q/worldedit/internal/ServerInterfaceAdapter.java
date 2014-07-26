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

package com.sk89q.worldedit.internal;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts {@link Platform}s into the legacy {@link ServerInterface}.
 */
@SuppressWarnings("ALL")
public class ServerInterfaceAdapter extends ServerInterface {

    private final Platform platform;

    /**
     * Create a new adapter.
     *
     * @param platform the platform
     */
    private ServerInterfaceAdapter(Platform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    @Override
    public int resolveItem(String name) {
        return platform.resolveItem(name);
    }

    @Override
    public boolean isValidMobType(String type) {
        return platform.isValidMobType(type);
    }

    @Override
    public void reload() {
        platform.reload();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return platform.schedule(delay, period, task);
    }

    @Override
    public List<? extends World> getWorlds() {
        return platform.getWorlds();
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        return platform.matchPlayer(player);
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        return platform.matchWorld(world);
    }

    @Override
    public void registerCommands(Dispatcher dispatcher) {
        platform.registerCommands(dispatcher);
    }

    @Override
    public void registerGameHooks() {
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return platform.getConfiguration();
    }

    @Override
    public String getVersion() {
        return platform.getVersion();
    }

    @Override
    public String getPlatformName() {
        return platform.getPlatformName();
    }

    @Override
    public String getPlatformVersion() {
        return platform.getPlatformVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        return platform.getCapabilities();
    }

    /**
     * Adapt an {@link Platform} instance into a {@link ServerInterface}.
     *
     * @param platform the platform
     * @return the server interface
     */
    public static ServerInterface adapt(Platform platform) {
        return new ServerInterfaceAdapter(platform);
    }

}
