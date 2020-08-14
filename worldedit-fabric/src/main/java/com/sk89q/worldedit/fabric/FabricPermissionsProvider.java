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

package com.sk89q.worldedit.fabric;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public interface FabricPermissionsProvider {

    boolean hasPermission(ServerPlayerEntity player, String permission);

    void registerPermission(String permission);

    class VanillaPermissionsProvider implements FabricPermissionsProvider {

        private final FabricPlatform platform;

        public VanillaPermissionsProvider(FabricPlatform platform) {
            this.platform = platform;
        }

        @Override
        public boolean hasPermission(ServerPlayerEntity player, String permission) {
            FabricConfiguration configuration = platform.getConfiguration();
            return configuration.cheatMode
                || player.server.getPlayerManager().isOperator(player.getGameProfile())
                || (configuration.creativeEnable && player.interactionManager.getGameMode() == GameMode.CREATIVE);
        }

        @Override
        public void registerPermission(String permission) {
        }
    }
}
