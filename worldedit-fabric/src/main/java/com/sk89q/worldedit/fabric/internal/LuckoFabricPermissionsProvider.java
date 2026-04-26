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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.coremc.CoreMcPermissionsProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;

final class LuckoFabricPermissionsProvider implements CoreMcPermissionsProvider {
    private final CoreMcPermissionsProvider fallback;

    LuckoFabricPermissionsProvider(CoreMcPermissionsProvider fallback) {
        this.fallback = fallback;
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permission) {
        return Permissions.getPermissionValue(player, permission)
            .orElseGet(() -> fallback.hasPermission(player, permission));
    }

    @Override
    public void registerPermission(String permission) {
    }
}
