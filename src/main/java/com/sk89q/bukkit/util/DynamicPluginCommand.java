// $Id$
/*
 * WorldEdit
 * Copyright (C) 2011 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.bukkit.util;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Arrays;

/**
* @author zml2008
*/
public class DynamicPluginCommand extends org.bukkit.command.Command {

    protected final CommandExecutor owner;

    public DynamicPluginCommand(String[] aliases, String desc, String usage, CommandExecutor owner) {
        super(aliases[0], desc, usage, Arrays.asList(aliases));
        this.owner = owner;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return owner.onCommand(sender, this, label, args);
    }
    
    public Object getOwner() {
        return owner;
    }
}
