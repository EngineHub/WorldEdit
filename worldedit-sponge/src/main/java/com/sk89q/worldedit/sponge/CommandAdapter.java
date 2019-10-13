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

import com.sk89q.worldedit.command.util.PermissionCondition;
import org.enginehub.piston.Command;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.sk89q.worldedit.sponge.SpongeTextAdapter.convert;

public abstract class CommandAdapter implements CommandCallable {
    private Command command;

    protected CommandAdapter(Command command) {
        this.command = command;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        Set<String> permissions = command.getCondition().as(PermissionCondition.class)
            .map(PermissionCondition::getPermissions)
            .orElseGet(Collections::emptySet);
        for (String perm : permissions) {
            if (source.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(command.getDescription())
            .map(desc -> SpongeTextAdapter.convert(desc, source.getLocale()));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(command.getFullHelp())
            .map(help -> SpongeTextAdapter.convert(help, source.getLocale()));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return convert(command.getUsage(), source.getLocale());
    }
}
