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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.util.command.CommandMapping;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class CommandWrapper extends CommandBase {
    private CommandMapping command;

    protected CommandWrapper(CommandMapping command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getPrimaryAlias();
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(command.getAllAliases());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    }

    @Override
    public String getUsage(ICommandSender icommandsender) {
        return "/" + command.getPrimaryAlias() + " " + command.getDescription().getUsage();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int compareTo(@Nullable ICommand o) {
        if (o == null) {
            return 0;
        } else {
            return super.compareTo(o);
        }
    }
}
