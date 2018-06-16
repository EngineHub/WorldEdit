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

package com.sk89q.worldedit.internal.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.util.command.CommandCompleter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the names of connected users as suggestions.
 */
public class UserCommandCompleter implements CommandCompleter {

    private final PlatformManager platformManager;

    /**
     * Create a new instance.
     *
     * @param platformManager the platform manager
     */
    public UserCommandCompleter(PlatformManager platformManager) {
        checkNotNull(platformManager);
        this.platformManager = platformManager;
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        Platform platform = platformManager.queryCapability(Capability.USER_COMMANDS);
        if (platform instanceof MultiUserPlatform) {
            List<String> suggestions = new ArrayList<>();
            Collection<Actor> users = ((MultiUserPlatform) platform).getConnectedUsers();
            for (Actor user : users) {
                if (user.getName().toLowerCase().startsWith(arguments.toLowerCase().trim())) {
                    suggestions.add(user.getName());
                }
            }
            return suggestions;
        } else {
            return Collections.emptyList();
        }
    }

}
