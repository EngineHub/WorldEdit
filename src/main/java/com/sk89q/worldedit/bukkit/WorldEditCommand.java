// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class WorldEditCommand extends org.bukkit.command.Command {

    public WorldEditCommand(Command command, CommandPermissions commandPermissions) {
        super(command.aliases()[0]);
        this.description = command.desc();
        this.usageMessage = "/"+getName()+" "+command.usage();

        List<String> aliases = new ArrayList<String>(Arrays.asList(command.aliases()));
        aliases.remove(0);
        this.setAliases(aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        // This method is never called. 
        return true;
    }
}
