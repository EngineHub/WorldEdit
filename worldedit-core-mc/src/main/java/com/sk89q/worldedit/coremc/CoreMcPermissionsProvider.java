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
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
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

    /**
     * Public API for other mods to get the current permissions provider.
     *
     * @return the current permissions provider
     */
    @SuppressWarnings("unused")
    static CoreMcPermissionsProvider current() {
        return getCoreMcPlatform().getPermissionsProvider();
    }

    /**
     * Public API for other mods to set the current permissions provider.
     *
     * @param provider the provider to set
     */
    @SuppressWarnings("unused")
    static void setCurrent(CoreMcPermissionsProvider provider) {
        getCoreMcPlatform().setPermissionsProvider(provider);
    }

    boolean hasPermission(ServerPlayer player, String permission);

    void registerPermission(String permission);
}
