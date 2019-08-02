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

package com.sk89q.worldedit.cli;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.registry.Registries;
import org.enginehub.piston.CommandManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

class CLIPlatform extends AbstractPlatform {

    private final CLIWorldEdit app;
    private int dataVersion = -1;

    private final List<World> worlds = new ArrayList<>();
    private final Timer timer = new Timer();
    private int lastTimerId = 0;

    CLIPlatform(CLIWorldEdit app) {
        this.app = app;
    }

    @Override
    public Registries getRegistries() {
        return CLIRegistries.getInstance();
    }

    @Override
    public int getDataVersion() {
        return this.dataVersion;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    @Override
    public DataFixer getDataFixer() {
        return null;
    }

    @Override
    public boolean isValidMobType(String type) {
        return EntityTypes.get(type) != null;
    }

    @Override
    public void reload() {
        getConfiguration().load();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
                if (period >= 0) {
                    timer.schedule(this, period);
                }
            }
        }, delay);
        return this.lastTimerId++;
    }

    @Override
    public List<? extends World> getWorlds() {
        return this.worlds;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        return null;
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        return this.worlds.stream()
                .filter(w -> w.getId().equals(world.getId()))
                .findAny()
                .orElse(null);
    }

    @Override
    public void registerCommands(CommandManager manager) {
    }

    @Override
    public void registerGameHooks() {
    }

    @Override
    public CLIConfiguration getConfiguration() {
        return app.getConfig();
    }

    @Override
    public String getVersion() {
        return app.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "CLI-Official";
    }

    @Override
    public String getPlatformVersion() {
        return app.getInternalVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.PREFER_OTHERS);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }
}
