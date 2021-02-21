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

package com.sk89q.worldedit.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectReference;

public class SpongePermissionsProvider {

    public boolean hasPermission(ServerPlayer player, String permission) {
        return player.hasPermission(permission);
    }

    public void registerPermission(Command command, String permission) {
        Sponge.getGame().getServiceProvider().getRegistration(PermissionService.class).ifPresent((permissionService -> {
            PermissionDescription.Builder permissionBuilder = permissionService.service().newDescriptionBuilder(SpongeWorldEdit.container());
            permissionBuilder.id(permission).register();
        }));
    }

    public String[] getGroups(ServerPlayer player) {
        return player.getParents().stream()
                .map(SubjectReference::getSubjectIdentifier)
                .toArray(String[]::new);
    }
}
