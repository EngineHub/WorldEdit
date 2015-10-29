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

package com.sk89q.worldedit.command.argument;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;

import java.util.Collections;
import java.util.List;

public class StringParser implements CommandExecutor<String> {

    private final String name;
    private final String description;
    private final String defaultSuggestion;

    public StringParser(String name, String description) {
        this(name, description, null);
    }

    public StringParser(String name, String description, String defaultSuggestion) {
        this.name = name;
        this.description = description;
        this.defaultSuggestion = defaultSuggestion;
    }

    @Override
    public String call(CommandArgs args, CommandLocals locals) throws CommandException {
        try {
            return args.next();
        } catch (MissingArgumentException e) {
            throw new CommandException("Missing value for <" + name + ">.");
        }
    }

    @Override
    public List<String> getSuggestions(CommandArgs args, CommandLocals locals) throws MissingArgumentException {
        String value = args.next();
        return value.isEmpty() && defaultSuggestion != null ? Lists.newArrayList(defaultSuggestion) : Collections.<String>emptyList();
    }

    @Override
    public String getUsage() {
        return "<" + name + ">";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return true;
    }

}
