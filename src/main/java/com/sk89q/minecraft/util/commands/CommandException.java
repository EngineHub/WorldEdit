// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.minecraft.util.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class CommandException extends Exception {
    
    private static final long serialVersionUID = 870638193072101739L;
    private List<String> commandStack = new ArrayList<String>();

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

    public String toStackString(String prefix, String spacedSuffix) {
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
        return builder.toString();
    }

}
