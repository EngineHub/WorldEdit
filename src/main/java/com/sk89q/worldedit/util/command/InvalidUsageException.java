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

package com.sk89q.worldedit.util.command;

import com.sk89q.minecraft.util.commands.CommandException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thrown when a command is not used properly.
 */
public class InvalidUsageException extends CommandException {

    private static final long serialVersionUID = -3222004168669490390L;
    private final CommandCallable command;
    private final boolean fullUsageSuggested;

    public InvalidUsageException(CommandCallable command) {
        this(null, command);
    }

    public InvalidUsageException(String message, CommandCallable command) {
        this(message, command, false);
    }

    public InvalidUsageException(String message, CommandCallable command, boolean fullUsageSuggested) {
        super(message);
        checkNotNull(command);
        this.command = command;
        this.fullUsageSuggested = fullUsageSuggested;
    }

    public CommandCallable getCommand() {
        return command;
    }

    public String getUsage(String prefix) {
        return toStackString(prefix, command.getDescription().getUsage());
    }

    /**
     * Return whether the full usage of the command should be shown.
     *
     * @return show full usage
     */
    public boolean isFullUsageSuggested() {
        return fullUsageSuggested;
    }
}
