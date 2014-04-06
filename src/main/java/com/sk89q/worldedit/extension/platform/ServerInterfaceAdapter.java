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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.world.World;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts {@link Platform}s into the legacy {@link ServerInterface}.
 */
class ServerInterfaceAdapter extends ServerInterface {

    private final Platform platform;

    /**
     * Create a new adapter.
     *
     * @param platform the platform
     */
    ServerInterfaceAdapter(Platform platform) {
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
    public BiomeTypes getBiomes() {
        return platform.getBiomes();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return platform.schedule(delay, period, task);
    }

    @Override
    public List<? extends World> getWorlds() {
        return platform.getWorlds();
    }

    @Override
    @Deprecated
    public void onCommandRegistration(List<Command> commands) {
        platform.onCommandRegistration(commands);
    }

    @Override
    public void onCommandRegistration(List<Command> commands, CommandsManager<LocalPlayer> manager) {
        platform.onCommandRegistration(commands, manager);
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

}
