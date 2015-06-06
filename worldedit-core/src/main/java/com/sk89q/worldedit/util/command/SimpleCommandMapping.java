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

import java.util.Arrays;

/**
 * Tracks a command registration.
 */
public class SimpleCommandMapping implements CommandMapping {
    
    private final String[] aliases;
    private final CommandCallable callable;
    
    /**
     * Create a new instance.
     * 
     * @param callable the command callable
     * @param alias a list of all aliases, where the first one is the primary one
     */
    public SimpleCommandMapping(CommandCallable callable, String... alias) {
        super();
        this.aliases = alias;
        this.callable = callable;
    }

    @Override
    public String getPrimaryAlias() {
        return aliases[0];
    }
    
    @Override
    public String[] getAllAliases() {
        return aliases;
    }
    
    @Override
    public CommandCallable getCallable() {
        return callable;
    }

    @Override
    public Description getDescription() {
        return getCallable().getDescription();
    }

    @Override
    public String toString() {
        return "CommandMapping{" +
                "aliases=" + Arrays.toString(aliases) +
                ", callable=" + callable +
                '}';
    }

}
