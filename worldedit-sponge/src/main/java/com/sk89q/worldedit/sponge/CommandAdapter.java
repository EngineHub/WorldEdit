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

import com.sk89q.worldedit.util.command.CommandMapping;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public abstract class CommandAdapter implements CommandCallable {
    private CommandMapping command;

    protected CommandAdapter(CommandMapping command) {
        this.command = command;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        for (String perm : command.getDescription().getPermissions()) {
            if (!source.hasPermission(perm)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        String description = command.getDescription().getDescription();
        if (description != null && !description.isEmpty()) {
            return Optional.of(Text.of(description));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        String help = command.getDescription().getHelp();
        if (help != null && !help.isEmpty()) {
            return Optional.of(Text.of(help));
        }
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of(command.getDescription().getUsage());
    }
}
