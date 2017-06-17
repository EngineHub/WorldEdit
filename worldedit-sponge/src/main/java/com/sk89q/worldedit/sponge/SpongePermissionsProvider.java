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

package com.sk89q.worldedit.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;

import java.util.stream.Collectors;

public class SpongePermissionsProvider {

    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    public void registerPermission(CommandCallable command, String permission) {
        Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).ifPresent((permissionService -> {
            PermissionDescription.Builder permissionBuilder = permissionService.getProvider().newDescriptionBuilder(SpongeWorldEdit.inst()).get();
            permissionBuilder.id(permission).register();
        }));
    }

    public String[] getGroups(Player player) {
        PermissionService permissionService = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
        return player.getParents().stream()
                .filter(subject -> subject.getContainingCollection().equals(permissionService.getGroupSubjects()))
                .map(Contextual::getIdentifier).collect(Collectors.toList()).toArray(new String[0]);
    }
}
