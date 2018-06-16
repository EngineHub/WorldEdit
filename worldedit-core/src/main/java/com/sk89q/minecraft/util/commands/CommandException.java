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

package com.sk89q.minecraft.util.commands;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class CommandException extends Exception {

    private List<String> commandStack = new ArrayList<>();

    public CommandException() {
        super();
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable t) {
        super(message, t);
    }

    public CommandException(Throwable t) {
        super(t);
    }

    public void prependStack(String name) {
        commandStack.add(name);
    }

    /**
     * Gets the command that was called, which will include the sub-command
     * (i.e. "/br sphere").
     *
     * @param prefix the command shebang character (such as "/") -- may be empty
     * @param spacedSuffix a suffix to put at the end (optional) -- may be null
     * @return the command that was used
     */
    public String getCommandUsed(String prefix, @Nullable String spacedSuffix) {
        checkNotNull(prefix);
        StringBuilder builder = new StringBuilder();
        if (prefix != null) {
            builder.append(prefix);
        }
        ListIterator<String> li = commandStack.listIterator(commandStack.size());
        while (li.hasPrevious()) {
            if (li.previousIndex() != commandStack.size() - 1) {
                builder.append(" ");
            }
            builder.append(li.previous());
        }
        if (spacedSuffix != null) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(spacedSuffix);
        }
        return builder.toString().trim();
    }

}
