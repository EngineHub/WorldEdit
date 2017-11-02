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

package com.sk89q.worldedit.internal.expression;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.world.World;

import java.util.Map;

final class ExpressionPlatform extends AbstractPlatform {

    @Override
    public int resolveItem(String name) {
        return 0;
    }

    @Override
    public boolean isValidMobType(String type) {
        return false;
    }

    @Override
    public void reload() {
    }

    @Override
    public Player matchPlayer(Player player) {
        return null;
    }

    @Override
    public World matchWorld(World world) {
        return null;
    }

    @Override
    public void registerCommands(Dispatcher dispatcher) {
    }

    @Override
    public void registerGameHooks() {
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return new LocalConfiguration() {

            @Override
            public void load() {
            }
        };
    }

    @Override
    public String getVersion() {
        return "INVALID";
    }

    @Override
    public String getPlatformName() {
        return "Expression Test";
    }

    @Override
    public String getPlatformVersion() {
        return "INVALID";
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        return ImmutableMap.of(Capability.CONFIGURATION, Preference.PREFER_OTHERS);
    }
}
