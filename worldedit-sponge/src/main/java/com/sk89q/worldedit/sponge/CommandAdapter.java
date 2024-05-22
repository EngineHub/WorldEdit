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

import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.sponge.internal.LocaleResolver;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.enginehub.piston.Command;
import org.spongepowered.api.command.CommandCause;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public abstract class CommandAdapter implements org.spongepowered.api.command.Command.Raw {
    private final Command command;

    protected CommandAdapter(Command command) {
        this.command = command;
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        Set<String> permissions = command.getCondition().as(PermissionCondition.class)
            .map(PermissionCondition::getPermissions)
            .orElseGet(Collections::emptySet);

        // Allow commands without permission nodes to always execute.
        if (permissions.isEmpty()) {
            return true;
        }

        for (String perm : permissions) {
            if (cause.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.of(command.getDescription())
            .map(desc -> SpongeTextAdapter.convert(desc, LocaleResolver.resolveLocale(cause.audience())));
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return command.getFooter()
            .map(footer -> SpongeTextAdapter.convert(footer, LocaleResolver.resolveLocale(cause.audience())));
    }

    @Override
    public Optional<Component> help(@NonNull CommandCause cause) {
        return Optional.of(command.getFullHelp())
            .map(help -> SpongeTextAdapter.convert(help, LocaleResolver.resolveLocale(cause.audience())));
    }

    @Override
    public Component usage(CommandCause cause) {
        return SpongeTextAdapter.convert(command.getUsage(), LocaleResolver.resolveLocale(cause.audience()));
    }
}
