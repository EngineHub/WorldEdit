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

package com.sk89q.worldedit.command.util;

import com.sk89q.worldedit.extension.platform.Actor;
import org.enginehub.piston.Command;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.Set;

public class PermissionCondition implements Command.Condition {

    private static final Key<Actor> ACTOR_KEY = Key.of(Actor.class);

    private final Set<String> permissions;

    public PermissionCondition(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public boolean satisfied(InjectedValueAccess context) {
        return permissions.isEmpty() ||
            context.injectedValue(ACTOR_KEY)
            .map(actor -> permissions.stream().anyMatch(actor::hasPermission))
            .orElse(false);
    }
}
