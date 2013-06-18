// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.rebar.command;


/**
 * Tracks a command registration.
 */
public class CommandMapping {
    
    private final String[] aliases;
    private final CommandCallable callable;
    
    /**
     * Create a new instance.
     * 
     * @param callable the command callable
     * @param alias a list of all aliases, where the first one is the primary one
     */
    public CommandMapping(CommandCallable callable, String... alias) {
        super();
        this.aliases = alias;
        this.callable = callable;
    }

    /**
     * Get the primary alias.
     * 
     * @return the primary alias
     */
    public String getPrimaryAlias() {
        return aliases[0];
    }
    
    /**
     * Get a list of all aliases.
     * 
     * @return aliases
     */
    public String[] getAllAliases() {
        return aliases;
    }
    
    /**
     * Get the callable
     * 
     * @return the callable
     */
    public CommandCallable getCallable() {
        return callable;
    }

    /**
     * Get the {@link Description} form the callable.
     * 
     * @return the description
     */
    public Description getDescription() {
        return getCallable().getDescription();
    }

}
