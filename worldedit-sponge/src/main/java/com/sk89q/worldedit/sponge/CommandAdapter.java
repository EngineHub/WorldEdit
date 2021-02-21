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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import net.kyori.adventure.text.Component;
import org.enginehub.piston.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.sk89q.worldedit.sponge.SpongeTextAdapter.convert;

public abstract class CommandAdapter implements org.spongepowered.api.command.Command.Raw {
    private final Command command;

    protected CommandAdapter(Command command) {
        this.command = command;
    }

    @Override
    public boolean canExecute(CommandCause source) {
        Set<String> permissions = command.getCondition().as(PermissionCondition.class)
            .map(PermissionCondition::getPermissions)
            .orElseGet(Collections::emptySet);
        for (String perm : permissions) {
            if (source.getSubject().hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Component> getShortDescription(CommandCause source) {
        Locale locale = source.getAudience() instanceof Player
            ? ((Player) source.getAudience()).getLocale()
            : WorldEdit.getInstance().getConfiguration().defaultLocale;
        return Optional.of(command.getDescription())
            .map(desc -> SpongeTextAdapter.convert(WorldEditText.format(desc, locale)));
    }

    @Override
    public Optional<Component> getHelp(CommandCause source) {
        Locale locale = source.getAudience() instanceof Player
            ? ((Player) source.getAudience()).getLocale()
            : WorldEdit.getInstance().getConfiguration().defaultLocale;
        return Optional.of(command.getFullHelp())
            .map(help -> SpongeTextAdapter.convert(WorldEditText.format(help, locale)));
    }

    @Override
    public Component getUsage(CommandCause source) {
        Locale locale = source.getAudience() instanceof Player
            ? ((Player) source.getAudience()).getLocale()
            : WorldEdit.getInstance().getConfiguration().defaultLocale;
        return convert(WorldEditText.format(command.getUsage(), locale));
    }
}
