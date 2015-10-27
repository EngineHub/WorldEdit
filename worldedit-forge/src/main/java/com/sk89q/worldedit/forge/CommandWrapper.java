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

import com.google.common.base.Joiner;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.util.command.CommandMapping;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CommandWrapper extends CommandBase {
    private CommandMapping command;

    protected CommandWrapper(CommandMapping command) {
        this.command = command;
    }

    @Override
    public String getCommandName() {
        return command.getPrimaryAlias();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList(command.getAllAliases());
    }

    @Override
    public void processCommand(ICommandSender var1, String[] var2) {}

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
        if (sender instanceof EntityPlayerMP) {
            CommandSuggestionEvent event = new CommandSuggestionEvent(ForgeWorldEdit.inst.wrap((EntityPlayerMP) sender), command.getPrimaryAlias() + " " + Joiner.on(" ").join(arguments));
            WorldEdit.getInstance().getEventBus().post(event);
            return event.getSuggestions();
        } else {
            return super.addTabCompletionOptions(sender, arguments);
        }
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/" + command.getPrimaryAlias() + " " + command.getDescription().getUsage();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int compareTo(@Nullable Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof ICommand) {
            return super.compareTo((ICommand) o);
        } else {
            return 0;
        }
    }
}
