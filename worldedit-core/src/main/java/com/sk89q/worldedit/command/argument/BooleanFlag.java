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

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;

import java.util.Collections;
import java.util.List;

public class BooleanFlag implements CommandExecutor<Boolean> {

    private final String description;

    public BooleanFlag(String description) {
        this.description = description;
    }

    @Override
    public Boolean call(CommandArgs args, CommandLocals locals) throws CommandException {
        return true;
    }

    @Override
    public List<String> getSuggestions(CommandArgs args, CommandLocals locals) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "";
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
