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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thrown when a command is not used properly.
 *
 * <p>When handling this exception, print the error message if it is not null.
 * Print a one line help instruction unless {@link #isFullHelpSuggested()}
 * is true, which, in that case, the full help of the command should be
 * shown.</p>
 *
 * <p>If no error message is set and full help is not to be shown, then a generic
 * "you used this command incorrectly" message should be shown.</p>
 */
public class InvalidUsageException extends CommandException {

    private final CommandCallable command;
    private final boolean fullHelpSuggested;

    /**
     * Create a new instance with no error message and with no suggestion
     * that full and complete help for the command should be shown. This will
     * result in a generic error message.
     *
     * @param command the command
     */
    public InvalidUsageException(CommandCallable command) {
        this(null, command);
    }

    /**
     * Create a new instance with a message and with no suggestion
     * that full and complete help for the command should be shown.
     *
     * @param message the message
     * @param command the command
     */
    public InvalidUsageException(@Nullable String message, CommandCallable command) {
        this(message, command, false);
    }

    /**
     * Create a new instance with a message.
     *
     * @param message the message
     * @param command the command
     * @param fullHelpSuggested true if the full help for the command should be shown
     */
    public InvalidUsageException(@Nullable String message, CommandCallable command, boolean fullHelpSuggested) {
        super(message);
        checkNotNull(command);
        this.command = command;
        this.fullHelpSuggested = fullHelpSuggested;
    }

    /**
     * Get the command.
     *
     * @return the command
     */
    public CommandCallable getCommand() {
        return command;
    }

    /**
     * Get a simple usage string.
     *
     * @param prefix the command shebang (such as "/") -- may be blank
     * @return a usage string
     */
    public String getSimpleUsageString(String prefix) {
        return getCommandUsed(prefix, command.getDescription().getUsage());
    }

    /**
     * Return whether the full usage of the command should be shown.
     *
     * @return show full usage
     */
    public boolean isFullHelpSuggested() {
        return fullHelpSuggested;
    }
}
