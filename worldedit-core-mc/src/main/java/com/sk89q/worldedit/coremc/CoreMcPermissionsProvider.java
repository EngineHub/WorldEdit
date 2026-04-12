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

package com.sk89q.worldedit.coremc;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.coremc.internal.CoreMcConfiguration;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.NonNull;

public interface CoreMcPermissionsProvider {

    private static @NonNull CoreMcPlatform getCoreMcPlatform() {
        Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.PERMISSIONS);
        if (!(platform instanceof CoreMcPlatform coreMcPlatform)) {
            throw new IllegalStateException(
                "Non-CoreMcPlatform is being used for capability " + Capability.PERMISSIONS + ": "
                    + platform.getClass()
            );
        }
        return coreMcPlatform;
    }

    static CoreMcPermissionsProvider current() {
        return getCoreMcPlatform().getPermissionsProvider();
    }

    // API for other mods to set a custom permissions provider.
    @SuppressWarnings("unused")
    static void setCurrent(CoreMcPermissionsProvider provider) {
        getCoreMcPlatform().setPermissionsProvider(provider);
    }

    boolean hasPermission(ServerPlayer player, String permission);

    void registerPermission(String permission);

    class VanillaPermissionsProvider implements CoreMcPermissionsProvider {

        private final CoreMcPlatform platform;

        public VanillaPermissionsProvider(CoreMcPlatform platform) {
            this.platform = platform;
        }

        @Override
        public boolean hasPermission(ServerPlayer player, String permission) {
            CoreMcConfiguration configuration = platform.getConfiguration();
            return configuration.cheatMode
                || player.level().getServer().getPlayerList().isOp(player.nameAndId())
                || (configuration.creativeEnable && player.gameMode.getGameModeForPlayer() == GameType.CREATIVE);
        }

        @Override
        public void registerPermission(String permission) {
        }
    }
}
